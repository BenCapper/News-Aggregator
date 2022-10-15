import os
 
import datetime
from firebase_admin import storage
 
from utils.utilities import (decodeTitle, imgFolder, initialise, jsonFolder, dumpJson, appendJson,
                            logFolder, pageSoup, pushToDB, titleFormat, similar,getHour)

# Set Global Variables
ref_list = []
token = ""
log_file_path = "/home/bencapper/src/News-Aggregator/scripts/log/rev.log"
log_folder_path = "/home/bencapper/src/News-Aggregator/scripts/log/"
json_dump_path = "/home/bencapper/src/News-Aggregator/scripts/json/rev.json"
json_folder_path = "/home/bencapper/src/News-Aggregator/scripts/json/"
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


 
# Gather News Page HTML
# Find the Div Containing
# Targeted Article Links
soup = pageSoup(page_url)
articles = soup.find_all("article")
 
# Cycle through List just Gathered
# And Save to DB or Pass due to Lack
# of Information Available
for article in articles[:10]:

    # Catch all for a Litany of Possible Errors

        # Order Based on Current Hour
        # Reversed in Android Studio
        # to Make Sure The Most Recent
        # Articles are Shown First
        order = getHour()

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
                title = decodeTitle(title)
                if "<span" in title:
                    pass
                else:
                    # Found On Date
                    # Aggregation Site Links to
                    # Many Sites with Diff Layouts
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
                        storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/Revolver%2Frev.png?alt=media&token=bbb47374-c02d-41b5-859f-593fc90c5ea0"
    
                        data = {
                            "title": title,
                            "date": date,
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
                        pushToDB(
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
