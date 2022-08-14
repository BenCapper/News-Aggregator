import os

import json
import datetime
from firebase_admin import storage
from tomlkit import key
 
from utils.utilities import jsonFolder, similar, titleDeFormat, initialise, pushDoubleToDB
from rake_nltk import Rake
from firebase_admin import db

log_mill = "/home/bencapper/src/News-Aggregator/scripts/log/pmilldone.log"
mill_ref = list()
mill_words = list()
log_glo = "/home/bencapper/src/News-Aggregator/scripts/log/globaldone.log"
glo_ref = list()
glo_words = list()
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"


db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
db_path = "stories"

json_mill_dump_path = "/home/bencapper/src/News-Aggregator/scripts/json/mill.json"
json_glo_dump_path = "/home/bencapper/src/News-Aggregator/scripts/json/glo.json"
json_folder_path = "/home/bencapper/src/News-Aggregator/scripts/json/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"

initialise(json_path, db_url, bucket)

key_checker = Rake()

# Read from Existing Log
if os.path.exists(log_mill):
   open_temp = open(log_mill, "r")
   read_temp = open_temp.read()
   mill_ref = read_temp.splitlines()
else:
   os.mknod(log_mill)

if os.path.exists(json_mill_dump_path):
   pass
else:
   os.mknod(json_mill_dump_path)

if os.path.exists(log_glo):
   open_temp = open(log_glo, "r")
   read_temp = open_temp.read()
   glo_ref = read_temp.splitlines()
else:
   os.mknod(log_glo)

if os.path.exists(json_glo_dump_path):
   pass
else:
   os.mknod(json_glo_dump_path)

jsonFolder(json_folder_path)


for line in mill_ref:
   line = titleDeFormat(line)
   mill_words.append(line)

for line in glo_ref:
   line = titleDeFormat(line)
   glo_words.append(line)


mill_title = ""
glo_title = ""
for i in mill_words:
   mill_sent = i
   for j in glo_words:
      if similar(mill_sent, j) > .51:
         mill_title = mill_sent
         glo_title = j

matches = list()

for i in range(len(mill_words)):
   mill_word = mill_words[i]
   mill_word = mill_word.split(' ')
   glo_word = glo_words[i]
   glo_word = glo_word.split(' ')
   sent_len = 0
   if len(mill_word) < len(glo_word):
      sent_len = len(mill_word)
   else:
      sent_len = len(glo_word)
   for i in range(sent_len):
      if mill_word == glo_word:
         matches.append(mill_word)

print(matches)
print(mill_title)
print(glo_title)

if mill_title == "" or glo_title == "":
   pass 
else:
   titlehead = ""
   for match in matches:
      titlehead = f"{titlehead} {match}" 

   open_json = open(json_mill_dump_path, "r")
   read_json = json.load(open_json)
   articles = read_json['articles']
   mart = dict()
   gart = dict()
   for article in articles:
      title = article['title']
      if title == mill_title:
         mart = article

   open_json = open(json_glo_dump_path, "r")
   read_json = json.load(open_json)
   articles = read_json['articles']
   for article in articles:
      title = article['title']
      if title == glo_title:
         gart = article

   print(mart)
   print(gart)

   # Need to get matching words as category

   pushDoubleToDB(
      db_path,
      titlehead,
      mart['title'],
      mart['date'],
      mart['img_name'],
      mart['link'],
      mart["outlet"],
      mart['storage_link'],
      gart['title'],
      gart['date'],
      gart['img_name'],
      gart['link'],
      gart["outlet"],
      gart['storage_link']
      )

