import os
import re
from uuid import uuid4
 
import requests
from firebase_admin import storage
 
from utils.utilities import (imgFolder, imgTitleFormat, initialise, logFolder,
                            pageSoup, pushToDB, titleFormat)
 
ref_list = []
token = ""
log_folder_path = "/home/bencapper/src/News-Aggregator/scripts/log/"
log_file_path = "/home/bencapper/src/News-Aggregator/scripts/log/timdone.log"
json_path = "/home/bencapper/src/News/news.json"
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
page_url = "https://timcast.com/news/"
img_path = "/home/bencapper/src/News/Timcast"
storage_path = "https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o"
db_path = "stories"
outlet = "www.Timcast.com"
 
 
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
articles = soup.find_all("div", "article-block")
 
 
for article in articles:
   # Get article date, format day if < 10
   found_date = ""
   match_pattern1 = "[0-9]{2}.[0-9]{1}.[0-9]{2}"
   match_pattern2 = "[0-9]{2}.[0-9]{2}.[0-9]{2}"
   date = article.find("div", "summary")
   date = str(date)
   found_date = re.findall(match_pattern1, date)
   found_date = str(found_date).replace(".", "-")
   found_date = found_date.lstrip("['").rstrip("']")
   if found_date == "":
       found_date = re.findall(match_pattern2, date)
       found_date = str(found_date).replace(".", "-").lstrip("['").rstrip("']")
   else:
       days = found_date.split("-")
       day = f"0{days[1]}"
       found_date = f"{days[0]}-{day}-{days[2]}"
 
   # Get link to article
   link = article.find("a")
   link = str(link)[23:]
   link = link.split('"')
   link = link[0]
 
   # Isolate author name
   auth = article.find("span", "auth")
   author = str(auth).split("|")[1].split("</span>")[0].strip()
 
   # Some article blocks dont have previews
   article_preview = article.find("p")
   preview = str(article_preview)
   lines = preview.split("\t")
   first_line = ""
   for line in lines:
       if len(line) > 10:
           first_line = line
 
   # Use img tag to get the Headline and src location
   image = article.find("img")
   image = str(image)
   img_list = image.split(" src=")
   title = img_list[0][10:-1]
   img_link = img_list[1][1:-3]
 
   bucket = storage.bucket()
   img_title = imgTitleFormat(title)
   title = titleFormat(title)
 
   if title not in ref_list:
 
       # add to log list and open file
       ref_list.append(title)
       open_temp = open(log_file_path, "a")
 
 
       # Try except in case of no image error on source page
       try:
           # upload the image to storage
           with open(f"{img_path}/{img_title}", "wb") as img:
               img.write(requests.get(img_link).content)
               blob = bucket.blob(f"Timcast/{img_title}")
               token = uuid4()
               metadata = {"firebaseStorageDownloadTokens": token}
               blob.upload_from_filename(f"{img_path}/{img_title}")
           storage_link = f"{storage_path}/Timcast%2F{img_title}?alt=media&token={token}"
       except:
           print("Image error")
 
       # Push the article to firebase as json
       pushToDB(
           db_path,
           title,
           found_date,
           author,
           first_line,
           img_link,
           img_title,
           link,
           outlet,
           storage_link,
       )
 
       # write title to log file
 
       open_temp.write(str(title) + "\n")
       print("Timcast story added to the database")
   else:
       print("Already in the database")
 
