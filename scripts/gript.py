import os
from uuid import uuid4
 
import requests
from firebase_admin import storage
 
from utils.utilities import (formatDate, imgFolder, imgTitleFormat, initialise,
                            logFolder, pageSoup, pushToDB, titleFormat)
 
ref_list = []
log_file_path = "/home/bencapper/src/News-Aggregator/scripts/log/griptdone.log"
log_folder_path = "/home/bencapper/src/News-Aggregator/scripts/log/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
page_url = "https://gript.ie/news/"
img_path = "/home/bencapper/src/News/Gript"
storage_path = "https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o"
db_path = "stories"
outlet = "www.Gript.ie"
 
 
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
 
articles = soup.find_all("archive-card")
articles = str(articles).split(" post-image")[1:-1]
for article in articles:
   date = (
       article.split(' post-date="')[1]
       .split('" post')[0]
       .replace(",", " ")
       .replace("  ", " ")
       .split()
   )
   month = date[0]
   day = date[1]
   if len(day) == 1:
       day = f"0{day}"
   year = date[2]
   date = [day, month, year]
 
   url = article.split(' post-url="')[1].split('/">')[0].split('" secondary')[0]
   img_link = article.split('="')[1].split('"')[0]
   title = article.split('" post-title="')[1].split('" post')[0]
 
   title = titleFormat(title)
   date = formatDate(date)
   img_title = imgTitleFormat(title)

   bucket = storage.bucket()
   token = ""

   if title not in ref_list:
      ref_list.append(title)
      open_temp = open(log_file_path, "a")   
      if img_link == "":
          img_title = "gript.jpg"
          storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/Gript%2Fgript.png?alt=media&token=b3bc4822-64f6-4ade-8910-e8a509ff759b"
          pushToDB(
             db_path,
             title,
             date,
             img_link,
             img_title,
             url,
             outlet,
             storage_link,
           )   
          open_temp.write(str(title) + "\n")
          print("Gript (no image) story added to the database")
      else:
         with open(f"{img_path}/{img_title}", "wb") as img:
           img.write(requests.get(img_link).content)
           blob = bucket.blob(f"GB/{img_title}")
           token = uuid4()
           metadata = {"firebaseStorageDownloadTokens": token}
           blob.upload_from_filename(f"{img_path}/{img_title}")   
           storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/GB%2F{img_title}?alt=media&token={token}"   
           pushToDB(
               db_path,
               title,
               date,
               img_link,
               img_title,
               url,
               outlet,
               storage_link,
           )   
           open_temp.write(str(title) + "\n")
           print("Gript story added to the database")
   else:
       print("Already in the database")


 
     


















 
   bucket = storage.bucket()
   token = ""
 
   if title not in ref_list:
       ref_list.append(title)
       open_temp = open(log_file_path, "a")
 
       with open(f"{img_path}/{img_title}", "wb") as img:
           img.write(requests.get(img_link).content)
           blob = bucket.blob(f"Gript/{img_title}")
           token = uuid4()
           metadata = {"firebaseStorageDownloadTokens": token}
           blob.upload_from_filename(f"{img_path}/{img_title}")
       storage_link = f"{storage_path}/Gript%2F{img_title}?alt=media&token={token}"
 
       # Push the article to firebase as json
       pushToDB(
           db_path, title, date, img_link, img_title, url, outlet, storage_link
       )
 
       # write title to log file
       open_temp.write(str(title) + "\n")
       print("Gript story added to the database")
   else:
       print("Already in the database")
