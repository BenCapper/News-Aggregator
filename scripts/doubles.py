import os
import json
from uuid import uuid4
from utils.utilities import jsonFolder, similar, titleDeFormat, initialise, pushDoubleToDB, logFolder, titleFormat, todayDate
 
double_log_gb = "/home/bencapper/src/News-Aggregator/scripts/doublelog/double.log"
double_gb_ref = list()
 
match_log = "/home/bencapper/src/News-Aggregator/scripts/doublelog/matches.log"
match_ref = list()
json_log_path = "/home/bencapper/src/News-Aggregator/scripts/json"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"
 
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
db_path = "doubles"
 
json_folder_path = "/home/bencapper/src/News-Aggregator/scripts/json/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"
 
logFolder(double_log_gb)
jsonFolder(json_folder_path)
initialise(json_path, db_url, bucket)

if os.path.exists(match_log):
   open_temp = open(match_log, "r")
   read_temp = open_temp.read()
   match_ref = read_temp.splitlines()
else:
   os.mknod(match_log)
 
left_json = ["euron.json","global.json","guard.json","vox.json",
            "huff.json","pol.json","rte.json","yah.json",
            "abc.json","beast.json","cbs.json","sky.json"]
right_json = ["tim.json","caller.json","gb.json","spiked.json","zero.json",
            "gript.json","gwp.json","rev.json","blaze.json","bong.json","breit.json"]

td = todayDate()
outlet_logs = os.listdir(json_log_path)
right_log = list()
left_log = list()
matches = list()

for outlet in outlet_logs:
    try:
        outlet_log = open(f"{json_log_path}/{outlet}")
        outlet_data = json.load(outlet_log)
        if outlet in left_json:
            for data in outlet_data['articles'][2:]:
                left_log.append(data)
        else:
            for data in outlet_data['articles'][2:]:
                right_log.append(data)
        outlet_log.close()
    except:
        print("None")


for article in right_log:

    date1 = article['date']
    title1 = titleDeFormat(article['title'])
    outlet1 = article['outlet'].split('.')[1].split('.')[0]
    for article2 in left_log:
        date2 = article2['date']
        title2 = titleDeFormat(article2['title'])
        outlet2 = article2['outlet'].split('.')[1].split('.')[0]
        if similar(title1,title2) > .7 and article['title'] not in match_ref and article2['title'] not in match_ref:
            if date1 == td or date2 == td:
                t1 = article['title']
                t2 = article2['title']
                match_ref.append(t1)
                match_ref.append(t2)
                open_temp = open(match_log, "a")
                open_temp.write(f"{t1}" + "\n")
                open_temp.write(f"{t2}" + "\n")
                open_temp.close()
                matches.append(f"{t1} // {t2}")
                break

for match in matches:
    right_article = dict()
    left_article = dict()
    title1 = match.split(" // ")[0]
    title2 = match.split(" // ")[1]
    for article in right_log:
        if title1 == article['title']:
            right_article = article
    for article in left_log:
        if title2 == article['title']:
            left_article = article

    if right_article['link'] == left_article['link']:
        pass
    else:
        key = uuid4()
        pushDoubleToDB(
            db_path,
            str(key),
            right_article['title'],
            right_article['date'],
            right_article['link'],
            right_article['outlet'],
            right_article['storage_link'],
            left_article['title'],
            left_article['date'],
            left_article['link'],
            left_article['outlet'],
            left_article['storage_link'],
            right_article['order']
        )
        print(f"Double Article Added to DB {match}")