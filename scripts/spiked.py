import os
from uuid import uuid4
import requests
from firebase_admin import storage
from utils.utilities import (formatDate, imgFolder, imgTitleFormat, initialise,
                           logFolder, pageSoup, pushToDB, titleFormat, similar,getHour)

ref_list = []
log_file_path = "/home/bencapper/src/News-Aggregator/scripts/log/spikedone.log"
log_folder_path = "/home/bencapper/src/News-Aggregator/scripts/log/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
page_url = "https://www.spiked-online.com/latest/"
img_path = "/home/bencapper/src/News/Spiked"
storage_path = "https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o"
db_path = "stories"
outlet = "www.Spiked-Online.com"

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
articles = soup.find_all("div", "post")
order = getHour()
for article in articles:
   try:
       url = article.find("a", "post-image block rel")
       title = article.select("h3")
       date = article.find("div", "post-date")
 
       link = str(url).split('href="')[1].split('">')[0]
       img_src = str(url).split('data-src="')[1].split('" ')[0]
       title = str(title).split('">')[1].split('</')[0].rstrip().lstrip()
       title = titleFormat(title)
       img_title = imgTitleFormat(title)
 
 
       date = str(date).split('">')[1].split('</')[0].split(' ')
       dates = list()
       day = date[0][:-2]
       if len(day) < 2:
           day = f"0{day}"
       month = date[1]
       year = date[2]
       dates.append(day)
       dates.append(month)
       dates.append(year)
       dates = formatDate(dates)
 
 
       if "<span" in title:
           pass
       else:
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
                   blob = bucket.blob(f"Spiked/{img_title}")
                   token = uuid4()
                   metadata = {"firebaseStorageDownloadTokens": token}
                   blob.upload_from_filename(f"{img_path}/{img_title}")
 
               storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/Spiked%2F{img_title}?alt=media&token={token}"
 
               pushToDB(
                   db_path, title, dates, img_src, img_title, link, outlet, storage_link, order
               )
               open_temp.write(str(title) + "\n")
               print("Spiked Article Added to DB")
           else:
               print("Spiked Article Already in DB")  
   except:
       print("Spiked Article Error")
