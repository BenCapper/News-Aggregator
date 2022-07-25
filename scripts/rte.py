import os
from uuid import uuid4
 
import requests
import datetime
from bs4 import BeautifulSoup
from firebase_admin import storage
 
from utils.utilities import (formatDate, imgFolder, imgTitleFormat, initialise,
                            logFolder, pageSoup, pushToDB, titleFormat)
 
ref_list = []
token = ""
log_file_path = "/home/bencapper/src/News-Aggregator/scripts/log/rtedone.log"
log_folder_path = "/home/bencapper/src/News-Aggregator/scripts/log/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
page_url = "https://www.rte.ie/news/"
img_path = "/home/bencapper/src/News/Rte"
storage_path = "https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o"
db_path = "stories"
outlet = "www.Rte.ie"
prefix = "https://www.rte.ie"
 
 
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
articles = soup.find_all("div", "article-meta")
 
for article in articles:
    empty = False
    url = article.select("a")
    if url != []:
        url = str(url).split(' href="')[1].split('">')[0]
        url = f"{prefix}{url}"
        full_page = requests.get(url).content
        articleSoup = BeautifulSoup(full_page, features="lxml")
        title = articleSoup.select("h1")
        title = str(title).split('">')[1].split('</')[0]
        title = title.rstrip().lstrip()
        title = titleFormat(title)
        img_title = imgTitleFormat(title)
        date = articleSoup.find("span", "modified-date")
        date = str(date).split(', ')[1].split(' <str')[0].split(' ')
        date = formatDate(date)
        img_src = articleSoup.find("img", "changeable")
        if img_src is None:
            empty = True
            img_src = ""
        else:
            img_src = str(img_src).split(' data-src="')[1].split('" item')[0]

        bucket = storage.bucket()
        token = ""
        if "<span" in title:
            print("passed")
            pass
        else:
            if title not in ref_list:
               ref_list.append(title)
               open_temp = open(log_file_path, "a")

               if empty is True:
                   img_title = "rte.jpg"
                   storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/Rte%2Frte.png?alt=media&token=850879db-27dd-4e87-9163-66f9088465b0"
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
                   print("RTE (no image) story added to the database")
               else:
                  with open(f"{img_path}/{img_title}", "wb") as img:
                    img.write(requests.get(img_src).content)
                    blob = bucket.blob(f"Rte/{img_title}")
                    token = uuid4()
                    metadata = {"firebaseStorageDownloadTokens": token}
                    blob.upload_from_filename(f"{img_path}/{img_title}")

                    storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/Rte%2F{img_title}?alt=media&token={token}"

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
                    print("RTE story added to the database")
            else:
                print("Already in the database")


