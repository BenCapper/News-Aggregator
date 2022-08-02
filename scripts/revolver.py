import os
 
import datetime
from firebase_admin import storage
 
from utils.utilities import (imgFolder, initialise,
                            logFolder, pageSoup, pushToDbNoImg, titleFormat, similar)
 
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
 
 
logFolder(log_folder_path)
 
if os.path.exists(log_file_path):
   open_temp = open(log_file_path, "r")
   read_temp = open_temp.read()
   ref_list = read_temp.splitlines()
else:
   os.mknod(log_file_path)
 
imgFolder(img_path)
initialise(json_path, db_url, bucket)
 
 
soup = pageSoup(page_url)
articles = soup.find_all("h2", "title")
 
order = 0
for article in articles:
    try:
        a = article.select("a")
        url = str(a).split(' href="')[1].split('" ')[0].split('">')[0]
        check = url.split('https://')
        if len(check) == 1:
            pass
        else:
            check = check[1].split('/')[0]
            twitter = check.find("twitter")
            archive = check.find("archive")
            if twitter == 0 or archive == 0:
                pass
            else:
                title = str(a).split('">')[1].split('</a')[0]
                title = titleFormat(title)
                date = datetime.datetime.now().strftime("%m-%d-%y")
                date = f"Found on: {date}"

                bucket = storage.bucket()
                token = ""
                check = False
                for ref in ref_list:
                   similarity = similar(ref,title)
                   if similarity > .8:
                      check = True
                      break
                if title not in ref_list and check is False:
                   ref_list.append(title)
                   open_temp = open(log_file_path, "a")
                   storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/Rev%2Frevolver.png?alt=media&token=fb623780-8bc9-45fd-a681-5dbd791cc988"

                   pushToDbNoImg(
                        db_path,
                        title,
                        date,
                        url,
                        outlet,
                        storage_link,
                        order
                    )
                   order = order + 1
                   open_temp.write(str(title) + "\n")
                   print("Revolver Article Added to DB")
                else:
                   print("Revolver Article Already in DB")
    except:
        print("Revolver Article Error")