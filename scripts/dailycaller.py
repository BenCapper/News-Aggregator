import os
from uuid import uuid4
 
import requests
from firebase_admin import storage
 
from utils.utilities import (addYearAndFormat, formatDate, imgFolder, imgTitleFormat, initialise,
                            logFolder, pageSoup, pushToDB, titleFormat)
 
ref_list = []
log_file_path = "/home/bencapper/src/News-Aggregator/scripts/log/dailycallerdone.log"
log_folder_path = "/home/bencapper/src/News-Aggregator/scripts/log/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
page_url = "https://dailycaller.com/section/world/"
img_path = "/home/bencapper/src/News/DailyCaller"
storage_path = "https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o"
db_path = "stories"
outlet = "www.Dailycaller.com"
 
logFolder(log_folder_path)
 
if os.path.exists(log_file_path):
   open_temp = open(log_file_path, "r")
   read_temp = open_temp.read()
   ref_list = read_temp.splitlines()
else:
   os.mknod(log_file_path)
 
imgFolder(img_path)
initialise(json_path, db_url, bucket)
 
 
soup = pageSoup(page_url)
articles = soup.find_all("article", "relative")

for article in articles:
    a = article.select("a")
    url = str(a).split('href="')[1].split('/"')[0]
    dates = url[1:11].split('/')
    day = dates[2]
    month = dates[1]
    year = dates[0][2:]
    date = f"{month}-{day}-{year}"
    url = f"https://www.dailycaller.com{url}"
    title = str(a).split('title="')[1].split('">')[0].split('Link to ')[1]
    title = titleFormat(title)

    img = article.select("img")
    img_src = str(img).split('data-src="')[1].split('" data')[0]
    img_title = imgTitleFormat(title)

    bucket = storage.bucket()
    token = ""
 
    if title not in ref_list:
        ref_list.append(title)
        open_temp = open(log_file_path, "a")
        # use images from folder to upload to storage
        with open(f"{img_path}/{img_title}", "wb") as img:
            img.write(requests.get(img_src).content)
            blob = bucket.blob(f"DailyCaller/{img_title}")
            token = uuid4()
            metadata = {"firebaseStorageDownloadTokens": token}
            blob.upload_from_filename(f"{img_path}/{img_title}")
  
        storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/DailyCaller%2F{img_title}?alt=media&token={token}"
  
        pushToDB(
            db_path, title, date, "", "", img_src, img_title, url, outlet, storage_link
        )
  
        open_temp.write(str(title) + "\n")
        print("Dailycaller story added to the database")
    else:
        print("Already in the database")
 