import os

import json

from utils.utilities import jsonFolder, similar, titleDeFormat, initialise, pushDoubleToDB

log_gb = "/home/bencapper/src/News-Aggregator/scripts/log/gbdone.log"
gb_ref = list()
gb_words = list()
log_sky = "/home/bencapper/src/News-Aggregator/scripts/log/skyukdone.log"
sky_ref = list()
sky_words = list()
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"


db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
db_path = "stories"

json_gb_dump_path = "/home/bencapper/src/News-Aggregator/scripts/json/gb.json"
json_sky_dump_path = "/home/bencapper/src/News-Aggregator/scripts/json/sky.json"
json_folder_path = "/home/bencapper/src/News-Aggregator/scripts/json/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"


jsonFolder(json_folder_path)
initialise(json_path, db_url, bucket)


# Read from Existing Log
if os.path.exists(log_gb):
   open_temp = open(log_gb, "r")
   read_temp = open_temp.read()
   gb_ref = read_temp.splitlines()
else:
   os.mknod(log_gb)

if os.path.exists(json_gb_dump_path):
   pass
else:
   os.mknod(json_gb_dump_path)

if os.path.exists(log_sky):
   open_temp = open(log_sky, "r")
   read_temp = open_temp.read()
   sky_ref = read_temp.splitlines()
else:
   os.mknod(log_sky)

if os.path.exists(json_sky_dump_path):
   pass
else:
   os.mknod(json_sky_dump_path)


for line in gb_ref:
   line = titleDeFormat(line)
   gb_words.append(line)

for line in sky_ref:
   line = titleDeFormat(line)
   sky_words.append(line)



gb_title = ""
sky_title = ""
for i in gb_words:
   gb_sent = i
   for j in sky_words:
      if similar(gb_sent, j) > .51:
         gb_title = gb_sent
         sky_title = j


matches = list()

for i in range(len(gb_words)):
   gb_word = gb_words[i]
   gb_word = gb_word.split(' ')
   sky_word = sky_words[i]
   sky_word = sky_word.split(' ')
   sent_len = 0
   if len(gb_word) < len(sky_word):
      sent_len = len(gb_word)
   else:
      sent_len = len(sky_word)
   for i in range(sent_len):
      if gb_word == sky_word:
         matches.append(gb_word)

print(matches)

print(gb_title)
print(sky_title)

if gb_title == "" or sky_title == "":
   pass 
else:
   titlehead = ""
   for match in matches:
      titlehead = f"{titlehead} {match}" 
   
   open_json = open(json_gb_dump_path, "r")
   read_json = json.load(open_json)
   articles = read_json['articles']
   gart = dict()
   sart = dict()
   for article in articles:
      title = article['title']
      if title == gb_title:
         gart = article
   
   open_json = open(json_sky_dump_path, "r")
   read_json = json.load(open_json)
   articles = read_json['articles']
   for article in articles:
      title = article['title']
      if title == sky_title:
         sart = article
   
   print(gart)
   print(sart)
   
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
      sart['title'],
      sart['date'],
      sart['img_title'],
      sart['link'],
      sart["outlet"],
      sart['storage_link']
      )