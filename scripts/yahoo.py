import os
 
import requests
from bs4 import BeautifulSoup
from firebase_admin import storage
 
from utils.utilities import (formatDate, imgFolder, initialise,
                            logFolder, pageSoup, pushToDB, titleFormat, similar,getHour)
 

ref_list = []
log_file_path = "/home/bencapper/src/News-Aggregator/scripts/log/yahoodone.log"
log_folder_path = "/home/bencapper/src/News-Aggregator/scripts/log/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
page_url = "https://news.yahoo.com"
img_path = "/home/bencapper/src/News/Yahoo"
storage_path = "https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o"
db_path = "stories"
outlet = "news.Yahoo.com"
 
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
articles = soup.find_all("div", "Cf")
order = getHour()
for article in articles:
   try:
      a = article.select("a")
      link = str(a).split('href="')[1].split('">')[0]
      link = f"{page_url}/{link}"
      full_page = requests.get(link).content
      articleSoup = BeautifulSoup(full_page, features="lxml")
      title = articleSoup.select("h1")
      title = str(title).split('">')[1].split('</')[0]
      title = titleFormat(title)
      img_title = ""
      date = list()
      dates = articleSoup.select("time")
      monthDay = str(dates).split('">')[1].split(',')[0].split(' ')
      year = str(dates).split('">')[1].split(',')[1].rstrip().lstrip()
      date.append(monthDay[1])
      date.append(monthDay[0])
      date.append(year)
      date = formatDate(date)

      img_src = ""
      check = False
      for ref in ref_list:
         similarity = similar(ref,title)
         if similarity > .8:
            check = True
            break
      if title not in ref_list and check is False:
         ref_list.append(title)
         open_temp = open(log_file_path, "a")
         storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/Yahoo%2Fyahoo-news.png?alt=media&token=6ea70d79-fbaf-442d-b952-babb2fb3f6d7"


         pushToDB(
              db_path, title, date, img_src, img_title, link, outlet, storage_link, order
          )
         open_temp.write(str(title) + "\n")
         print("Yahoo Article Added to the database")
      else:
         print("Yahoo Article Already in the database")
   except:
      print("Yahoo Article Error")