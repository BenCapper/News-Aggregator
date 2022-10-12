import os

log_path = "/home/bencapper/src/News-Aggregator/scripts/log"

log_list = os.listdir(log_path)

for log in log_list:
    os.chdir(log_path)
    open(f"{log}","w").close()

