import os
from uuid import uuid4
 
import requests
from bs4 import BeautifulSoup
from firebase_admin import storage
 
from utils.utilities import (formatDate, imgFolder, imgTitleFormat, initialise,
                            logFolder, pageSoup, pushToDB, titleFormat, similar,getHour)
 
ref_list = []
log_file_path = "/home/bencapper/src/News-Aggregator/scripts/log/abcdone.log"
log_folder_path = "/home/bencapper/src/News-Aggregator/scripts/log/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
page_url = "https://abcnews.go.com/US"
img_path = "/home/bencapper/src/News/Abc"
storage_path = "https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o"
db_path = "stories"
outlet = "AbcNews.go.com"
 
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
articles = soup.find_all("section", "ContentRoll__Item")
order = getHour()
for article in articles:
   try:
      link = article.select("a")
      link = str(link).split('href="')[1].split('" ')[0]
      if "/video/" in link:
         pass
      else:
         dates = list()
         full_page = requests.get(link).content
         articleSoup = BeautifulSoup(full_page, features="lxml")
         img_src = articleSoup.find("div", "InlineImage fnIPT")
         if len(str(img_src)) < 500:
            pass
         else:
            img_src = str(img_src).split('src="')[1].split('"/>')[0]

            date = articleSoup.find('div', 'xTlfF')
            monthDay = str(date).split('">')[1].split(', ')[0].split(' ')
            year = str(date).split('">')[1].split(', ')[1]
            dates.append(monthDay[1])
            dates.append(monthDay[0])
            dates.append(year)
            date = formatDate(dates)
            title = articleSoup.select('h1')
            title = str(title).split('">')[1].split('</h')[0]
            title = titleFormat(title)
            img_title = imgTitleFormat(title)

            bucket = storage.bucket()
            token = ""

            check = False
            for ref in ref_list:
               similarity = similar(ref,title)
               if similarity > .8:
                  check = True
                  print('sigh')
                  break

            if title not in ref_list and check is False:
                ref_list.append(title)
                open_temp = open(log_file_path, "a")
                # use images from folder to upload to storage
                with open(f"{img_path}/{img_title}", "wb") as img:
                    img.write(requests.get(img_src).content)
                    blob = bucket.blob(f"Abc/{img_title}")
                    token = uuid4()
                    metadata = {"firebaseStorageDownloadTokens": token}
                    blob.upload_from_filename(f"{img_path}/{img_title}")

                storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/Abc%2F{img_title}?alt=media&token={token}"

                pushToDB(
                    db_path, title, date, img_src, img_title, link, outlet, storage_link, order
                )
                open_temp.write(str(title) + "\n")
                print("ABC Article Added to DB")
            else:
                print("ABC Article Already in DB")
   except:
      print("Abc Article Error")