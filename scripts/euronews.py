import os
from uuid import uuid4
import requests
from bs4 import BeautifulSoup
from firebase_admin import storage
from utils.utilities import (addYearAndFormat, formatDate, imgFolder, imgTitleFormat, initialise,
                           logFolder, pageSoup, pushToDB, titleFormat)


ref_list = []
log_file_path = "/home/bencapper/src/News-Aggregator/scripts/log/euronewsdone.log"
log_folder_path = "/home/bencapper/src/News-Aggregator/scripts/log/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
page_url = "https://www.euronews.com/news/international"
img_path = "/home/bencapper/src/News/Euronews"
storage_path = "https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o"
db_path = "stories"
outlet = "www.Euronews.com"
prefix = "https://www.euronews.com"


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
articles = soup.find_all("div", "m-object__body")[22:-34]
 
for article in articles:
   h = article.select('h3')
   href = str(h).split('href="')[1].split('" ')[0]
   link = f"{prefix}{href}"
   full_page = requests.get(link).content
   articleSoup = BeautifulSoup(full_page, features="lxml")
   title = articleSoup.select("h1")
   title = str(title).split('">')[1].split('</')[0].rstrip().lstrip()
   title = titleFormat(title)
   img_title = imgTitleFormat(title)
   dates = articleSoup.select("time")
   dates = str(dates).split('datetime="')[1].split('">')[0].split('-')
   date = list()
   date.append(dates[2])
   date.append(dates[1])
   date.append(dates[0])
   date = formatDate(date)
   img = articleSoup.find("img", "js-poster-img c-article-media__img u-max-height-full u-width-full")
   bucket = storage.bucket()
   token = ""
   if img is None:
       img_title = "eu.png"
       img_src = ""
       storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/Euronews%2Feun.jpg?alt=media&token=1fc82731-59b7-41cb-b1e5-07f936a0322f"
       if title not in ref_list:
           ref_list.append(title)
           open_temp = open(log_file_path, "a")
           pushToDB(
                  db_path,
                  title,
                  date,
                  img_src,
                  img_title,
                  link,
                  outlet,
                  storage_link,
                )
 
           open_temp.write(str(title) + "\n")
           print("Euronews (No Image) story added to the database")
       else:
           print("Euronews (No Image) already in the database")
   else:
       img_src = str(img).split('src="')[1].split('" ')[0]
       if title not in ref_list:
           with open(f"{img_path}/{img_title}", "wb") as img:
               img.write(requests.get(img_src).content)
               blob = bucket.blob(f"Euronews/{img_title}")
               token = uuid4()
               metadata = {"firebaseStorageDownloadTokens": token}
               blob.upload_from_filename(f"{img_path}/{img_title}")
 
               storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/Euronews%2F{img_title}?alt=media&token={token}"
 
               pushToDB(
                   db_path,
                   title,
                   date,
                   img_src,
                   img_title,
                   link,
                   outlet,
                   storage_link,
               )
 
               open_temp.write(str(title) + "\n")
               print("Euronews story added to the database")
       else:
           print("Euronews story already in the database")
 