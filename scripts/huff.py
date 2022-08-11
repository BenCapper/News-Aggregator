import os
from uuid import uuid4
import requests
from bs4 import BeautifulSoup
from firebase_admin import storage
from utils.utilities import (formatDate, imgFolder, imgTitleFormat, initialise,
                         logFolder, pageSoup, pushToDB, titleFormat, similar,getHour)
                        
ref_list = []
log_file_path = "/home/bencapper/src/News-Aggregator/scripts/log/huffdone.log"
log_folder_path = "/home/bencapper/src/News-Aggregator/scripts/log/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
page_url = "https://www.huffpost.com/news/"
img_path = "/home/bencapper/src/News/Huff"
storage_path = "https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o"
db_path = "stories"
outlet = "www.HuffPost.com"

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
articles = soup.find_all("div", "card")
order = getHour()
for article in articles:
   try:
       link = article.select("a")
       link = str(link).split(' href="')[1].split('" ')[0]
       img_src = article.select("picture")
       img_src = str(img_src).split(' src="')[1].split('" ')[0]
       title = article.select("h3")
       title = str(title).split('">')[1].split('</')[0]
       title = titleFormat(title)
       img_title = imgTitleFormat(title)
       full_page = requests.get(link).content
       articleSoup = BeautifulSoup(full_page, features="lxml")
       dates = articleSoup.select("time")
       dates = str(dates).split(' datetime="')[1].split('T')[0].split('-')
       date = list()
       date.append(dates[2])
       date.append(dates[1])
       date.append(dates[0])
       date = formatDate(date)
 
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
               blob = bucket.blob(f"Huff/{img_title}")
               token = uuid4()
               metadata = {"firebaseStorageDownloadTokens": token}
               blob.upload_from_filename(f"{img_path}/{img_title}")
 
           storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/Huff%2F{img_title}?alt=media&token={token}"
 
           pushToDB(
               db_path, title, date, img_src, img_title, link, outlet, storage_link, order
           )
           open_temp.write(str(title) + "\n")
           print("Huffington Post Article Added to DB")
       else:
           print("Huffington Post Article Already in DB") 
   except:
       print("Huffington Post Article Error")
