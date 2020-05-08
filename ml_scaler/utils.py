import os
import sys
import subprocess
import re
import requests
from collections import defaultdict
import time
import pandas as pd
from datetime import datetime


# Get Environment variables
FLINK_HOME = os.environ['FLINK_HOME']
KAFKA_HOME = os.environ['KAFKA_HOME']
FLINK_MASTER = os.environ['FLINK_MASTER']
WORKING_DIR = os.environ['WORKING_DIR']
FLINK_QUERIES_JAR = os.environ['FLINK_QUERIES_JAR']
DATAGEN_JAR = os.environ['DATAGEN_JAR']
MAX_PARALLELISM = int(os.environ['MAX_PARALLELISM'])
JOLOKIA_URL = os.environ['JOLOKIA_URL']
TMP_DIR = os.environ['TMP_DIR']


if not FLINK_HOME:
    print("Environmental variables not set! Exiting! ")
    sys.exit(1)


def start_everything():
    cmd = '{WORKING_DIR}/start_everything.sh'.format(WORKING_DIR=WORKING_DIR)
    subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    time.sleep(30)
    print("Everything started...Probably...")


def stop_everything():
    output = subprocess.check_output('{WORKING_DIR}/stop_everything.sh'.format(WORKING_DIR=WORKING_DIR))
    print(output.decode("utf-8"))


def run_job(arguments, parallelism, jarpath=FLINK_QUERIES_JAR):
    return_value = subprocess.check_output('''{FLINK_HOME}/bin/flink run -m {FLINK_MASTER} -d
    -p {parallelism} {jarpath} {arguments}'''
                                           .format(FLINK_HOME=FLINK_HOME,
                                                   FLINK_MASTER=FLINK_MASTER,
                                                   parallelism=parallelism,
                                                   arguments=arguments,
                                                   jarpath=jarpath).split())
    print(return_value)
    return return_value.split()[-1].decode("utf-8")


def list_running_jobs():
    return_value = subprocess.check_output('''{FLINK_HOME}/bin/flink list -r -m {FLINK_MASTER}'''
                                           .format(FLINK_HOME=FLINK_HOME, FLINK_MASTER=FLINK_MASTER).split())
    print(return_value)
    job_list = re.findall(': (.{32}) : (--\S*) \(RUNNING\)', str(return_value))
    return job_list


def set_parallelism(job_id, job_name, new_parallelism):
    job_args, cur_parallelism = job_name.split(',')
    cur_parallelism = int(cur_parallelism)
    new_parallelism = min(MAX_PARALLELISM, new_parallelism)
    if new_parallelism != cur_parallelism:
        cancel_job(job_id)
        run_job(arguments=job_args, parallelism=new_parallelism)
    else:
        print("Parallelism not changed, because it was set to the same that already in place:", cur_parallelism)


def increase_parallelism(job_id, job_name, step):
    job_args, cur_parallelism = job_name.split(',')
    cur_parallelism = int(cur_parallelism)
    new_parallelism = cur_parallelism + step
    new_parallelism = min(MAX_PARALLELISM, new_parallelism)
    if new_parallelism != cur_parallelism:
        cancel_job(job_id)
        run_job(arguments=job_args, parallelism=new_parallelism)
    else:
        print("Not possible to increase parallelism! Reached maximum parallelism of",cur_parallelism)


def decrease_parallelism(job_id, job_name, step):
    job_args, cur_parallelism = job_name.split(',')
    cur_parallelism = int(cur_parallelism)
    new_parallelism = cur_parallelism - step
    new_parallelism = max(1, new_parallelism)
    if new_parallelism != cur_parallelism:
        cancel_job(job_id)
        run_job(arguments=job_args, parallelism=new_parallelism)
    else:
        print("Not possible to decrease parallelism! Current parallelism is", cur_parallelism)


def cancel_job(job_id):
    return_value = subprocess.check_output('''{FLINK_HOME}/bin/flink cancel {job_id} -m {FLINK_MASTER}'''
                                           .format(FLINK_HOME=FLINK_HOME, job_id=job_id, FLINK_MASTER=FLINK_MASTER).split())
    print(return_value)


def topic_byte_rate(topic, inout='In'):
    if inout not in ['In', 'Out']:
        print("Set inout to In/Out for topic input/output byte rate")
        return None
    r = requests.get('http://{url}/read/kafka.server:name=Bytes{inout}PerSec,topic={topic},type=BrokerTopicMetrics'.format(url='127.0.0.1:8778/jolokia', inout=inout, topic=topic))
    return r.json().get('value').get('OneMinuteRate')


def topic_inmessage_rate(topic):
    r = requests.get('http://{url}/read/kafka.server:name=MessagesInPerSec,topic={topic},type=BrokerTopicMetrics'.format(url='127.0.0.1:8778/jolokia', topic=topic))
    return r.json().get('value').get('OneMinuteRate')


class FlinkJob(object):
    def __init__(self, argument, parallelism):
        self.argument = argument
        self.parallelism = parallelism
        self.jid = run_job(self.argument, self.parallelism)
        print("FJ: Spawned new", self)

    def kill(self):
        print("FJ: Cancelling", self)
        cancel_job(self.jid)

    def set_parallelism(self, parallelism):
        if self.parallelism == parallelism:
            print("FJ: Nothing happened. New parallelism is the same as current one")
            return
        self.kill()
        self.jid = run_job(self.argument, parallelism)
        self.parallelism = parallelism
        print("FJ: New", self)

    def __str__(self):
        return 'Flink Job ({0}) with id {1} and parallelism {2}'.format(self.argument, self.jid, self.parallelism)


class DataGenerator(object):
    def __init__(self):
        self.process_dict = defaultdict(list)
        self.rate_dict = defaultdict(int)

    def set_rate(self, argument, rate):
        self.stop(argument)
        maximum_thread_rate = 50000
        rate_steps = rate//maximum_thread_rate
        cmd_nf = "java -jar {jar} {topic} 0 {rate}"
        for i in range(rate_steps):
            cmd = cmd_nf \
                .format(jar=DATAGEN_JAR, topic=argument, rate=maximum_thread_rate)
            print("DG: Running: ", cmd)
            s = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
            self.process_dict[argument].append(s)
        remainder = rate % maximum_thread_rate
        if remainder:
            cmd = cmd_nf \
                .format(jar=DATAGEN_JAR, topic=argument, rate=remainder)
            print("DG: Running: ", cmd)
            s = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
            self.process_dict[argument].append(s)
        self.rate_dict[argument] = rate

    def stop(self, key):
        if not self.process_dict.get(key):
            return
        for p in self.process_dict.get(key):
            print("DG: Killing process {0}...".format(p.pid))
            p.kill()
        self.process_dict[key].clear()

    def stop_all(self):
        for key in self.process_dict.keys():
            self.stop(key)

    def play_pattern(self, argument, pattern, filename=TMP_DIR +'/pattern{0}.csv', timing='sec'):
        """
        Plays a data generation pattern for a given topic
        :param timing: wait minutes(min) or seconds(sec) specified in pattern
        :param filename: name of the file to write results
        :param argument: [--clicks, --cars]
        :param pattern: [(rate, wait_time(timing)), .., ]
        """
        rates = []
        times = []
        start_time = time.time()
        for rate, wait_time in pattern:
            self.set_rate(argument, rate)
            rates.append(rate)
            times.append(time.time())
            if timing == 'sec':
                time.sleep(wait_time)
            else:
                time.sleep(wait_time*60)
            rates.append(rate)
            times.append(time.time())
        self.stop(argument)
        print("DG: Played pattern {0} in {1} minutes".format(pattern, (time.time() - start_time)/60))
        df = pd.DataFrame({'rate': rates, 'time': times})
        df.to_csv(filename.format(argument), index=False)


    def __str__(self):
        return 'DG: Processes running {0}. Current Rates: {1}'.format(self.process_dict, self.rate_dict)


