import os
from uuid import uuid4
import requests
from bs4 import BeautifulSoup
from firebase_admin import storage
from utils.utilities import (formatDate, imgFolder, imgTitleFormat, initialise,
                           logFolder, pageSoup, pushToDB, titleFormat)


ref_list = []
log_file_path = "/home/bencapper/src/News-Aggregator/scripts/log/blazedone.log"
log_folder_path = "/home/bencapper/src/News-Aggregator/scripts/log/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
page_url = "https://theblaze.com/"
img_path = "/home/bencapper/src/News/Blaze"
storage_path = "https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o"
db_path = "stories"
outlet = "www.TheBlaze.com"

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
articles = soup.find_all("div", "posts-wrapper clearfix")
#articles = articles.find_all("h1", "widget__headline h1")
for article in articles:
   a = article.find_all("a")
   urls = list()
   for art in a:
       url = str(art).split(' href="')[1].split('">')[0].split('" target')[0]
       if "bit.ly" in url:
           pass
       elif "podcast" in url:
           pass
       elif "/u/" in url:
           pass
       elif "/shows/" in url:
           pass
       elif url in urls:
           pass
       else:
           urls.append(url)
 
   for link in urls:
       full_page = requests.get(link).content
       articleSoup = BeautifulSoup(full_page, features="lxml")
       d = articleSoup.find("span","post-date")
       if d is None:
           pass
       else:
           dates = str(d).split('">')[1].split('</')[0].replace(',',' ').replace('  ', ' ').split(' ')
           date = list()
           date.append(dates[1])
           date.append(dates[0])
           date.append(dates[2])
           date = formatDate(date)
 
           img_src = articleSoup.find("div", "widget__image crop-16x9")
           title = str(img_src).split('label="')[1].split('" class')[0]
           title = titleFormat(title)
           img_title = imgTitleFormat(title)
           img_src = str(img_src).split("url('")[1].split("')")[0]
 
           bucket = storage.bucket()
           token = ""
           if title not in ref_list:
               ref_list.append(title)
               open_temp = open(log_file_path, "a")
               # use images from folder to upload to storage
               with open(f"{img_path}/{img_title}", "wb") as img:
                   img.write(requests.get(img_src).content)
                   blob = bucket.blob(f"Blaze/{img_title}")
                   token = uuid4()
                   metadata = {"firebaseStorageDownloadTokens": token}
                   blob.upload_from_filename(f"{img_path}/{img_title}")
 
               storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/Blaze%2F{img_title}?alt=media&token={token}"
 
               pushToDB(
                   db_path, title, date, img_src, img_title, link, outlet, storage_link
               )
 
               open_temp.write(str(title) + "\n")
               print("Blaze story added to the database")
           else:
               print("Already in the database")
