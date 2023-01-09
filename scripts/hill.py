import os
from uuid import uuid4
import requests
from bs4 import BeautifulSoup
from firebase_admin import storage
from utils.utilities import (formatDate, imgTitleFormat, initialise, jsonFolder, appendJson, todayDate, imgFolder,
                          logFolder, pageSoup, pushToDB, titleFormat, similar,getHour)


td = todayDate()
# Set Global Variables
ref_list = []
log_file_path = "/home/bencapper/src/News-Aggregator/scripts/log/hill.log"
log_folder_path = "/home/bencapper/src/News-Aggregator/scripts/log/"
json_dump_path = "/home/bencapper/src/News-Aggregator/scripts/json/hill.json"
json_folder_path = "/home/bencapper/src/News-Aggregator/scripts/json/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
page_url = "https://thehill.com/news/"
img_path = f"/home/bencapper/src/News/Hill/{td}"
storage_path = "https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o"
db_path = "tests"
outlet = "www.TheHill.com"


# Set Local Folders
logFolder(log_folder_path)
imgFolder(img_path)
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
articles = soup.find_all("article", "archive__item")

# Cycle through List just Gathered
# And Save to DB or Pass due to Lack
# of Information Available
for article in articles[1:]:
    try:
        url = article.find("a")
        url = str(url).split('href="')[1].split('" rel')[0]
        img = article.find("a")
        img_src = str(img).split('320w, ')[1].split(' 512w,')[0]
        title = article.find("h2")
        title = title.find("a")
        title = str(title).split('">')[1].split('</a')[0].lstrip().rstrip()
        title = titleFormat(title)
        img_title = imgTitleFormat(title)
        date = article.find("p","archive__item__date")
        date = str(date).split('">')[1].split('</p')[0].rstrip().lstrip().split(' ')[0].replace('/','-')
        bucket = storage.bucket()
        token = ""

        check = False
        for ref in ref_list:
            similarity = similar(ref,title)
            if similarity > .8 or "<span" in title:
              check = True
              break

        if title not in ref_list and check is False:
            ref_list.append(title)
            open_temp = open(log_file_path, "a")

            with open(f"{img_path}/{img_title}", "wb") as img:
                headers = {"User-Agent": "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.96 Safari/537.36"} 
                img.write(requests.get(img_src, headers=headers).content)
                blob = bucket.blob(f"Hill/{td}/{img_title}")
                token = uuid4()
                metadata = {"firebaseStorageDownloadTokens": token}
                blob.upload_from_filename(f"{img_path}/{img_title}")

            # Get Link to the Stored Image
            storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/Hill%2F{td}%2F{img_title}?alt=media&token={token}"
            data = {
                 "title": title,
                 "date": date,
                 "link": url,
                 "outlet": outlet,
                 "storage_link": storage_link,
                 "order": order
            }
            open_json = open(json_dump_path, "r")
            read_json = open_json.read()
            appendJson(json_dump_path,data)
            # Push the Gathered Data to DB
            # Using Utils method
            pushToDB(
               db_path, title, date, url, outlet, storage_link, order
            )
            # Write Title to Local Log File
            open_temp.write(str(title) + "\n")
            print("Hill Article Added to DB")
        else:
            print("Hill Article Already in DB")
    except:
        print("Hill Article Error")
