import os
from uuid import uuid4
 
import requests
from bs4 import BeautifulSoup
from firebase_admin import storage
 
from utils.utilities import (formatDate, imgFolder, imgTitleFormat, initialise,
                            logFolder, pageSoup, pushToDB, titleFormat, similar)
 
ref_list = []
token = ""
log_file_path = "/home/bencapper/src/News-Aggregator/scripts/log/gbdone.log"
log_folder_path = "/home/bencapper/src/News-Aggregator/scripts/log/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
page_url = "https://www.gbnews.uk/tag/news"
img_path = "/home/bencapper/src/News/GB"
storage_path = "https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o"
db_path = "stories"
outlet = "www.GBNews.uk"
 
 
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
articles = soup.find_all("div", "content")[:-2]
 
order = 0
for article in articles:
    try:
        if order == 7:
            order = 0
        # get url
        a = article.select("a")
        url = str(a).split('href="')[1].split('" itemprop')[0]
    
        # get article page
        full_page = requests.get(url).content
        articleSoup = BeautifulSoup(full_page, features="lxml")
    
        # headline
        title = str(articleSoup.select("h1")).split('">')[1].split("</h1>")[0]
        title = titleFormat(title)
    
        # date
        date = str(articleSoup.select("time")).split("day ")[1].split(" -")[0].split(" ")
        date = formatDate(date)
    
    
        # Get image source (big image from article page)
        src = articleSoup.select("img")[1]
        src = (
            str(src)
            .replace("'", '"')
            .split(' src="')[1]
            .split('" title="')[0]
            .replace("&amp;", "&")
        )
    
        # replace title spaces for the image title
        img_title = imgTitleFormat(title)
    
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
                img.write(requests.get(src).content)
                blob = bucket.blob(f"GB/{img_title}")
                token = uuid4()
                metadata = {"firebaseStorageDownloadTokens": token}
                blob.upload_from_filename(f"{img_path}/{img_title}")
    
            storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/GB%2F{img_title}?alt=media&token={token}"
    
            pushToDB(
                db_path, title, date, src, img_title, url, outlet, storage_link, order
            )
            order = order + 1
            open_temp.write(str(title) + "\n")
            print("GB News Article Added to DB")
        else:
            print("GB News Article Already in DB")
    except:
        print("GB News Article Error")
 