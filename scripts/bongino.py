import os
 
import datetime
from firebase_admin import storage
 
from utils.utilities import (imgFolder, imgTitleFormat, initialise,
                            logFolder, pageSoup, pushToDbNoImg, titleFormat, similar)
 
ref_list = []
log_file_path = "/home/bencapper/src/News-Aggregator/scripts/log/bonginodone.log"
log_folder_path = "/home/bencapper/src/News-Aggregator/scripts/log/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
page_url = "https://www.bonginoreport.com/"
img_path = "/home/bencapper/src/News/Bongino"
storage_path = "https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o"
db_path = "stories"
outlet = "www.BonginoReport.com"
 
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
articles = soup.find_all("li")
articles = articles[:-8]
cap_1 = articles[:8]
cult_2 = articles[8:16]
eco_3 = articles[16:24]
swamp_4 = articles[24:31]
glob_5 = articles[31:38]
opin_6 = articles[38:45]
ent_7 = articles[45:51]
sport_8 = articles[51:57]
health_9 = articles[57:63]

for article in articles:
   try:
      a = article.select("a")
      url = str(a).split(' href="')[1].split('" target')[0]
      title = str(a).split('">')[1].split('</a')[0]
      title = titleFormat(title)
      img_title = imgTitleFormat(title)
      date = datetime.datetime.now().strftime("%m-%d-%y")
      date = f"Found on: {date}" 
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
         storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/Bongino%2Fbongino.jpg?alt=media&token=c347bc71-7d59-45bf-b1eb-a1f98ab0965f"

         pushToDbNoImg(
              db_path,
              title,
              date,
              url,
              outlet,
              storage_link,
          )

         open_temp.write(str(title) + "\n")
         print("Bongino Article Added to DB")
      else:
         print("Bongino Article Already in DB")
   except:
      print("Bongino Article Error")
