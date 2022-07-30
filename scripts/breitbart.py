import os
from uuid import uuid4

import time
import requests
from bs4 import BeautifulSoup
from firebase_admin import storage
 
from utils.utilities import (formatDate, imgFolder, imgTitleFormat, initialise,
                            logFolder, pageSoup, pushToDB, titleFormat)
 
ref_list = []
token = ""
log_file_path = "/home/bencapper/src/News-Aggregator/scripts/log/breitbartdone.log"
log_folder_path = "/home/bencapper/src/News-Aggregator/scripts/log/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
page_url = "https://www.breitbart.com"
img_path = "/home/bencapper/src/News/Breitbart"
storage_path = "https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o"
db_path = "stories"
outlet = "www.Breitbart.com"
storage_link = ""
img_src = ""
 
 
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
articles = soup.find_all("article")
for article in articles:
    empty = False
    time.sleep(5)
    url = str(article).split(' href="')[1].split('" title="')[0].split('">')[0]
    full_page = requests.get(url).content
    articleSoup = BeautifulSoup(full_page, features="lxml")
    date = str(articleSoup).split('datetime="')[1].split('T')[0].split('-')
    day = date[2]
    month = date[1]
    year = date[0][2:]
    date = f"{month}-{day}-{year}"
    title = articleSoup.select("h1")
    title = str(title).split('h1>')[1].split('</')[0]
    title = titleFormat(title)
    img_title = imgTitleFormat(title)
    try:
        img_src = articleSoup.select("figure")
        img_src = str(img_src).split('src="')[1].split('" title')[0]
    except:
        img_src = ""
        empty = True
        print("Empty Article")

    bucket = storage.bucket()
    token = ""
  
    if title not in ref_list:
       ref_list.append(title)
       open_temp = open(log_file_path, "a")

       if empty is True:
           img_title = "bb.jpg"
           storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/Breitbart%2Fbb.jpg?alt=media&token=14e1f346-6cdd-4ded-bbba-4dd6e9a42d1f"
           pushToDB(
              db_path,
              title,
              date,
              img_src,
              img_title,
              url,
              outlet,
              storage_link,
            )

           open_temp.write(str(title) + "\n")
           print("Breitbart (no image) story added to the database")
       else:
          with open(f"{img_path}/{img_title}", "wb") as img:
            img.write(requests.get(img_src).content)
            blob = bucket.blob(f"Breitbart/{img_title}")
            token = uuid4()
            metadata = {"firebaseStorageDownloadTokens": token}
            blob.upload_from_filename(f"{img_path}/{img_title}")

            storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/Breitbart%2F{img_title}?alt=media&token={token}"

            pushToDB(
                db_path,
                title,
                date,
                img_src,
                img_title,
                url,
                outlet,
                storage_link,
            )

            open_temp.write(str(title) + "\n")
            print("Breitbart story added to the database")
    else:
        print("Already in the database")


 
     