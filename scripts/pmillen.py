import os
from uuid import uuid4
 
import requests
from bs4 import BeautifulSoup
from firebase_admin import storage
 
from utils.utilities import (formatDate, imgFolder, imgTitleFormat, initialise,
                            logFolder, pageSoup, pushToDB, titleFormat, similar,getHour)

# Set Global Variables
ref_list = []
token = ""
log_file_path = "/home/bencapper/src/News-Aggregator/scripts/log/pmill.log"
log_folder_path = "/home/bencapper/src/News-Aggregator/scripts/log/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
page_url = "https://thepostmillennial.com/category/news/"
img_path = "/home/bencapper/src/News/PMill"
storage_path = "https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o"
db_path = "stories"
outlet = "www.ThePostMillennial.com"
 
# Set Local Folders
logFolder(log_folder_path)
imgFolder(img_path)

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
articles = soup.find_all("div", "post-card-inner")

# Cycle through List just Gathered
# And Save to DB or Pass due to Lack
# of Information Available
for article in articles:

    # Catch all for a Litany of Possible Errors
    try:

        # Get Article Link
        url = "https://www.ThePostMillennial.com"
        a = article.select("a")
        url = url + str(a).split('href="')[1].split('">')[0]

        # Gather Title / Img Title
        h = article.select("h3")
        title = str(h).split('">')[1].split('</h')[0]
        title = titleFormat(title)
        img_title = imgTitleFormat(title)

        # Get Full Article Page
        full_page = requests.get(url).content
        articleSoup = BeautifulSoup(full_page, features="lxml")

        # Get Image
        content = articleSoup.find_all("section", "cover")
        img_src = str(content).split(' src="')[1].split('" ')[0]

        # Gather / Format Date
        date = articleSoup.find("div", "article-info")
        date = date.select("time")
        date = str(date).split('">')[1].split(':')[0][:-2].replace(',', ' ').replace('  ', ' ')
        dates = date.split(' ')
        date = list()
        date.append(dates[1])
        date.append(dates[0])
        date.append(dates[2])
        date = formatDate(date)

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
                blob = bucket.blob(f"PMill/{img_title}")
                token = uuid4()
                metadata = {"firebaseStorageDownloadTokens": token}
                blob.upload_from_filename(f"{img_path}/{img_title}")

            # Get Link to the Stored Image
            storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/PMill%2F{img_title}?alt=media&token={token}"

            # Push the Gathered Data to DB
            # Using Utils method
            pushToDB(
                db_path, title, date, img_src, img_title, url, outlet, storage_link, order
            )

            # Write Title to Local Log File
            open_temp.write(str(title) + "\n")
            print("Post Millennial Article Added to DB")
        else:
            print("Post Millennial Article Already in DB")
            
    # One of Many Possible Things
    # Went Wrong - 
    # Too Much of This is an Issue
    except:
        print("Post Millennial Article Error")