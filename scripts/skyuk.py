import os
from uuid import uuid4
 
import requests
import datetime
from bs4 import BeautifulSoup
from firebase_admin import storage
 
from utils.utilities import (decodeTitle, formatDate, imgFolder, imgTitleFormat, initialise, jsonFolder, dumpJson, appendJson,
                            todayDate,logFolder, pageSoup, pushToDB, titleFormat, similar,getHour)
td = todayDate()
# Set Global Variables
ref_list = []
token = ""
log_file_path = "/home/bencapper/src/News-Aggregator/scripts/log/skyukdone.log"
log_folder_path = "/home/bencapper/src/News-Aggregator/scripts/log/"
json_dump_path = "/home/bencapper/src/News-Aggregator/scripts/json/sky.json"
json_folder_path = "/home/bencapper/src/News-Aggregator/scripts/json/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
page_url = "https://news.sky.com/uk"
img_path = f"/home/bencapper/src/News/Skyuk/{td}"
storage_path = "https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o"
db_path = "stories"
outlet = "news.Sky.com"
url = "https://news.sky.com"
 
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
articles = soup.find_all("h3", "sdc-site-tile__headline")

# Cycle through List just Gathered
# And Save to DB or Pass due to Lack
# of Information Available
for article in articles:

    # Catch all for a Litany of Possible Errors
    try:
        
        # Get Article Link
        href = str(article).split('href="')[1].split('">')[0]
        link = f"{url}{href}"

        # Get Full Article Page
        full_page = requests.get(link).content
        articleSoup = BeautifulSoup(full_page, features="lxml")

        # Gather Title / Img Title
        title = articleSoup.find("span", "sdc-article-header__long-title")
        title = str(title).split('">')[1].split("</")[0]
        title = decodeTitle(title)
        img_title = imgTitleFormat(title)

        # Gather / Format Date
        date = articleSoup.find("p", "sdc-article-date__date-time")
        if date is None:
            pass
        else:
            date = str(date).split('day ')[1].split(":")[0][:-3].split(" ")
            date = formatDate(date)

            # Attempt to Find Image
            img_src = articleSoup.find("img", "sdc-article-image__item")
            if img_src is None:
                pass
            else:

                # Finalize Image Link
                img_src = str(img_src).split('src="')[1].split('" srcset')[0]

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
                        blob = bucket.blob(f"Skyuk/{td}/{img_title}")
                        token = uuid4()
                        metadata = {"firebaseStorageDownloadTokens": token}
                        blob.upload_from_filename(f"{img_path}/{img_title}")

                    # Get Link to the Stored Image
                    storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/Skyuk%2F{td}%2F{img_title}?alt=media&token={token}"

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
                    print("Sky UK Article Added to DB")
                else:
                    print("Sky UK Article Already in DB")
                    
    # One of Many Possible Things
    # Went Wrong - 
    # Too Much of This is an Issue
    except:
        print("Sky UK Article Error")