import os
from uuid import uuid4
import requests
from bs4 import BeautifulSoup
from firebase_admin import storage
from utils.utilities import (formatDate, imgFolder, imgTitleFormat, initialise,
                          logFolder, pageSoup, pushToDB, titleFormat, similar)
 
ref_list = []
log_file_path = "/home/bencapper/src/News-Aggregator/scripts/log/guarddone.log"
log_folder_path = "/home/bencapper/src/News-Aggregator/scripts/log/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
page_url = "https://www.theguardian.com/world"
img_path = "/home/bencapper/src/News/Guardian"
storage_path = "https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o"
db_path = "stories"
outlet = "www.TheGuardian.com"
 
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
articles = soup.find_all("div", "most-popular__link")
order = 0
for article in articles:
   try:
       if order == 7:
          order = 0
       link = str(article).split('href="')[1].split('">')[0]
       full_page = requests.get(link).content
       articleSoup = BeautifulSoup(full_page, features="lxml")
       title = articleSoup.select('h1')
       title = str(title).split('">')[1].split('</')[0]
       title = titleFormat(title)
       img_title = imgTitleFormat(title)
       date = articleSoup.find('span', 'dcr-10i63lj')
       date = str(date).split('">')[1].split('</')[0].split(' ')[1:-2]
       date = formatDate(date)
       img_src = "guard.png"
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
            storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/Guardian%2Fguard.png?alt=media&token=442ad837-685b-4ac5-a089-a14c88c5dddc"
            pushToDB(
               db_path, title, date, img_src, img_title, link, outlet, storage_link, order
            )
            order = order + 1
            open_temp.write(str(title) + "\n")
            print("Guardian Article Added to DB")
       else:
            print("Guardian Article Already in DB")
   except:
        print("Guardian Article Error")