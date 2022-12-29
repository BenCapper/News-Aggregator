import os
from uuid import uuid4
 
import requests
from bs4 import BeautifulSoup
from firebase_admin import storage
 
from utils.utilities import (decodeTitle, formatDate, imgFolder, imgTitleFormat, initialise, jsonFolder, dumpJson, appendJson,
                            todayDate,logFolder, pageSoup, pushToDB, titleFormat, similar,getHour)
td = todayDate()
# Set Global Variables
ref_list = []
log_file_path = "/home/bencapper/src/News-Aggregator/scripts/log/voxdone.log"
log_folder_path = "/home/bencapper/src/News-Aggregator/scripts/log/"
json_dump_path = "/home/bencapper/src/News-Aggregator/scripts/json/vox.json"
json_folder_path = "/home/bencapper/src/News-Aggregator/scripts/json/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
page_url = "https://www.vox.com/"
img_path = f"/home/bencapper/src/News/Vox/{td}"
storage_path = "https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o"
db_path = "stories"
outlet = "www.Vox.com"

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
articles = soup.find_all("div", "c-compact-river__entry")

# Cycle through List just Gathered
# And Save to DB or Pass due to Lack
# of Information Available
for article in articles:

    # Catch all for a Litany of Possible Errors
    try:

        # Get Article Link
        h = article.select("h2")
        link = str(h).split('href="')[1].split('">')[0]

        # Get Full Article Page
        full_page = requests.get(link).content
        articleSoup = BeautifulSoup(full_page, features="lxml")

        # Gather Title / Img Title / Src
        title = articleSoup.select("h1")
        if "404 Not" in str(title):
            pass
        else:
            title = str(title).split('">')[1].split('</')[0]
            title = decodeTitle(title)
            img_title = imgTitleFormat(title)
            img = article.select("img")
            if len(img) > 1:
                img_src = str(img[1]).split('src="')[1].split('"/>')[0]
            elif 'native-ad-logo' in str(img[0]):
                img_src = "ad"
            else:
                img_src = str(img[0]).split('src="')[1].split('"/>')[0]
            if img_src == "ad":
                pass
            else:

                # Gather / Format Date
                dates = article.select('time')
                if dates == []:
                    pass
                else:
                    dates = str(dates).split('datetime="')[1].split('T')[0].split('-')
                    date = list()
                    date.append(dates[2])
                    date.append(dates[1])
                    date.append(dates[0])
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
                        ref_list.append(title)
                        open_temp = open(log_file_path, "a")

                        # Add the Title to the List
                        # of Titles Already in the Log
                        #with open(f"{img_path}/{img_title}", "wb") as img:
                        #    img.write(requests.get(img_src).content)
                        #    blob = bucket.blob(f"Vox/{td}/{img_title}")
                        #    token = uuid4()
                        #    metadata = {"firebaseStorageDownloadTokens": token}
                        #    blob.upload_from_filename(f"{img_path}/{img_title}")

                        # Get Link to the Stored Image
                        storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/Vox%2Fvox.png?alt=media&token=db1c46f1-1155-44fa-b0aa-61d0b992da21"

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
                        print("Vox Article Added to DB")
                    else:
                        print("Vox Article Already in DB")
    except:
        print("Vox Article Error")


