import os
from uuid import uuid4
import requests
from bs4 import BeautifulSoup
from firebase_admin import storage
from utils.utilities import (formatDate, imgFolder, imgTitleFormat, initialise, jsonFolder, dumpJson, appendJson,
                           logFolder, pageSoup, pushToDB, titleFormat, similar,getHour)

# Set Global Variables
ref_list = []
log_file_path = "/home/bencapper/src/News-Aggregator/scripts/log/euronewsdone.log"
log_folder_path = "/home/bencapper/src/News-Aggregator/scripts/log/"
json_dump_path = "/home/bencapper/src/News-Aggregator/scripts/json/euron.json"
json_folder_path = "/home/bencapper/src/News-Aggregator/scripts/json/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
page_url = "https://www.euronews.com/news/international"
img_path = "/home/bencapper/src/News/Euronews"
storage_path = "https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o"
db_path = "stories"
outlet = "www.Euronews.com"
prefix = "https://www.euronews.com"

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
articles = soup.find_all("div", "m-object__body")[22:-34]

# Cycle through List just Gathered
# And Save to DB or Pass due to Lack
# of Information Available
for article in articles:

  # Catch all for a Litany of Possible Errors
  try:

      # Get Article Link
      h = article.select('h3')
      href = str(h).split('href="')[1].split('" ')[0]
      link = f"{prefix}{href}"

      # Get Full Article Page
      full_page = requests.get(link).content
      articleSoup = BeautifulSoup(full_page, features="lxml")

      # Gather Title / Img Title
      title = articleSoup.select("h1")
      title = str(title).split('">')[1].split('</')[0].rstrip().lstrip()
      title = titleFormat(title)
      img_title = imgTitleFormat(title)

      # Gather / Format Date
      dates = articleSoup.select("time")
      dates = str(dates).split('datetime="')[1].split('">')[0].split('-')
      date = list()
      date.append(dates[2])
      date.append(dates[1])
      date.append(dates[0])
      date = formatDate(date)

      # Get Image HTML Tag
      img = articleSoup.find("img", "js-poster-img c-article-media__img u-max-height-full u-width-full")
      
      # Initialize Storage Variables
      bucket = storage.bucket()
      token = ""

      # No Image Tag Found
      # Use Default Image
      if img is None:
          img_title = "eu.png"
          img_src = ""
          storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/Euronews%2Feun.jpg?alt=media&token=1fc82731-59b7-41cb-b1e5-07f936a0322f"
          
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
              appendJson(json_dump_path,data)

              # Push the Gathered Data to DB
              # Using Utils method
              pushToDB(
                     db_path,
                     title,
                     date,
                     img_src,
                     img_title,
                     link,
                     outlet,
                     storage_link,
                     order
                   )

              # Write Title to Local Log File
              open_temp.write(str(title) + "\n")
              print("Euronews Article Added to DB - (No Image)")
          else:
              print("Euronews Article Already in DB - (No Image)")

      # Image Tag Found
      # Use it
      else:

          # Finalize Image Link
          img_src = str(img).split('src="')[1].split('" ')[0]

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

              # Get Image Data using Requests
              # Create the Image Locally
              # Upload image to Storage
              with open(f"{img_path}/{img_title}", "wb") as img:
                  img.write(requests.get(img_src).content)
                  blob = bucket.blob(f"Euronews/{img_title}")
                  token = uuid4()
                  metadata = {"firebaseStorageDownloadTokens": token}
                  blob.upload_from_filename(f"{img_path}/{img_title}")

                  # Get Link to the Stored Image
                  storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/Euronews%2F{img_title}?alt=media&token={token}"

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
                  appendJson(json_dump_path,data)
                  # Push the Gathered Data to DB
                  # Using Utils method
                  pushToDB(
                      db_path,
                      title,
                      date,
                      img_src,
                      img_title,
                      link,
                      outlet,
                      storage_link,
                      order
                  )

                  # Write Title to Local Log File
                  open_temp.write(str(title) + "\n")
                  print("Euronews Article Added to DB")
          else:
              print("Euronews Article Already in DB")

  # One of Many Possible Things
  # Went Wrong - 
  # Too Much of This is an Issue
  except:
      print("Euronews Article Error")
