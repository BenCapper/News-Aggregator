import os
from uuid import uuid4
import requests
from firebase_admin import storage
from utils.utilities import (
    formatDate,
    imgTitleFormat,
    initialise,
    jsonFolder,
    appendJson,
    todayDate,
    imgFolder,
    logFolder,
    pageSoup,
    pushToDB,
    titleFormat,
    similar,
    getHour,
)


td = todayDate()
# Set Global Variables
ref_list = []
log_file_path = "/home/bencapper/src/News-Aggregator/scripts/log/dmail.log"
log_folder_path = "/home/bencapper/src/News-Aggregator/scripts/log/"
json_dump_path = "/home/bencapper/src/News-Aggregator/scripts/json/dmail.json"
json_folder_path = "/home/bencapper/src/News-Aggregator/scripts/json/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
page_url = "https://www.dailymail.co.uk/news/index.html"
img_path = f"/home/bencapper/src/News/DMail/{td}"
storage_path = ("https://firebasestorage.googleapis.com/"
                "v0/b/news-a3e22.appspot.com/o")
db_path = "stories"
outlet = "www.DailyMail.co.uk"
imagepre = "https://i.dailymail.co.uk/"


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
articles = soup.find_all("div", "article article-small")


# Cycle through List just Gathered
# And Save to DB or Pass due to Lack
# of Information Available
for article in articles:
    try:
        url = str(article).split('data-url="')[1].split('" href')[0]
        image = str(article).split('data-article-image="/')
        [1].split('" data')[0]
        img_src = f"{imagepre}{image}"
        title = str(article).split('data-headline="')[1].split('" data')[0]
        title = titleFormat(title)
        img_title = imgTitleFormat(title)
        date = str(article).split('data-article-image="/')
        [1].split('" data')[0]
        date = date[3:13].split("/")
        dates = list()
        dates.append(date[2])
        dates.append(date[1])
        dates.append(date[0])
        date = formatDate(dates)
        bucket = storage.bucket()
        token = ""
        check = False
        for ref in ref_list:
            similarity = similar(ref, title)
            if similarity > 0.8 or "<span" in title:
                check = True
                break
        if title not in ref_list and check is False:
            ref_list.append(title)
            open_temp = open(log_file_path, "a")
            with open(f"{img_path}/{img_title}", "wb") as img:
                img.write(requests.get(img_src).content)
                blob = bucket.blob(f"DMail/{td}/{img_title}")
                token = uuid4()
                metadata = {"firebaseStorageDownloadTokens": token}
                blob.upload_from_filename(f"{img_path}/{img_title}")
            # Get Link to the Stored Image
            storage_link = (f"https://firebasestorage.googleapis.com/"
                            f"v0/b/news-a3e22.appspot.com/o/DMail%2F"
                            f"{td}%2F{img_title}?alt=media&token={token}")
            data = {
                "title": title,
                "date": date,
                "link": url,
                "outlet": outlet,
                "storage_link": storage_link,
                "order": order,
            }
            open_json = open(json_dump_path, "r")
            read_json = open_json.read()
            appendJson(json_dump_path, data)
            # Push the Gathered Data to DB
            # Using Utils method
            pushToDB(db_path, title, date, url, outlet, storage_link, order)
            # Write Title to Local Log File
            open_temp.write(str(title) + "\n")
            print("DailyMail Article Added to DB")
        else:
            print("DailyMail Article Already in DB")
    except:
        print("DailyMail Article Error")
