import os
 
import datetime
from firebase_admin import storage
 
from utils.utilities import (imgFolder, initialise,
                            logFolder, pageSoup, pushToDbNoImg, titleFormat, similar,getHour)

# Set Global Variables
ref_list = []
token = ""
log_file_path = "/home/bencapper/src/News-Aggregator/scripts/log/rev.log"
log_folder_path = "/home/bencapper/src/News-Aggregator/scripts/log/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
page_url = "https://www.revolver.news/"
img_path = "/home/bencapper/src/News/Rev"
storage_path = "https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o"
db_path = "stories"
outlet = "www.Revolver.news"
twitter = 0
archive = 0
url = ""
 
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
articles = soup.find_all("h2", "title")
 
# Cycle through List just Gathered
# And Save to DB or Pass due to Lack
# of Information Available
for article in articles:

    # Catch all for a Litany of Possible Errors
    try:

        # Get Article Link
        a = article.select("a")
        url = str(a).split(' href="')[1].split('" ')[0].split('">')[0]
        check = url.split('https://')

        # Make sure the element exists
        if len(check) == 1:
            pass
        else:

            # Make Sure Twitter or Archive
            # Sections are Not Included
            check = check[1].split('/')[0]
            twitter = check.find("twitter")
            archive = check.find("archive")
            if twitter == 0 or archive == 0:
                pass
            else:

                # Gather Title / No Images
                title = str(a).split('">')[1].split('</a')[0]
                title = titleFormat(title)

                # Found On Date
                # Aggregation Site Links to
                # Many Sites with Diff Layouts
                date = datetime.datetime.now().strftime("%m-%d-%y")
                date = f"Found on: {date}"

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
                    storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/Rev%2Frevolver.png?alt=media&token=fb623780-8bc9-45fd-a681-5dbd791cc988"

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
                    print("Revolver Article Added to DB")
                else:
                    print("Revolver Article Already in DB")
    
    # One of Many Possible Things
    # Went Wrong - 
    # Too Much of This is an Issue
    except:
        print("Revolver Article Error")