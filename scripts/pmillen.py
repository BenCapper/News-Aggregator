import os
from uuid import uuid4
 
import requests
from bs4 import BeautifulSoup
from firebase_admin import storage
 
from utils.utilities import (formatDate, imgFolder, imgTitleFormat, initialise,
                            logFolder, pageSoup, pushToDB, titleFormat)
 
ref_list = []
token = ""
log_file_path = "/home/bencapper/src/News-Aggregator/scripts/log/pmill.log"
log_folder_path = "/home/bencapper/src/News-Aggregator/scripts/log/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
page_url = "https://thepostmillennial.com/category/news/"
img_path = "/home/bencapper/src/News/PMill"
storage_path = "https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o"
db_path = "stories"
outlet = "www.ThePostMillennial.com"
 
 
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
articles = soup.find_all("div", "post-card-inner")
 
for article in articles:
    try:
        url = "https://www.ThePostMillennial.com"
        a = article.select("a")
        url = url + str(a).split('href="')[1].split('">')[0]
        h = article.select("h3")
        title = str(h).split('">')[1].split('</h')[0]
        title = titleFormat(title)
        img_title = imgTitleFormat(title)

        full_page = requests.get(url).content
        articleSoup = BeautifulSoup(full_page, features="lxml")
        content = articleSoup.find_all("section", "cover")
        img_src = str(content).split(' src="')[1].split('" ')[0]
        date = articleSoup.find("div", "article-info")
        date = date.select("time")
        date = str(date).split('">')[1].split(':')[0][:-2].replace(',', ' ').replace('  ', ' ')
        dates = date.split(' ')
        date = list()
        date.append(dates[1])
        date.append(dates[0])
        date.append(dates[2])
        date = formatDate(date)


        bucket = storage.bucket()
        token = ""
    
        if title not in ref_list:
            ref_list.append(title)
            open_temp = open(log_file_path, "a")
            # use images from folder to upload to storage
            with open(f"{img_path}/{img_title}", "wb") as img:
                img.write(requests.get(img_src).content)
                blob = bucket.blob(f"PMill/{img_title}")
                token = uuid4()
                metadata = {"firebaseStorageDownloadTokens": token}
                blob.upload_from_filename(f"{img_path}/{img_title}")
    
            storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/PMill%2F{img_title}?alt=media&token={token}"
    
            pushToDB(
                db_path, title, date, img_src, img_title, url, outlet, storage_link
            )
    
            open_temp.write(str(title) + "\n")
            print("Post Millennial Article Added to DB")
        else:
            print("Post Millennial Article Already in DB")
    except:
        print("Post Millennial Article Error")