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
log_file_path = "/home/bencapper/src/News-Aggregator/scripts/log/politicodone.log"
log_folder_path = "/home/bencapper/src/News-Aggregator/scripts/log/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
page_url = "https://www.politico.com"
img_path = "/home/bencapper/src/News/Politico"
storage_path = "https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o"
db_path = "stories"
outlet = "www.politico.com"
url = "https://www.politico.com"


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
articles = soup.find_all("h1", "headline is-standard-typeface")

for article in articles:
   link = str(article).split('href="')[1].split('" tar')[0]
 
   full_page = requests.get(link).content
   articleSoup = BeautifulSoup(full_page, features="lxml")
   title = articleSoup.find("h2", "headline")

   if title is None:
       pass
   else:
       title = str(title).split('">')[1].split('</')[0]
       title = title.rstrip().lstrip()
       title = titleFormat(title)
       img_title = imgTitleFormat(title)
       dates = articleSoup.find('p', 'story-meta__timestamp')
       dates = str(dates).split('">')[2].split(' ')[0].split('/')
       date = list()
       date.append(dates[1])
       date.append(dates[0])
       date.append(dates[2])
       date = formatDate(date)
       img_src = articleSoup.find("div", "fig-graphic")

       if img_src is None:
           pass
       else:
           img_src = str(img_src).split('srcset="')[1].split(' ')[0]
 
           bucket = storage.bucket()
           token = ""
 
           if title not in ref_list:
               ref_list.append(title)
               open_temp = open(log_file_path, "a")
               # use images from folder to upload to storage
               with open(f"{img_path}/{img_title}", "wb") as img:
                   img.write(requests.get(img_src).content)
                   blob = bucket.blob(f"Politico/{img_title}")
                   token = uuid4()
                   metadata = {"firebaseStorageDownloadTokens": token}
                   blob.upload_from_filename(f"{img_path}/{img_title}")
 
               storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/Politico%2F{img_title}?alt=media&token={token}"
 
               pushToDB(
                   db_path, title, date, img_src, img_title, link, outlet, storage_link
               )
 
               open_temp.write(str(title) + "\n")
               print("Politico story added to the database")
           else:
               print("Already in the database")
