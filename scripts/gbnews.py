from uuid import uuid4
from model import Article
from utils import logFolder, logFile, initialise, pageSoup, imgTitleFormat, imgFolder, pushToDB, formatDate, titleFormat
import requests
from bs4 import BeautifulSoup
from firebase_admin import db, storage
 
 
ref_list = []
token = ""
log_file_path = "/home/bencapper/src/News-Aggregator/scripts/log/gbdone.log"
log_folder_path = "/home/bencapper/src/News-Aggregator/scripts/log/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
page_url = "https://www.gbnews.uk/tag/news"
img_path = "/home/bencapper/src/News/GB"
storage_path = "https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o"
db_path = "stories/GB"
outlet = "www.GBNews.uk"
 
 
 
logFolder(log_folder_path)
ref_list = logFile(log_file_path)
imgFolder(img_path)
initialise(json_path, db_url, bucket)
 
 
soup = pageSoup(page_url)
articles = soup.find_all("div", "content")[:-2]
 
 
for article in articles:
 
   # get url
   a = article.select("a")
   url = str(a).split('href="')[1].split('" itemprop')[0]
 
   # get article page
   full_page = requests.get(url).content
   articleSoup = BeautifulSoup(full_page, features="lxml")
 
   # headline
   title = str(articleSoup.select("h1")).split('">')[1].split("</h1>")[0]
   title = titleFormat(title)
 
   # date
   date = str(articleSoup.select("time")).split("day ")[1].split(" -")[0].split(" ")
   date = formatDate(date)
 
   # author
   fname = (
       str(articleSoup.select("address")).split('firstname">')[1].split("</span>")[0]
   )
   author = (
       f"{fname} "
       + str(articleSoup.select("address")).split('lastname">')[1].split("</span>")[0]
   )
 
 
   # Get image source (big image from article page)
   src = articleSoup.select("img")[1]
   src = (
       str(src)
       .replace("'", '"')
       .split(' src="')[1]
       .split('" title="')[0]
       .replace("&amp;", "&")
   )
 
   # replace title spaces for the image title
   img_title = imgTitleFormat(title)
 
   bucket = storage.bucket()
   token = ""
 
   if title not in ref_list:
       ref_list.append(title)
       open_temp = open(log_file_path, "a")
       # use images from folder to upload to storage
       with open(f"{img_path}/{img_title}", "wb") as img:
           img.write(requests.get(src).content)
           blob = bucket.blob(f"GB/{img_title}")
           token = uuid4()
           metadata = {"firebaseStorageDownloadTokens": token}
           blob.upload_from_filename(f"{img_path}/{img_title}")
 
       storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/GB%2F{img_title}?alt=media&token={token}"
 
       pushToDB(db_path,title,date,author,"",src,img_title,url,outlet,storage_link)
 
       open_temp.write(str(title) + "\n")
       print("GB story added to the database")
   else:
       print("Already in the database")
 

