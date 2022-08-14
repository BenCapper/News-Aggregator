import os
from uuid import uuid4
 
import requests
from bs4 import BeautifulSoup
from firebase_admin import storage
 
from utils.utilities import (formatDate, imgFolder, imgTitleFormat, initialise, jsonFolder, dumpJson, appendJson,
                            logFolder, pageSoup, pushToDB, titleFormat, similar,getHour)

# Set Global Variables
ref_list = []
token = ""
log_file_path = "/home/bencapper/src/News-Aggregator/scripts/log/gbdone.log"
log_folder_path = "/home/bencapper/src/News-Aggregator/scripts/log/"
json_dump_path = "/home/bencapper/src/News-Aggregator/scripts/json/gb.json"
json_folder_path = "/home/bencapper/src/News-Aggregator/scripts/json/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
page_url = "https://www.gbnews.uk/tag/news"
img_path = "/home/bencapper/src/News/GB"
storage_path = "https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o"
db_path = "stories"
outlet = "www.GBNews.uk"
 
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
articles = soup.find_all("div", "content")[:-2]
 
# Cycle through List just Gathered
# And Save to DB or Pass due to Lack
# of Information Available
for article in articles:
    try:
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
                img.write(requests.get(src).content)
                blob = bucket.blob(f"GB/{img_title}")
                token = uuid4()
                metadata = {"firebaseStorageDownloadTokens": token}
                blob.upload_from_filename(f"{img_path}/{img_title}")

            # Get Link to the Stored Image
            storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/GB%2F{img_title}?alt=media&token={token}"
    
            data = {
                   "title": title,
                   "date": date,
                   "img_src": src,
                   "img_title": img_title,
                   "link": url,
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
            # Push the Gathered Data to DB
            # Using Utils method
            pushToDB(
                db_path, title, date, src, img_title, url, outlet, storage_link, order
            )

            # Write Title to Local Log File
            open_temp.write(str(title) + "\n")
            print("GB News Article Added to DB")
        else:
            print("GB News Article Already in DB")
    
    # One of Many Possible Things
    # Went Wrong - 
    # Too Much of This is an Issue
    except:
        print("GB News Article Error")
 