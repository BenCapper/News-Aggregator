import os
from uuid import uuid4
import requests
from bs4 import BeautifulSoup
from firebase_admin import storage
from utils.utilities import (formatDate, imgFolder, imgTitleFormat, initialise,
                           logFolder, pageSoup, pushToDB, titleFormat, similar,getHour)

# Set Global Variables
ref_list = []
log_file_path = "/home/bencapper/src/News-Aggregator/scripts/log/blazedone.log"
log_folder_path = "/home/bencapper/src/News-Aggregator/scripts/log/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
page_url = "https://theblaze.com/"
img_path = "/home/bencapper/src/News/Blaze"
storage_path = "https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o"
db_path = "stories"
outlet = "www.TheBlaze.com"

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
articles = soup.find_all("div", "posts-wrapper clearfix")

# Cycle through List just Gathered
# And Save to DB or Pass due to Lack
# of Information Available
for article in articles:
    
    # Catch all for a Litany of Possible Errors
    try:

        # Get Links to each article
        # If Link is from an Unwanted
        # Section then Skip it
        a = article.find_all("a")
        urls = list()
        for art in a:
            url = str(art).split(' href="')[1].split('">')[0].split('" target')[0]
            if "bit.ly" in url:
                pass
            elif "podcast" in url:
                pass
            elif "/u/" in url:
                pass
            elif "/shows/" in url:
                pass
            elif url in urls:
                pass
            else:
                urls.append(url)

        # Have a List of Links
        # Cycle Through and Use Each Link
        # to get Full Page HTML
        for link in urls:

            # Get Full Article Page
            full_page = requests.get(link).content
            articleSoup = BeautifulSoup(full_page, features="lxml")

            # Find the Date Tag
            d = articleSoup.find("span","post-date")

            # Skip if Date is Empty
            if d is None:
                pass
            else:

                # Gather Date from Article
                # Format with a Utils Function
                dates = str(d).split('">')[1].split('</')[0].replace(',',' ').replace('  ', ' ').split(' ')
                date = list()
                date.append(dates[1])
                date.append(dates[0])
                date.append(dates[2])
                date = formatDate(date)
      
                # Gather Title from Article
                # Format with  a Utils Function
                # Format Title to get Image Title
                img_src = articleSoup.find("div", "widget__image crop-16x9")
                title = str(img_src).split('label="')[1].split('" class')[0]
                title = titleFormat(title)
                img_title = imgTitleFormat(title)

                # Finalize Image Link
                # Must be Done After img_src Variable
                # is Used to Grab the Title
                img_src = str(img_src).split("url('")[1].split("')")[0]
      
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
                        blob = bucket.blob(f"Blaze/{img_title}")
                        token = uuid4()
                        metadata = {"firebaseStorageDownloadTokens": token}
                        blob.upload_from_filename(f"{img_path}/{img_title}")

                    # Get Link to the Stored Image
                    storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/Blaze%2F{img_title}?alt=media&token={token}"
      
                    # Push the Gathered Data to DB
                    # Using Utils method
                    pushToDB(
                        db_path, title, date, img_src, img_title, link, outlet, storage_link, order
                    )

                    # Write Title to Local Log File
                    open_temp.write(str(title) + "\n")

                    # Return Confirmation of New DB Entry 
                    print("Blaze Article Added to DB")

                # Title was Already in the Log List
                # or too Similar to another
                else:
                    print("Blaze Article Already in DB")
                    
    # One of Many Possible Things
    # Went Wrong - 
    # Too Much of This is an Issue
    except:
        print("Blaze Article Error")
