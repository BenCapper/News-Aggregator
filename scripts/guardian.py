import os
from uuid import uuid4
import requests
from bs4 import BeautifulSoup
from firebase_admin import storage
from utils.utilities import (formatDate, imgFolder, imgTitleFormat, initialise, jsonFolder, dumpJson, appendJson,
                          logFolder, pageSoup, pushToDB, titleFormat, similar,getHour)
# Set Global Variables
ref_list = []
log_file_path = "/home/bencapper/src/News-Aggregator/scripts/log/guarddone.log"
log_folder_path = "/home/bencapper/src/News-Aggregator/scripts/log/"
json_dump_path = "/home/bencapper/src/News-Aggregator/scripts/json/guard.json"
json_folder_path = "/home/bencapper/src/News-Aggregator/scripts/json/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
page_url = "https://www.theguardian.com/world"
img_path = "/home/bencapper/src/News/Guardian"
storage_path = "https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o"
db_path = "stories"
outlet = "www.TheGuardian.com"

# Set Local Folders
logFolder(log_folder_path)
imgFolder(img_path)
jsonFolder(json_folder_path)

# Read from Existing Log
if os.path.exists(log_file_path):
 open_temp = open(log_file_path, "r")
 read_temp = open_temp.read()
 ref_list = read_temp.splitlines()
else:
 os.mknod(log_file_path)

# Initialize Firebase
initialise(json_path, db_url, bucket)

# Order Based on Current Hour
# Reversed in Android Studio
# to Make Sure The Most Recent
# Articles are Shown First
order = getHour()

# Gather News Page HTML
# Find the Div Containing
# Targeted Article Links
soup = pageSoup(page_url)
articles = soup.find_all("div", "most-popular__link")

# Cycle through List just Gathered
# And Save to DB or Pass due to Lack
# of Information Available
for article in articles:

     # Catch all for a Litany of Possible Errors
     try:
         # Get Article Link
         link = str(article).split('href="')[1].split('">')[0]

         # Get Full Article Page
         full_page = requests.get(link).content
         articleSoup = BeautifulSoup(full_page, features="lxml")

         # Gather Title / Img Title
         title = articleSoup.select('h1')
         title = str(title).split('">')[1].split('</')[0]
         title = titleFormat(title)
         img_title = imgTitleFormat(title)

         # Gather / Format Date
         date = articleSoup.find('span', 'dcr-10i63lj')
         date = str(date).split('">')[1].split('</')[0].split(' ')[1:-2]
         date = formatDate(date)

         # Images Protected - Use Default
         img_src = "guard.png"

         # Initialize Storage Variables
         bucket = storage.bucket()
         token = ""

         # Check if an Article which is 80%+
         # Similar to any Other in the Log
         check = False
         for ref in ref_list:
            similarity = similar(ref,title)
            if similarity > .8 or "<span" in title:
              check = True
              break

         # Only Continue if the Title is not
         # Already in the Log and is not too
         # Similar to Another
         if title not in ref_list and check is False:

              # Add the Title to the List
              # of Titles Already in the Log
              ref_list.append(title)
              open_temp = open(log_file_path, "a")
              storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/Guardian%2Fguard.png?alt=media&token=5193672c-3e9a-44db-8512-632f0a4fb0c5"
              data = {
                   "title": title,
                   "date": date,
                   "link": link,
                   "outlet": outlet,
                   "storage_link": storage_link,
                   "order": order
              }
              open_json = open(json_dump_path, "r")
              read_json = open_json.read()
              appendJson(json_dump_path,data)
              # Push the Gathered Data to DB
              # Using Utils method
              pushToDB(
                 db_path, title, date, link, outlet, storage_link, order
              )

              # Write Title to Local Log File
              open_temp.write(str(title) + "\n")
              print("Guardian Article Added to DB")
         else:
              print("Guardian Article Already in DB")
              
     # One of Many Possible Things
     # Went Wrong - 
     # Too Much of This is an Issue
     except:
          print("Guardian Article Error")