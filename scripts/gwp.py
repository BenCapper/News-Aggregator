import os
from uuid import uuid4
 
import requests
from bs4 import BeautifulSoup
from firebase_admin import storage
 
from utils.utilities import (formatDate, imgFolder, imgTitleFormat, initialise,
                            logFolder, pageSoup, pushToDB, titleFormat)
 
ref_list = []
log_file_path = "/home/bencapper/src/News-Aggregator/scripts/log/GWPdone.log"
log_folder_path = "/home/bencapper/src/News-Aggregator/scripts/log/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
page_url = "https://www.thegatewaypundit.com/"
img_path = "/home/bencapper/src/News/GWP"
storage_path = "https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o"
db_path = "stories"
outlet = "www.TheGatewayPundit.com"
 
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
articles = soup.find_all("div","tgp-post")

for article in articles:
   a = article.select("a")
   img_src = str(a).split(' data-src="')[1].split('" data-src')[0].split('" height')[0]
   url = str(article).split(' href="')[1].split('">')[0]

   full_page = requests.get(url).content
   articleSoup = BeautifulSoup(full_page, features="lxml")
   title = str(articleSoup.select("h1")).split('">')[1].split('</')[0]
   title = titleFormat(title)
   img_title = imgTitleFormat(title)
   date = articleSoup.find_all("div", "entry-meta-text")
   date = str(date).split("Published ")[1].split(" at")[0]
   date = date.replace(",", " ").replace("  ", " ").split(" ")
   day = date[1]
   month = date[0]
   year = date[2]
   date = [day,month,year]
   date = formatDate(date)

   bucket = storage.bucket()
   token = ""
 
   if title not in ref_list:
      ref_list.append(title)
      open_temp = open(log_file_path, "a")
      # use images from folder to upload to storage
      with open(f"{img_path}/{img_title}", "wb") as img:
          img.write(requests.get(img_src).content)
          blob = bucket.blob(f"GB/{img_title}")
          token = uuid4()
          metadata = {"firebaseStorageDownloadTokens": token}
          blob.upload_from_filename(f"{img_path}/{img_title}")

      storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/GB%2F{img_title}?alt=media&token={token}"

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
      print("GatewayPundit story added to the database")
   else:
      print("Already in the database")
 


