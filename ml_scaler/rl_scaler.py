import utils
import os
import sys
import time
import json
import subprocess
import math
from multiprocessing import Process
import atexit
import pandas as pd

# This file contains a q-learning based scaler
from config import all_jobs, actions, states, train_sleep_time, alpha, cutoff, \
    rescaling_latency, rescaling_interval, stability_period, pattern, pattern_timing, qtablefile, pattern_file


generator = utils.DataGenerator()


@atexit.register
def exit_function():
    print("EXIT: Stopping Generators")
    generator.stop_all()
    print("EXIT: Stopping Everything")
    utils.stop_everything()


def init_Q(policy='zeroes'):
    if policy == 'zeroes':
        Q = {}
        for state in states:
            Q[state] = [0 for x in actions]
    return Q


def check_for_backpressure(job_name, omega_latency=rescaling_latency):
    job_filename = job_name + '.csv'
    results_path = os.path.join(utils.TMP_DIR, job_filename)
    # df = pd.read_csv(results_path, header=None)
    line = subprocess.check_output(['tail', '-1', results_path]).decode("utf-8")
    current_latency = float(line.split(',')[-1].split('.')[0])
    print("Current latency", current_latency)
    if current_latency > omega_latency:
        return True
    return False


def get_current_latency(job_name):
    job_filename = job_name + '.csv'
    results_path = os.path.join(utils.TMP_DIR, job_filename)
    # df = pd.read_csv(results_path, header=None)
    line = subprocess.check_output(['tail', '-1', results_path]).decode("utf-8")
    current_latency = float(line.split(',')[-1].split('.')[0])
    print("Current latency", current_latency)
    return current_latency


def calculate_reward(rate_in, rate_out, cur_par):
    ratio_io = (rate_in/rate_out)
    ratio_par = (cur_par/utils.MAX_PARALLELISM)
    print("Ratio_io={0}, Ratio_par={1}".format(ratio_io, ratio_par))
    return - round(rate_in/rate_out, 2) - (cur_par/utils.MAX_PARALLELISM) * alpha


def training(job_type_arg, qtable=None, qtablefile=qtablefile):
    """
    :param qtablefile:
    :param job_type_arg: ['--clicks', '--cars']
    :return: learns and writes Q-table in q tablefile
    """
    if not qtable:
        Q = init_Q()
    else:
        Q = qtable
    min_opt_parallelism = 1
    topic_name = job_type_arg[2:] + '_bdapro'
    print("TRN: Starting everything")
    utils.start_everything()
    for state in states:
        print('TRN: -----------State w/ input {0} records/sec---------------'.format(state))
        job = utils.FlinkJob(job_type_arg, min_opt_parallelism)
        generator.set_rate(job_type_arg, state)
        best_reward = -math.inf
        best_io = math.inf
        for action in [x for x in actions if x > min_opt_parallelism] + [utils.MAX_PARALLELISM + 1]:
            print("TRN: Sleeping for {0} seconds".format(train_sleep_time))
            time.sleep(train_sleep_time)
            rate_in = float(utils.topic_byte_rate(topic=topic_name, inout='In'))
            byte_states.append(rate_in)
            print("TRN: Input rate (records)", utils.topic_inmessage_rate(topic_name))
            rate_out = float(utils.topic_byte_rate(topic=topic_name, inout='Out'))
            reward = calculate_reward(rate_in, rate_out, job.parallelism)
            print("TRN: Reward", reward)
            Q[state][job.parallelism - 1] = reward
            print("TRN: Q-Table", Q)
            if reward > best_reward:
                best_reward = reward
                best_io = rate_in/rate_out
                min_opt_parallelism = job.parallelism
                print("TRN: best_reward", best_reward)
                print("TRN: min_opt_parallelism", min_opt_parallelism)
            elif best_io < cutoff:
                print("TRN: Breaking For Loop")
                break
            print("TRN: Parallelism to try", action)
            best_expected_reward = calculate_reward(1, 1, action)
            print("TRN: Best expected reward", best_expected_reward)
            if best_expected_reward < best_reward and best_io < cutoff:
                print("TRN: Not trying parallelism {0}, because expected reward more than current reward".format(action))
                break
            if action <= utils.MAX_PARALLELISM:
                job.set_parallelism(action)
        job.kill()
        utils.stop_everything()
        if action < utils.MAX_PARALLELISM + 1:
            utils.start_everything()
    with open(qtablefile.format(job_type_arg), 'w') as f:
        json.dump(Q, f)
    generator.stop(job_type_arg)
    utils.stop_everything()


def get_current_state(topic_name):
    rate_in = float(utils.topic_inmessage_rate(topic_name))
    print("Rate in:", rate_in)
    for state in states:
        if state > rate_in:
            return str(state)
    return str(states[-1])


def get_stabilized_state(topic_name, period=stability_period):
    while True:
        # let kafka metrics stabilize for grace_period seconds
        temp_state = get_current_state(topic_name)
        time.sleep(period)
        current_state = get_current_state(topic_name)
        if current_state == temp_state:
            break
    return current_state


def optimal_parallelism(q_table, state):
    l = q_table[state]
    l_filtered = [x for x in l if x != 0]
    maximum = max(l_filtered)
    return l.index(maximum) + 1


def testing(q_table, job_type_arg, latencies_filename=utils.TMP_DIR + '/latency{0}.csv'):
    print("TST: Starting everything")
    utils.start_everything()
    dg = utils.DataGenerator()
    p = Process(target=dg.play_pattern, args=(job_type_arg, pattern, pattern_file, pattern_timing))
    p.start()
    job_prefix = job_type_arg[2:]
    topic_name = job_prefix + '_bdapro'
    aggregator = utils.FlinkJob(argument='--agg', parallelism=1)
    job = utils.FlinkJob(argument=job_type_arg, parallelism=1)
    print("TST: Sleeping for 3*60 seconds")
    time.sleep(3*60)
    latencies = []
    times = []
    paralls = []
    state = get_current_state(topic_name)
    while p.is_alive():
        current_latency = get_current_latency(job_prefix)
        latencies.append(current_latency)
        times.append(time.time())
        paralls.append(job.parallelism)
        if current_latency > rescaling_latency:
            print("TST: We have latency higher than omega latency of", rescaling_latency)
            temp_state = get_current_state(topic_name)
            if optimal_parallelism(q_table, temp_state) != job.parallelism:
                print("TST: Getting stabilized state")
                state = get_stabilized_state(topic_name, 10)
            print("TST: Current state:", state)
            opt_par = optimal_parallelism(q_table, state)
            print("TST: Optimal parallelism:", opt_par)
            # add output results for plotting just before changing parallelism
            latencies.append(get_current_latency(job_prefix))
            times.append(time.time())
            paralls.append(job.parallelism)
            job.set_parallelism(opt_par)
            time.sleep(rescaling_interval)
        else:
            print("TST: Latency less than omega latency of", rescaling_latency)
            # check if parallelism is too high for the current state
            temp_state = get_current_state(topic_name)
            if optimal_parallelism(q_table, temp_state) < job.parallelism:
                while True:
                    # let kafka metrics stabilize for grace_period seconds
                    temp_state = get_current_state(topic_name)
                    time.sleep(stability_period)
                    current_state = get_current_state(topic_name)
                    if current_state >= temp_state:
                        break
                state = current_state
                print("TST: Current state:", current_state)
                opt_par = optimal_parallelism(q_table, current_state)
                print("TST: Optimal parallelism:", opt_par)
                if opt_par < job.parallelism:
                    job.set_parallelism(opt_par)
            else:
                print("TST: Sleeping for 30 seconds")
                time.sleep(rescaling_interval)
        print(dg)
    df = pd.DataFrame({'latency': latencies, 'parallelism': paralls, 'time': times})
    df.to_csv(latencies_filename.format(job_type_arg), index=False)
    dg.stop_all()
    aggregator.kill()
    job.kill()
    utils.stop_everything()


if __name__ == '__main__':
    if sys.argv[1] == 'training':
        for job in all_jobs:
            training(job)
    elif sys.argv[2] == 'testing':
        for job in all_jobs:
            with open(qtablefile.format(job), 'r') as f:
                print("Reading Q table for job", job)
                q_table = json.load(f)
                print("Qtable", q_table)
            testing(q_table, job)
