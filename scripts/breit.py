import os
from uuid import uuid4

import time
import requests
from bs4 import BeautifulSoup
from firebase_admin import storage
 
from utils.utilities import (formatDate, imgFolder, imgTitleFormat, initialise,jsonFolder, dumpJson, appendJson,
                            todayDate,logFolder, pageSoup, pushToDB, titleFormat, similar,getHour)
td = todayDate()
# Set Global Variables
ref_list = []
token = ""
log_file_path = "/home/bencapper/src/News-Aggregator/scripts/log/breitbartdone.log"
log_folder_path = "/home/bencapper/src/News-Aggregator/scripts/log/"
json_dump_path = "/home/bencapper/src/News-Aggregator/scripts/json/breit.json"
json_folder_path = "/home/bencapper/src/News-Aggregator/scripts/json/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
page_url = "https://www.breitbart.com/politics"
img_path = f"/home/bencapper/src/News/Breitbart/{td}"
storage_path = "https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o"
db_path = "stories"
outlet = "www.Breitbart.com"
storage_link = ""
img_src = ""
 
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


# Gather News Page HTML
# Find the List of Articles
soup = pageSoup(page_url)
articles = soup.find_all("article")

# Cycle through List just Gathered
# And Save to DB or Pass due to Lack
# of Information Available
for article in articles:
    time.sleep(2)
    try:
        url = str(article).split('href="')[1].split('" ')[0]
        url = f"{page_url}{url}"
        img_src = str(article).split('src="')[1].split('" ')[0]
        title = str(article).split('title="')[1].split('">')[0]
        dates = str(article).split('datetime="')[1].split('T')[0].split('-')
        date = list()
        date.append(dates[2])
        date.append(dates[1])
        date.append(dates[0])
        date = formatDate(date)
        title = titleFormat(title)
        img_title = imgTitleFormat(title)
        
        # Order Based on Current Hour
        # Reversed in Android Studio
        # to Make Sure The Most Recent
        # Articles are Shown First
        order = getHour()

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
                blob = bucket.blob(f"Breitbart/{td}/{img_title}")
                token = uuid4()
                metadata = {"firebaseStorageDownloadTokens": token}
                blob.upload_from_filename(f"{img_path}/{img_title}")

            # Get Link to the Stored Image
            storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/Breitbart%2F{td}%2F{img_title}?alt=media&token={token}"
            data = {
                   "title": title,
                   "date": date,
                   "link": url,
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
                db_path, title, date, url, outlet, storage_link, order
            )

            # Write Title to Local Log File
            open_temp.write(str(title) + "\n")

            # Return Confirmation of New DB Entry 
            print("Breitbart Article Added to DB")

        # Title was Already in the Log List
        # or too Similar to another
        else:
            print("Breitbart Article Already in DB")
    except:
        print("Error")