import os

import json

from utils.utilities import jsonFolder, similar, titleDeFormat, initialise, pushDoubleToDB

log_rev = "/home/bencapper/src/News-Aggregator/scripts/log/revdone.log"
rev_ref = list()
rev_words = list()
log_yah = "/home/bencapper/src/News-Aggregator/scripts/log/yahdone.log"
yah_ref = list()
yah_words = list()
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"


db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
db_path = "stories"

json_rev_dump_path = "/home/bencapper/src/News-Aggregator/scripts/json/rev.json"
json_yah_dump_path = "/home/bencapper/src/News-Aggregator/scripts/json/yah.json"
json_folder_path = "/home/bencapper/src/News-Aggregator/scripts/json/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"


jsonFolder(json_folder_path)
initialise(json_path, db_url, bucket)


# Read from Existing Log
if os.path.exists(log_rev):
   open_temp = open(log_rev, "r")
   read_temp = open_temp.read()
   rev_ref = read_temp.splitlines()
else:
   os.mknod(log_rev)

if os.path.exists(json_rev_dump_path):
   pass
else:
   os.mknod(json_rev_dump_path)

if os.path.exists(log_yah):
   open_temp = open(log_yah, "r")
   read_temp = open_temp.read()
   yah_ref = read_temp.splitlines()
else:
   os.mknod(log_yah)

if os.path.exists(json_yah_dump_path):
   pass
else:
   os.mknod(json_yah_dump_path)


for line in rev_ref:
   line = titleDeFormat(line)
   rev_words.append(line)

for line in yah_ref:
   line = titleDeFormat(line)
   yah_words.append(line)



rev_title = ""
yah_title = ""
for i in rev_words:
   rev_sent = i
   for j in yah_words:
      if similar(rev_sent, j) > .51:
         rev_title = rev_sent
         yah_title = j


matches = list()

for i in range(len(rev_words)):
   rev_word = rev_words[i]
   rev_word = rev_word.split(' ')
   yah_word = yah_words[i]
   yah_word = yah_word.split(' ')
   sent_len = 0
   if len(rev_word) < len(yah_word):
      sent_len = len(rev_word)
   else:
      sent_len = len(yah_word)
   for i in range(sent_len):
      if rev_word == yah_word:
         matches.append(rev_word)

print(matches)
print(rev_title)
print(yah_title)


if rev_title == "" or yah_title == "":
   pass 
else:
    titlehead = ""
    for match in matches:
       titlehead = f"{titlehead} {match}" 



    open_json = open(json_rev_dump_path, "r")
    read_json = json.load(open_json)
    articles = read_json['articles']
    rart = dict()
    yart = dict()
    for article in articles:
       title = article['title']
       if title == rev_title:
          rart = article

    open_json = open(json_yah_dump_path, "r")
    read_json = json.load(open_json)
    articles = read_json['articles']
    for article in articles:
       title = article['title']
       if title == yah_title:
          yart = article

    print(rart)
    print(yart)

    # Need to get matching words as category

    pushDoubleToDB(
       db_path,
       titlehead,
       rart['title'],
       rart['date'],
       rart['img_title'],
       rart['link'],
       rart["outlet"],
       rart['storage_link'],
       yart['title'],
       yart['date'],
       yart['img_title'],
       yart['link'],
       yart["outlet"],
       yart['storage_link']
       )


