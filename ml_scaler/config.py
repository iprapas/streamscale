import utils

# different arguments for flink jobs
all_jobs = ['--clicks', '--cars']
# different parallelism levels
actions = range(1, utils.MAX_PARALLELISM + 1)
input_rate_step = 7500
steps = 20
states = range(input_rate_step, (steps+1)*input_rate_step, input_rate_step)
# train sleep time between probing kafka for reward
train_sleep_time = 5*60
# parallelism penalty coefficient
alpha = 0.3
# min acceptable ratio_io
cutoff = 1.15
# if latency > omega_latency, change state
rescaling_latency = 2500
# interval (in seconds) for which to check if rescaling is needed
rescaling_interval = 30
# be in the same state for one minute before deciding that this is the state you are in
stability_period = 60
# pattern for testing [(rate, time), ...]
pattern = [(10000, 7), (27000, 7), (42000, 7), (120000, 7),  (20000, 7),
           (5000, 7), (60000, 15), (30000, 7), (75000, 7), (5000, 7)]
#  pattern time minutes (min) or seconds (sec)
pattern_timing = 'min'
# file format to save Q table
qtablefile= utils.TMP_DIR + '/qtable{0}.json'
# file format to save pattern measurements
pattern_file = utils.TMP_DIR + '/pattern{0}.csv'