import os
from uuid import uuid4
 
import requests
from bs4 import BeautifulSoup
from firebase_admin import storage
 
from utils.utilities import (formatDate, imgFolder, imgTitleFormat, initialise,
                            logFolder, pageSoup, pushToDB, titleFormat, similar,getHour)
 
ref_list = []
log_file_path = "/home/bencapper/src/News-Aggregator/scripts/log/globaldone.log"
log_folder_path = "/home/bencapper/src/News-Aggregator/scripts/log/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
page_url = "https://globalnews.ca/canada"
img_path = "/home/bencapper/src/News/Global"
storage_path = "https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o"
db_path = "stories"
outlet = "www.GlobalNews.ca"
 
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
articles = soup.find_all("li", "c-posts__item c-posts__loadmore")
order = getHour()
for article in articles:
    try:
        link = str(article).split(' href="')[1].split('">')[0]
        title = str(article).split('data-title="">')[1].split('</span')[0]
        title = titleFormat(title)
        img_title = imgTitleFormat(title)
        img_src = str(article).split('data-src="')[1].split('" ')[0]
        full_page = requests.get(link).content
        articleSoup = BeautifulSoup(full_page, features="lxml")
        dates = articleSoup.find("div", "c-byline__date c-byline__date--pubDate")
        monthDay = str(dates).split('<span>')[1].split('ed ')[1].split(',')[0].split(' ')
        year = str(dates).split('<span>')[1].split('ed ')[1].split(',')[1].split(' ')[1]
        date = list()
        date.append(monthDay[1])
        date.append(monthDay[0])
        date.append(year)
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
                blob = bucket.blob(f"Global/{img_title}")
                token = uuid4()
                metadata = {"firebaseStorageDownloadTokens": token}
                blob.upload_from_filename(f"{img_path}/{img_title}")
    
            storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/Global%2F{img_title}?alt=media&token={token}"
    
            pushToDB(
                db_path, title, date, img_src, img_title, link, outlet, storage_link, order
            )
            open_temp.write(str(title) + "\n")
            print("Global News Article Added to DB")
        else:
            print("Global News Article Already in DB")
    except:
        print("Global News Article Error")
    
