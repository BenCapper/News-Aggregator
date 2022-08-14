import os
from uuid import uuid4
 
import requests
from bs4 import BeautifulSoup
from firebase_admin import storage
 
from utils.utilities import (formatDate, imgFolder, imgTitleFormat, initialise, jsonFolder, dumpJson, appendJson,
                            logFolder, pageSoup, pushToDB, titleFormat, similar, getHour)

# Set Global Variables
ref_list = []
log_file_path = "/home/bencapper/src/News-Aggregator/scripts/log/abcdone.log"
log_folder_path = "/home/bencapper/src/News-Aggregator/scripts/log/"
json_dump_path = "/home/bencapper/src/News-Aggregator/scripts/json/abc.json"
json_folder_path = "/home/bencapper/src/News-Aggregator/scripts/json/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
page_url = "https://abcnews.go.com/US"
img_path = "/home/bencapper/src/News/Abc"
storage_path = "https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o"
db_path = "stories"
outlet = "AbcNews.go.com"
 
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
# Find the List of Articles
soup = pageSoup(page_url)
articles = soup.find_all("section", "ContentRoll__Item")

# Cycle through List just Gathered
# And Save to DB or Pass due to Lack
# of Information Available
for article in articles:

   # Catch all for a Litany of Possible Errors
   try:

      # Get Link to the article
      link = article.select("a")
      link = str(link).split('href="')[1].split('" ')[0]

      # Skip video content:
      # Many Differences in Articles
      if "/video/" in link:
         pass

      # Link to Article Gathered
      else:

         # Use the Link to get Article HTML
         full_page = requests.get(link).content
         articleSoup = BeautifulSoup(full_page, features="lxml")

         # Find Image from Article HTML
         img_src = articleSoup.find("div", "InlineImage fnIPT")

         # Image Links we can Use will be Less than 500 chars
         # Pass if too Long
         if len(str(img_src)) < 500:
            pass
         else:

            # Finalize Image Link
            img_src = str(img_src).split('src="')[1].split('"/>')[0]

            # Gather Date from Article
            # Format with a Utils Function
            dates = list()
            date = articleSoup.find('div', 'xTlfF')
            monthDay = str(date).split('">')[1].split(', ')[0].split(' ')
            year = str(date).split('">')[1].split(', ')[1]
            dates.append(monthDay[1])
            dates.append(monthDay[0])
            dates.append(year)
            date = formatDate(dates)
            
            # Gather Title from Article
            # Format with  a Utils Function
            # Format Title to get Image Title
            title = articleSoup.select('h1')
            title = str(title).split('">')[1].split('</h')[0]
            title = titleFormat(title)
            img_title = imgTitleFormat(title)

            # Initialize Storage Variables
            bucket = storage.bucket()
            token = ""

            # Check if an Article which is 80%+
            # Similar to any Other in the Log
            # Similar function in Utils
            check = False
            for ref in ref_list:
               similarity = similar(ref,title)
               if similarity > .8:
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

               # Get Image Data using Requests
               # Create the Image Locally
               # Upload image to Storage
               with open(f"{img_path}/{img_title}", "wb") as img:
                   img.write(requests.get(img_src).content)
                   blob = bucket.blob(f"Abc/{img_title}")
                   token = uuid4()
                   metadata = {"firebaseStorageDownloadTokens": token}
                   blob.upload_from_filename(f"{img_path}/{img_title}")
               
               # Get Link to the Stored Image
               storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/Abc%2F{img_title}?alt=media&token={token}"

               # Push the Gathered Data to DB
               # Using Utils method
               data = {
                   "title": title,
                   "date": date,
                   "img_src": img_src,
                   "img_title": img_title,
                   "link": link,
                   "outlet": outlet,
                   "storage_link": storage_link,
                   "order": order
               }
               open_json = open(json_dump_path, "r")
               read_json = open_json.read()
               if read_temp == "":
                   dumpJson(json_dump_path,data)
               else:
                   appendJson(json_dump_path,data)
               
               pushToDB(
                   db_path, title, date, img_src, img_title, link, outlet, storage_link, order
               )
               
               # Write Title to Local Log File
               open_temp.write(str(title) + "\n")

               # Return Confirmation of New DB Entry 
               print("ABC Article Added to DB")

            # Title was Already in the Log List
            # or too Similar to another
            else:
                print("ABC Article Already in DB")
   
   # One of Many Possible Things
   # Went Wrong - 
   # Too Much of This is an Issue
   except:
      print("Abc Article Error")