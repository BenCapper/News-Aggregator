import os
from uuid import uuid4
import requests
from bs4 import BeautifulSoup
from firebase_admin import storage
from utils.utilities import (
    decodeTitle,
    formatDate,
    imgFolder,
    imgTitleFormat,
    initialise,
    jsonFolder,
    appendJson,
    todayDate,
    logFolder,
    pageSoup,
    pushToDB,
    similar,
    getHour,
)

td = todayDate()
# Set Global Variables
ref_list = []
log_file_path = "/home/bencapper/src/News-Aggregator/scripts/log/cbsdone.log"
log_folder_path = "/home/bencapper/src/News-Aggregator/scripts/log/"
json_dump_path = "/home/bencapper/src/News-Aggregator/scripts/json/cbs.json"
json_folder_path = "/home/bencapper/src/News-Aggregator/scripts/json/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
page_url = "https://www.cbsnews.com/us/"
img_path = f"/home/bencapper/src/News/Cbs/{td}"
storage_path = ("https://firebasestorage.googleapis.com/"
                "v0/b/news-a3e22.appspot.com/o")
db_path = "stories"
outlet = "www.CbsNews.com"

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
articles = soup.find_all("article")

# Cycle through List just Gathered
# And Save to DB or Pass due to Lack
# of Information Available
for article in articles:
    # Catch all for a Litany of Possible Errors
    try:
        # Unwanted Article Type
        if "item--type-video" in str(article):
            pass

        else:
            # Gather Title from Article
            # Format with  a Utils Function
            # Format Title to get Image Title
            title = article.select("h4")
            title = str(title).split('">')[1].split("</")[0].lstrip().rstrip()
            title = decodeTitle(title)
            img_title = imgTitleFormat(title)

            # Select Image Tag from Article HTML
            # Finalize Image Link
            img_src = article.select("img")
            img_src = str(img_src).split(' data-srcset="')[1].split(" ")[0]

            # Get Link to the article
            link = article.select("a")
            link = str(link).split('href="')[1].split('" ')[0]

            # Get Full Article Page
            full_page = requests.get(link).content
            articleSoup = BeautifulSoup(full_page, features="lxml")

            # Gather Date from Article
            # Format with a Utils Function
            date = articleSoup.select("time")
            if "Updated on:" in str(date):
                dates = list()
                date = (
                    str(date)
                    .split("Updated on:")[1]
                    .split("/")[0]
                    .replace(",", " ")
                    .replace("  ", " ")
                    .lstrip()
                    .rstrip()
                    .split(" ")
                )
                dates.append(date[1])
                dates.append(date[0])
                dates.append(date[2])
                date = formatDate(dates)
            else:
                dates = list()
                date = (
                    str(date)
                    .split('">')[1]
                    .split("/")[0]
                    .replace(",", " ")
                    .replace("  ", " ")
                    .lstrip()
                    .rstrip()
                    .split(" ")
                )
                dates.append(date[1])
                dates.append(date[0])
                dates.append(date[2])
                date = formatDate(dates)

            # Initialize Storage Variables
            bucket = storage.bucket()
            token = ""

            # Check if an Article which is 80%+
            # Similar to any Other in the Log
            # Similar function in Utils
            check = False
            for ref in ref_list:
                similarity = similar(ref, title)
                if similarity > 0.8:
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
                    blob = bucket.blob(f"Cbs/{td}/{img_title}")
                    token = uuid4()
                    metadata = {"firebaseStorageDownloadTokens": token}
                    blob.upload_from_filename(f"{img_path}/{img_title}")

                # Get Link to the Stored Image
                storage_link = (
                                f"https://firebasestorage.googleapis.com/"
                                f"v0/b/news-a3e22.appspot.com/o/Cbs%2F{td}"
                                f"%2F{img_title}?alt=media&token={token}"
                               )

                data = {
                    "title": title,
                    "date": date,
                    "link": link,
                    "outlet": outlet,
                    "storage_link": storage_link,
                    "order": order,
                }
                open_json = open(json_dump_path, "r")
                read_json = open_json.read()
                appendJson(json_dump_path, data)
                # Push the Gathered Data to DB
                # Using Utils method
                pushToDB(db_path, title, date,
                         link, outlet,
                         storage_link, order)

                # Write Title to Local Log File
                open_temp.write(str(title) + "\n")

                # Return Confirmation of New DB Entry
                print("CBS Article Added to DB")

            # Title was Already in the Log List
            # or too Similar to another
            else:
                print("CBS Article Already in DB")

    # One of Many Possible Things
    # Went Wrong -
    # Too Much of This is an Issue
    except:
        ("CBS Article Error")
