import os

import json

from utils.utilities import jsonFolder, similar, titleDeFormat, initialise, pushDoubleToDB

log_gript = "/home/bencapper/src/News-Aggregator/scripts/log/griptdone.log"
gript_ref = list()
gript_words = list()
log_rte = "/home/bencapper/src/News-Aggregator/scripts/log/rtedone.log"
rte_ref = list()
rte_words = list()
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"


db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
db_path = "stories"

json_gript_dump_path = "/home/bencapper/src/News-Aggregator/scripts/json/gript.json"
json_rte_dump_path = "/home/bencapper/src/News-Aggregator/scripts/json/rte.json"
json_folder_path = "/home/bencapper/src/News-Aggregator/scripts/json/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"


jsonFolder(json_folder_path)
initialise(json_path, db_url, bucket)


# Read from Existing Log
if os.path.exists(log_gript):
   open_temp = open(log_gript, "r")
   read_temp = open_temp.read()
   gript_ref = read_temp.splitlines()
else:
   os.mknod(log_gript)

if os.path.exists(json_gript_dump_path):
   pass
else:
   os.mknod(json_gript_dump_path)

if os.path.exists(log_rte):
   open_temp = open(log_rte, "r")
   read_temp = open_temp.read()
   rte_ref = read_temp.splitlines()
else:
   os.mknod(log_rte)

if os.path.exists(json_rte_dump_path):
   pass
else:
   os.mknod(json_rte_dump_path)


for line in gript_ref:
   line = titleDeFormat(line)
   gript_words.append(line)

for line in rte_ref:
   line = titleDeFormat(line)
   rte_words.append(line)



gript_title = ""
rte_title = ""
for i in gript_words:
   gript_sent = i
   for j in rte_words:
      if similar(gript_sent, j) > .51:
         gript_title = gript_sent
         rte_title = j


matches = list()

for i in range(len(gript_words)):
   gript_word = gript_words[i]
   gript_word = gript_word.split(' ')
   rte_word = rte_words[i]
   rte_word = rte_word.split(' ')
   sent_len = 0
   if len(gript_word) < len(rte_word):
      sent_len = len(gript_word)
   else:
      sent_len = len(rte_word)
   for i in range(sent_len):
      if gript_word == rte_word:
         matches.append(gript_word)

print(matches)
print(gript_title)
print(rte_title)

if gript_title == "" or rte_title == "":
   pass 
else:
   titlehead = ""
   for match in matches:
      titlehead = f"{titlehead} {match}" 

   open_json = open(json_gript_dump_path, "r")
   read_json = json.load(open_json)
   articles = read_json['articles']
   gart = dict()
   rart = dict()
   for article in articles:
      title = article['title']
      if title == gript_title:
         gart = article

   open_json = open(json_rte_dump_path, "r")
   read_json = json.load(open_json)
   articles = read_json['articles']
   for article in articles:
      title = article['title']
      if title == rte_title:
         rart = article

   print(gart)
   print(rart)

   # Need to get matching words as category

   pushDoubleToDB(
      db_path,
      titlehead,
      gart['title'],
      gart['date'],
      gart['img_title'],
      gart['link'],
      gart["outlet"],
      gart['storage_link'],
      rart['title'],
      rart['date'],
      rart['img_title'],
      rart['link'],
      rart["outlet"],
      rart['storage_link']
      )
