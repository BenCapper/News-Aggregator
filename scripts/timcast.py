import os
import re
from uuid import uuid4
import requests
from firebase_admin import storage
from utils.utilities import (
    decodeTitle,
    imgFolder,
    imgTitleFormat,
    initialise,
    logFolder,
    pageSoup,
    pushToDB,
    similar,
    getHour,
    todayDate,
    jsonFolder,
    appendJson,
)

td = todayDate()
# Set Global Variables
ref_list = []
log_folder_path = "/home/bencapper/src/News-Aggregator/scripts/log/"
log_file_path = "/home/bencapper/src/News-Aggregator/scripts/log/timdone.log"
json_dump_path = "/home/bencapper/src/News-Aggregator/scripts/json/tim.json"
json_folder_path = "/home/bencapper/src/News-Aggregator/scripts/json/"
json_path = "/home/bencapper/src/News/news.json"
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
page_url = "https://timcast.com/news/"
img_path = f"/home/bencapper/src/News/Timcast/{td}"
storage_path = ("https://firebasestorage.googleapis.com"
                "/v0/b/news-a3e22.appspot.com/o")
db_path = "stories"
outlet = "www.Timcast.com"

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
articles = soup.find_all("div", "article-block")

# Cycle through List just Gathered
# And Save to DB or Pass due to Lack
# of Information Available
for article in articles:
    # Catch all for a Litany of Possible Errors
    try:
        # Get article date, format day if < 10
        found_date = ""
        match_pattern1 = "[0-9]{2}.[0-9]{1}.[0-9]{2}"
        match_pattern2 = "[0-9]{2}.[0-9]{2}.[0-9]{2}"
        date = article.find("div", "summary")
        date = str(date)
        found_date = re.findall(match_pattern1, date)
        found_date = str(found_date).replace(".", "-")
        found_date = found_date.lstrip("['").rstrip("']")
        if found_date == "":
            found_date = re.findall(match_pattern2, date)
            found_date = str(found_date).replace(".", "-")
            found_date = found_date.lstrip("['").rstrip("']")
        else:
            days = found_date.split("-")
            day = f"0{days[1]}"
            found_date = f"{days[0]}-{day}-{days[2]}"

        # Get link to article
        link = article.find("a")
        link = str(link)[23:]
        link = link.split('"')
        link = link[0]

        # Get Image and Title
        image = article.find("img")
        image = str(image)
        img_list = image.split(" src=")
        title = img_list[0][10:-1]
        img_link = img_list[1][1:-3]
        title = decodeTitle(title)
        img_title = imgTitleFormat(title)

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
            # add to log list and open file
            ref_list.append(title)
            open_temp = open(log_file_path, "a")

            # Get Image Data using Requests
            # Create the Image Locally
            # Upload image to Storage
            with open(f"{img_path}/{img_title}", "wb") as img:
                img.write(requests.get(img_link).content)
                blob = bucket.blob(f"Timcast/{td}/{img_title}")
                token = uuid4()
                metadata = {"firebaseStorageDownloadTokens": token}
                blob.upload_from_filename(f"{img_path}/{img_title}")
            storage_link = (
                f"{storage_path}/Timcast%2F{td}%2F{img_title}?"
                f"alt=media&token={token}"
            )

            data = {
                "title": title,
                "date": found_date,
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
            pushToDB(db_path, title, found_date,
                     link, outlet, storage_link, order)

            # Write Title to Local Log File
            open_temp.write(str(title) + "\n")
            print("Timcast Article Added to DB")
        else:
            print("Timcast Article Already in DB")

    # One of Many Possible Things
    # Went Wrong -
    # Too Much of This is an Issue
    except:
        print("Timcast Article Error")
