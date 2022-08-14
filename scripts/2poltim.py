import os

import json
import datetime
from firebase_admin import storage
from tomlkit import key
 
from utils.utilities import jsonFolder, similar, titleDeFormat, initialise, pushDoubleToDB
from rake_nltk import Rake
from firebase_admin import db

log_tim = "/home/bencapper/src/News-Aggregator/scripts/log/timdone.log"
tim_ref = list()
tim_words = list()
log_pol = "/home/bencapper/src/News-Aggregator/scripts/log/politicodone.log"
pol_ref = list()
pol_words = list()
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"


db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
db_path = "stories"

json_tim_dump_path = "/home/bencapper/src/News-Aggregator/scripts/json/tim.json"
json_pol_dump_path = "/home/bencapper/src/News-Aggregator/scripts/json/pol.json"
json_folder_path = "/home/bencapper/src/News-Aggregator/scripts/json/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"

initialise(json_path, db_url, bucket)

key_checker = Rake()

# Read from Existing Log
if os.path.exists(log_tim):
   open_temp = open(log_tim, "r")
   read_temp = open_temp.read()
   tim_ref = read_temp.splitlines()
else:
   os.mknod(log_tim)

if os.path.exists(json_tim_dump_path):
   pass
else:
   os.mknod(json_tim_dump_path)

if os.path.exists(log_pol):
   open_temp = open(log_pol, "r")
   read_temp = open_temp.read()
   pol_ref = read_temp.splitlines()
else:
   os.mknod(log_pol)

if os.path.exists(json_pol_dump_path):
   pass
else:
   os.mknod(json_pol_dump_path)

jsonFolder(json_folder_path)


for line in tim_ref:
   line = titleDeFormat(line)
   tim_words.append(line)

for line in pol_ref:
   line = titleDeFormat(line)
   pol_words.append(line)


tim_title = ""
pol_title = ""
for i in tim_words:
   tim_sent = i
   for j in pol_words:
      if similar(tim_sent, j) > .51:
         tim_title = tim_sent
         pol_title = j

matches = list()

for i in range(len(tim_words)):
   tim_word = tim_words[i]
   tim_word = tim_word.split(' ')
   pol_word = pol_words[i]
   pol_word = pol_word.split(' ')
   sent_len = 0
   if len(tim_word) < len(pol_word):
      sent_len = len(tim_word)
   else:
      sent_len = len(pol_word)
   for i in range(sent_len):
      if tim_word == pol_word:
         matches.append(tim_word)

print(matches)
print(tim_title)
print(pol_title)

if tim_title == "" or pol_title == "":
   pass 
else:
   titlehead = ""
   for match in matches:
      titlehead = f"{titlehead} {match}" 

      open_json = open(json_tim_dump_path, "r")
      read_json = json.load(open_json)
      articles = read_json['articles']
      tart = dict()
      part = dict()
      for article in articles:
         title = article['title']
         if title == tim_title:
            tart = article

      open_json = open(json_pol_dump_path, "r")
      read_json = json.load(open_json)
      articles = read_json['articles']
      for article in articles:
         title = article['title']
         if title == pol_title:
            part = article

      print(tart['date'])
      print(part['link'])

      # Need to get matching words as category

      pushDoubleToDB(
         db_path,
         titlehead,
         tart['title'],
         tart['date'],
         tart['img_name'],
         tart['link'],
         tart["outlet"],
         tart['storage_link'],
         part['title'],
         part['date'],
         part['img_name'],
         part['link'],
         part["outlet"],
         part['storage_link']
         )

