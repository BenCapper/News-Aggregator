import os
 
import datetime
from firebase_admin import storage
from tomlkit import key
 
from utils.utilities import similar, titleDeFormat
from rake_nltk import Rake

log_gript = "/home/bencapper/src/News-Aggregator/scripts/log/griptdone.log"
gript_ref = list()
gript_words = list()
log_rte = "/home/bencapper/src/News-Aggregator/scripts/log/rtedone.log"
rte_ref = list()
rte_words = list()


key_checker = Rake()

# Read from Existing Log
if os.path.exists(log_gript):
   open_temp = open(log_gript, "r")
   read_temp = open_temp.read()
   gript_ref = read_temp.splitlines()
else:
   os.mknod(log_gript)

if os.path.exists(log_rte):
   open_temp = open(log_rte, "r")
   read_temp = open_temp.read()
   rte_ref = read_temp.splitlines()
else:
   os.mknod(log_rte)


for line in gript_ref:
   line = titleDeFormat(line)
   gript_words.append(line)

for line in rte_ref:
   line = titleDeFormat(line)
   rte_words.append(line)

for i in gript_words:
   gript_sent = i
   for j in rte_words:
      if similar(gript_sent, j) > .5:
         print(f"gript = {gript_sent}")
         print(f"rte = {j}")
         print()
         print()
