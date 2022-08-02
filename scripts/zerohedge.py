import os
from uuid import uuid4
 
import requests
from firebase_admin import storage
 
from utils.utilities import (addYearAndFormat, formatDate, imgFolder, imgTitleFormat, initialise,
                            logFolder, pageSoup, pushToDB, titleFormat, similar)
 
ref_list = []
log_file_path = "/home/bencapper/src/News-Aggregator/scripts/log/zerodone.log"
log_folder_path = "/home/bencapper/src/News-Aggregator/scripts/log/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
page_url = "https://www.zerohedge.com/"
img_path = "/home/bencapper/src/News/Zerohedge"
storage_path = "https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o"
db_path = "stories"
outlet = "www.Zerohedge.com"
 
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
articles = soup.find_all("div","Article_nonStickyContainer__XQgbr")
order = 0
for article in articles:
    try:
        if order == 7:
            order = 0
        a = article.select("a")
        url = str(a).split('href="')[1].split('"')[0]
        url = f"https://{outlet}{url}"
        title = str(a).split('">')[1].split('</a>')[0]
        title = titleFormat(title)
        img_title = imgTitleFormat(title)
        img_src = str(a).split('<img alt=')[1].split('src="')[1].split('"/>')[0].replace("&amp;", "&")
        date = str(article).split('ArticleFooter_mobileTimeStamp__FdD_1">')[1].split("AT")[0]
        date = addYearAndFormat(date)
    
    
        # Get year and add to date
    
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
                blob = bucket.blob(f"Zerohedge/{img_title}")
                token = uuid4()
                metadata = {"firebaseStorageDownloadTokens": token}
                blob.upload_from_filename(f"{img_path}/{img_title}")
    
            storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/Zerohedge%2F{img_title}?alt=media&token={token}"
    
            pushToDB(
                db_path, title, date, img_src, img_title, url, outlet, storage_link, order
            )
            order = order + 1
            open_temp.write(str(title) + "\n")
            print("Zerohedge Article Added to DB")
        else:
            print("Zerohedge Article Already in DB")
    except:
        print("Zerohedge Article Error")
 
