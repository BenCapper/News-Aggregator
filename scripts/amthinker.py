import os
from uuid import uuid4
import requests
from bs4 import BeautifulSoup
from firebase_admin import storage
from utils.utilities import (formatDate, imgFolder, imgTitleFormat, initialise, jsonFolder, dumpJson, appendJson, pushToDbNoImg,
                          todayDate,logFolder, pageSoup, pushToDB, titleFormat, similar,getHour)
# Set Global Variables
ref_list = []
log_file_path = "/home/bencapper/src/News-Aggregator/scripts/log/amthinkerdone.log"
log_folder_path = "/home/bencapper/src/News-Aggregator/scripts/log/"
json_dump_path = "/home/bencapper/src/News-Aggregator/scripts/json/amthinker.json"
json_folder_path = "/home/bencapper/src/News-Aggregator/scripts/json/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
page_url = "https://www.americanthinker.com"
storage_path = "https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o"
db_path = "stories"
outlet = "www.AmericanThinker.com"

# Set Local Folders
logFolder(log_folder_path)
jsonFolder(json_folder_path)

# Read from Existing Log
if os.path.exists(log_file_path):
 open_temp = open(log_file_path, "r")
 read_temp = open_temp.read()
 ref_list = read_temp.splitlines()
else:
 os.mknod(log_file_path)

# Initialize Firebase
initialise(json_path, db_url, bucket)

# Order Based on Current Hour
# Reversed in Android Studio
# to Make Sure The Most Recent
# Articles are Shown First
order = getHour()

# Gather News Page HTML
# Find the Div Containing
# Targeted Article Links
soup = pageSoup(page_url)
articles = soup.find_all("div", "home_entry")

# Cycle through List just Gathered
# And Save to DB or Pass due to Lack
# of Information Available
for article in articles:
    try:
        link = str(article).split('href="')[1].split('">')[0]
        link = f"{page_url}{link}"
        title = str(article).split('href="')[1].split('">')[1].split('</a')[0]
        title = titleFormat(title)

        full_page = requests.get(link).content
        articleSoup = BeautifulSoup(full_page, features="lxml")

        date = articleSoup.find('div', 'article_date')
        date = str(date).split('">')[1].split('</d')[0]
        date = date.replace(',',' ').replace('  ',' ').split(' ')
        dates = list()
        dates.append(date[1])
        dates.append(date[0])
        dates.append(date[2])
        date = formatDate(dates)
        storage_link = "https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/AmThinker%2Famthink.jpg?alt=media&token=bcb3f4db-4354-4f80-b5fb-e785b9aaa853"

        check = False
        for ref in ref_list:
            similarity = similar(ref,title)
            if similarity > .8 or "<span" in title:
              check = True
              break
                 # Only Continue if the Title is not
             # Already in the Log and is not too
             # Similar to Another
        if title not in ref_list and check is False:
            # Add the Title to the List
            # of Titles Already in the Log
            ref_list.append(title)
            open_temp = open(log_file_path, "a")
            data = {
                 "title": title,
                 "date": date,
                 "link": link,
                 "outlet": outlet,
                 "storage_link": storage_link,
                 "order": order
            }
            open_json = open(json_dump_path, "r")
            read_json = open_json.read()
            appendJson(json_dump_path,data)
            # Push the Gathered Data to DB
            # Using Utils method
            pushToDbNoImg(
               db_path, title, date, link, outlet, storage_link, order
            )
            # Write Title to Local Log File
            open_temp.write(str(title) + "\n")
            print("American Thinker Article Added to DB")
        else:
            print("American Thinker Article Already in DB")
    except:
        print('American Thinker Article Error')
