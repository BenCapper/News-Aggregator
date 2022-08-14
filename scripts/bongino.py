import os
 
import datetime
from firebase_admin import storage
 
from utils.utilities import (imgFolder, imgTitleFormat, initialise, jsonFolder, dumpJson, appendJson,
                            logFolder, pageSoup, pushToDbNoImg, titleFormat, similar,getHour)

# Set Global Variables
ref_list = []
log_file_path = "/home/bencapper/src/News-Aggregator/scripts/log/bonginodone.log"
log_folder_path = "/home/bencapper/src/News-Aggregator/scripts/log/"
json_dump_path = "/home/bencapper/src/News-Aggregator/scripts/json/bong.json"
json_folder_path = "/home/bencapper/src/News-Aggregator/scripts/json/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
page_url = "https://www.bonginoreport.com/"
img_path = "/home/bencapper/src/News/Bongino"
storage_path = "https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o"
db_path = "stories"
outlet = "www.BonginoReport.com"

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
articles = soup.find_all("li")
articles = articles[:-8]

# Pointlessly (so far) split
# Articles into Categories from
# Bongino Site
cap_1 = articles[:8]
cult_2 = articles[8:16]
eco_3 = articles[16:24]
swamp_4 = articles[24:31]
glob_5 = articles[31:38]
opin_6 = articles[38:45]
ent_7 = articles[45:51]
sport_8 = articles[51:57]
health_9 = articles[57:63]

# Cycle through List just Gathered
# And Save to DB or Pass due to Lack
# of Information Available
for article in articles:

   # Catch all for a Litany of Possible Errors
   try:

      # Get Link to the article
      a = article.select("a")
      url = str(a).split(' href="')[1].split('" target')[0]

      # Gather Title from Article
      # Format with  a Utils Function
      # Format Title to get Image Title
      title = str(a).split('">')[1].split('</a')[0]
      title = titleFormat(title)
      img_title = imgTitleFormat(title)

      # Gather Date from Article
      # Format with a Utils Function
      date = datetime.datetime.now().strftime("%m-%d-%y")

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

         # Site Links to Many News Sites
         # Can't Reliably Isolate Image Tag
         # Use Pre-Stored Default Image Instead
         storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/Bongino%2Fbongino.jpg?alt=media&token=c347bc71-7d59-45bf-b1eb-a1f98ab0965f"

         data = {
             "title": title,
             "date": date,
             "img_src": "",
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
         pushToDbNoImg(
              db_path,
              title,
              date,
              url,
              outlet,
              storage_link,
              order
          )

         # Write Title to Local Log File
         open_temp.write(str(title) + "\n")

         # Return Confirmation of New DB Entry 
         print("Bongino Article Added to DB")

      # Title was Already in the Log List
      # or too Similar to another
      else:
         print("Bongino Article Already in DB")

   # One of Many Possible Things
   # Went Wrong - 
   # Too Much of This is an Issue
   except:
      print("Bongino Article Error")
