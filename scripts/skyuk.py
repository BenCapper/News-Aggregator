import os
from uuid import uuid4
 
import requests
import datetime
from bs4 import BeautifulSoup
from firebase_admin import storage
 
from utils.utilities import (formatDate, imgFolder, imgTitleFormat, initialise,
                            logFolder, pageSoup, pushToDB, titleFormat, similar)
 
ref_list = []
token = ""
log_file_path = "/home/bencapper/src/News-Aggregator/scripts/log/skyukdone.log"
log_folder_path = "/home/bencapper/src/News-Aggregator/scripts/log/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
page_url = "https://news.sky.com/uk"
img_path = "/home/bencapper/src/News/Skyuk"
storage_path = "https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o"
db_path = "stories"
outlet = "news.Sky.com"
url = "https://news.sky.com"
 
 
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
articles = soup.find_all("h3", "sdc-site-tile__headline")
 
for article in articles:
    try:
        href = str(article).split('href="')[1].split('">')[0]
        link = f"{url}{href}"

        full_page = requests.get(link).content
        articleSoup = BeautifulSoup(full_page, features="lxml")

        title = articleSoup.find("span", "sdc-article-header__long-title")
        title = str(title).split('">')[1].split("</")[0]
        title = titleFormat(title)
        img_title = imgTitleFormat(title)

        date = articleSoup.find("p", "sdc-article-date__date-time")
        if date is None:
            pass
        else:
            date = str(date).split('day ')[1].split(":")[0][:-3].split(" ")
            date = formatDate(date)
            img_src = articleSoup.find("img", "sdc-article-image__item")
            if img_src is None:
                pass
            else:
                img_src = str(img_src).split('src="')[1].split('" srcset')[0]
                bucket = storage.bucket()
                token = ""
                check = False
                for ref in ref_list:
                   similarity = similar(ref,title)
                   if similarity > .8:
                      check = True
                      break
                if title not in ref_list and check is False:
                    ref_list.append(title)
                    open_temp = open(log_file_path, "a")
                    # use images from folder to upload to storage
                    with open(f"{img_path}/{img_title}", "wb") as img:
                        img.write(requests.get(img_src).content)
                        blob = bucket.blob(f"Skyuk/{img_title}")
                        token = uuid4()
                        metadata = {"firebaseStorageDownloadTokens": token}
                        blob.upload_from_filename(f"{img_path}/{img_title}")

                    storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/Skyuk%2F{img_title}?alt=media&token={token}"

                    pushToDB(
                        db_path, title, date, img_src, img_title, link, outlet, storage_link
                    )

                    open_temp.write(str(title) + "\n")
                    print("Sky UK Article Added to DB")
                else:
                    print("Sky UK Article Already in DB")
    except:
        print("Sky UK Article Error")