import os
from uuid import uuid4

import time
import requests
from bs4 import BeautifulSoup
from firebase_admin import storage
 
from utils.utilities import (imgFolder, imgTitleFormat, initialise,
                            logFolder, pageSoup, pushToDB, titleFormat, similar,getHour)

# Set Global Variables
ref_list = []
token = ""
log_file_path = "/home/bencapper/src/News-Aggregator/scripts/log/breitbartdone.log"
log_folder_path = "/home/bencapper/src/News-Aggregator/scripts/log/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
page_url = "https://www.breitbart.com"
img_path = "/home/bencapper/src/News/Breitbart"
storage_path = "https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o"
db_path = "stories"
outlet = "www.Breitbart.com"
storage_link = ""
img_src = ""
 
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
# Find the List of Articles
soup = pageSoup(page_url)
articles = soup.find_all("article")

# Cycle through List just Gathered
# And Save to DB or Pass due to Lack
# of Information Available
for article in articles:

    # Catch all for a Litany of Possible Errors
    try:

        # Boolean to Track Whether an
        # Image Exists to Grab
        empty = False

        # Sleep for 5 secs Before Next Article
        # Avoids Request Refusals from Servers
        time.sleep(5)

        # Get Link to the article
        url = str(article).split(' href="')[1].split('" title="')[0].split('">')[0]

        # Get Full Article Page
        full_page = requests.get(url).content
        articleSoup = BeautifulSoup(full_page, features="lxml")

        # Gather Date from Article
        date = str(articleSoup).split('datetime="')[1].split('T')[0].split('-')
        day = date[2]
        month = date[1]
        year = date[0][2:]
        date = f"{month}-{day}-{year}"

        # Gather Title from Article
        # Format with  a Utils Function
        # Format Title to get Image Title
        title = articleSoup.select("h1")
        title = str(title).split('h1>')[1].split('</')[0]
        title = titleFormat(title)
        img_title = imgTitleFormat(title)

        # Finalize Image Link
        # If Empty is True, Use Default Image
        try:
            img_src = articleSoup.select("figure")
            img_src = str(img_src).split('src="')[1].split('" title')[0]
        except:
            img_src = ""
            empty = True

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

            # Using Default Image
            if empty is True:
                img_title = "bb.jpg"
                storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/Breitbart%2Fbb.jpg?alt=media&token=14e1f346-6cdd-4ded-bbba-4dd6e9a42d1f"
                
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
                 )
 
                # Write Title to Local Log File
                open_temp.write(str(title) + "\n")

                # Return Confirmation of New DB Entry 
                print("Breitbart Article Added to DB - (No Image)")

            # Found Image on Article Page
            else:

                # Get Image Data using Requests
                # Create the Image Locally
                # Upload image to Storage
                with open(f"{img_path}/{img_title}", "wb") as img:
                  img.write(requests.get(img_src).content)
                  blob = bucket.blob(f"Breitbart/{img_title}")
                  token = uuid4()
                  metadata = {"firebaseStorageDownloadTokens": token}
                  blob.upload_from_filename(f"{img_path}/{img_title}")
                  
                  # Get Link to the Stored Image
                  storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/Breitbart%2F{img_title}?alt=media&token={token}"
  
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

                  # Return Confirmation of New DB Entry 
                  print("Breitbart Article Added to DB")

        # Title was Already in the Log List
        # or too Similar to another
        else:
            print("Breitbart Article Already in DB")
    
    # One of Many Possible Things
    # Went Wrong - 
    # Too Much of This is an Issue
    except:
        print("Breitbart Article Error")


 
     