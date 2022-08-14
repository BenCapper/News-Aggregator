import os
from uuid import uuid4
 
import requests
import datetime
from bs4 import BeautifulSoup
from firebase_admin import storage
 
from utils.utilities import (formatDate, imgFolder, imgTitleFormat, initialise, jsonFolder, dumpJson, appendJson,
                            logFolder, pageSoup, pushToDB, titleFormat, similar,getHour)

# Set Global Variables
ref_list = []
token = ""
log_file_path = "/home/bencapper/src/News-Aggregator/scripts/log/rtedone.log"
log_folder_path = "/home/bencapper/src/News-Aggregator/scripts/log/"
json_dump_path = "/home/bencapper/src/News-Aggregator/scripts/json/rte.json"
json_folder_path = "/home/bencapper/src/News-Aggregator/scripts/json/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
page_url = "https://www.rte.ie/news/"
img_path = "/home/bencapper/src/News/Rte"
storage_path = "https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o"
db_path = "stories"
outlet = "www.RTE.ie"
prefix = "https://www.rte.ie"
 
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
articles = soup.find_all("div", "article-meta")

# Cycle through List just Gathered
# And Save to DB or Pass due to Lack
# of Information Available
for article in articles:

    # Catch all for a Litany of Possible Errors
    try:

        # Tracks if Article has an Image
        empty = False

        # Select the Article Link
        url = article.select("a")

        # Link Comes as a List lol
        # Pass if Empty
        if url != []:

            # Finalize Link
            url = str(url).split(' href="')[1].split('">')[0]
            url = f"{prefix}{url}"

            # Get Full Article Page
            full_page = requests.get(url).content
            articleSoup = BeautifulSoup(full_page, features="lxml")

            # Gather Title / Img Title
            title = articleSoup.select("h1")
            title = str(title).split('">')[1].split('</')[0]
            title = title.rstrip().lstrip()
            title = titleFormat(title)
            img_title = imgTitleFormat(title)

            # Gather / Format Date
            date = articleSoup.find("span", "modified-date")
            date = str(date).split(', ')[1].split(' <str')[0].split(' ')
            date = formatDate(date)

            # Attempt to Find Image Tag
            img_src = articleSoup.find("img", "changeable")
            if img_src is None:
                empty = True
                img_src = ""
            else:
                img_src = str(img_src).split(' data-src="')[1].split('" item')[0]

            # Initialize Storage Variables
            bucket = storage.bucket()
            token = ""

            # Unwanted Article, Pass
            if "<span" in title:
                pass
            else:

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

                    # No Image Found
                    # Use Default
                    if empty is True:
                        img_title = "rte.jpg"
                        storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/Rte%2Frte2.jpg?alt=media&token=ef07119d-f1bc-4823-a037-47d4e8947707"
                        
                        data = {
                            "title": title,
                            "date": date,
                            "img_src": img_src,
                            "img_title": img_title,
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
                           db_path,
                           title,
                           date,
                           img_src,
                           img_title,
                           url,
                           outlet,
                           storage_link,
                           order
                         )
                        
                        # Write Title to Local Log File
                        open_temp.write(str(title) + "\n")
                        print("RTE Article Added to DB - (No Image)")
                    else:
                        # Get Image Data using Requests
                        # Create the Image Locally
                        # Upload image to Storage
                        with open(f"{img_path}/{img_title}", "wb") as img:
                          img.write(requests.get(img_src).content)
                          blob = bucket.blob(f"Rte/{img_title}")
                          token = uuid4()
                          metadata = {"firebaseStorageDownloadTokens": token}
                          blob.upload_from_filename(f"{img_path}/{img_title}")
  
                          storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/Rte%2F{img_title}?alt=media&token={token}"

                        data = {
                            "title": title,
                            "date": date,
                            "img_src": img_src,
                            "img_title": img_title,
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
                            db_path,
                            title,
                            date,
                            img_src,
                            img_title,
                            url,
                            outlet,
                            storage_link,
                            order
                        )
                        # Write Title to Local Log File
                        open_temp.write(str(title) + "\n")
                        print("RTE Article Added to DB")
                else: 
                    print("RTE Article Already in DB")

    # One of Many Possible Things
    # Went Wrong - 
    # Too Much of This is an Issue
    except:
        print("RTE Article Error")


