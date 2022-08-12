import os
from uuid import uuid4
 
import requests
from firebase_admin import storage
 
from utils.utilities import (formatDate, imgFolder, imgTitleFormat, initialise,
                            logFolder, pageSoup, pushToDB, titleFormat, similar,getHour)

# Set Global Variables
ref_list = []
log_file_path = "/home/bencapper/src/News-Aggregator/scripts/log/griptdone.log"
log_folder_path = "/home/bencapper/src/News-Aggregator/scripts/log/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
page_url = "https://gript.ie/news/"
img_path = "/home/bencapper/src/News/Gript"
storage_path = "https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o"
db_path = "stories"
outlet = "www.Gript.ie"
 
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
articles = soup.find_all("archive-card")
articles = str(articles).split(" post-image")[1:-1]

# Cycle through List just Gathered
# And Save to DB or Pass due to Lack
# of Information Available
for article in articles:

    # Catch all for a Litany of Possible Errors
    try:

        # Gather Date from Article
        # Format with a Utils Function
        date = (
            article.split(' post-date="')[1]
            .split('" post')[0]
            .replace(",", " ")
            .replace("  ", " ")
            .split()
        )
        month = date[0]
        day = date[1]
        if len(day) == 1:
            day = f"0{day}"
        year = date[2]
        date = [day, month, year]
        date = formatDate(date)
    
        # Get Article Link
        url = article.split(' post-url="')[1].split('/">')[0].split('" secondary')[0]
        img_link = article.split('="')[1].split('"')[0]

        # Gather Title from Article
        # Format with  a Utils Function
        # Format Title to get Image Title
        title = article.split('" post-title="')[1].split('" post')[0]
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

           # If Image is Empty
           # Use Default   
           if img_link == "":
               img_title = "gript.jpg"
               storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/Gript%2Fgript.png?alt=media&token=b3bc4822-64f6-4ade-8910-e8a509ff759b"
               
               # Push the Gathered Data to DB
               # Using Utils method
               pushToDB(
                  db_path,
                  title,
                  date,
                  img_link,
                  img_title,
                  url,
                  outlet,
                  storage_link,
                  order
                )
               # Write Title to Local Log File
               open_temp.write(str(title) + "\n")
               print("Gript Article Added to DB - (No Image)")

           # Image Available
           else:
              
              # Get Image Data using Requests
              # Create the Image Locally
              # Upload image to Storage
              with open(f"{img_path}/{img_title}", "wb") as img:
                img.write(requests.get(img_link).content)
                blob = bucket.blob(f"Gript/{img_title}")
                token = uuid4()
                metadata = {"firebaseStorageDownloadTokens": token}
                blob.upload_from_filename(f"{img_path}/{img_title}")   
                storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/Gript%2F{img_title}?alt=media&token={token}"   
                
                # Push the Gathered Data to DB
                # Using Utils method
                pushToDB(
                    db_path,
                    title,
                    date,
                    img_link,
                    img_title,
                    url,
                    outlet,
                    storage_link,
                    order
                )

                # Write Title to Local Log File
                open_temp.write(str(title) + "\n")
                print("Gript Article Added to DB")
        else:
            print("Gript Article Already in DB")

    # One of Many Possible Things
    # Went Wrong - 
    # Too Much of This is an Issue
    except:
        print("Gript Article Error")
