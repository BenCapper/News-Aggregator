from datetime import datetime
from uuid import uuid4
import os
import requests
import datetime
import json
import firebase_admin
from firebase_admin import credentials, storage, db
from bs4 import BeautifulSoup

class Article:
 def __init__(self, headline, date, author, preview, image, image_title, link, outlet, storage_link):
     self.headline = headline
     self.date = date
     self.author = author
     self.preview = preview
     self.image = image
     self.image_title = image_title
     self.link = link
     self.outlet = outlet
     self.storage_link = storage_link


path = "/home/bencapper/src/News-Aggregator/scripts/news.json"
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"

def initialise(path, db_url, bucket):
    with open (path) as file:
        data = json.load(file)
        cred = credentials.Certificate(data)
    firebase_admin.initialize_app(cred, {
        'databaseURL': db_url,
        'storageBucket': bucket
    })


initialise(path,db_url,bucket)
gbPage = requests.get("https://www.gbnews.uk/tag/news").content
gbSoup = BeautifulSoup(gbPage, features="lxml")
articles = gbSoup.find_all("div","content")[:-2]
for article in articles:
    images = article.select("img")

    for i in images:
        img_src = str(i).split(' src="')[1].split(' title="')[0].split("?image")[0]
        title = str(i).split(' title="')[1].split('" width')[0]
        img_name = title.replace(" ", "-")
        
        if os.path.exists("/home/bencapper/src/News-Aggregator/scripts/GB") == False:
            os.mkdir("/home/bencapper/src/News-Aggregator/scripts/GB")
        bucket = storage.bucket()
        img_title = title.replace(".", " ").replace("‘","'").replace("’", "'").replace(","," ").replace("£", " ").replace("&amp;", "and").replace("  ", " ").replace(" ", "-")
        img_title += ".jpg"
        token = ""

        today = datetime.date.today().strftime('%m-%d-%y')

        with open(f"/home/bencapper/src/News-Aggregator/scripts/GB/{img_title}", "wb") as img:
            img.write(requests.get(img_src).content)
            blob = bucket.blob(f"{img_title}")
            token = uuid4()
            metadata = {'firebaseStorageDownloadTokens': token}
 
            blob.upload_from_filename(f"/home/bencapper/src/News-Aggregator/scripts/GB/{img_title}")

        storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/{img_title}?alt=media&token={token}"
        
        

        title = title.replace(".", " ").replace("‘","'").replace("’", "'").replace(","," ").replace("£", " ").replace("&amp;", "and").replace("  ", " ")


        links = article.select("a")
        art_link = ""
        for link in links: # loop to match title, add right one to article
            article_link = str(link).split('a class="" href="')[1].split('" itemprop=')[0]
            if article_link == img_title:
                art_link = article_link
        articlePage = requests.get(article_link).content
        articleSoup = BeautifulSoup(articlePage, features="lxml")
        article_date = str(articleSoup.find("div","meta")).split('="Published')[1].split("-")[0].split('day ')[1]
        day = article_date.split(" ")[0]
        month = article_date.split(" ")[1]
        year = article_date.split(" ")[2][2:]
        if month == "January":
            month = "01"
        if month == "February":
            month = "02"
        if month == "March":
            month = "03"
        if month == "April":
            month = "04"
        if month == "May":
            month = "05"
        if month == "June":
            month = "06"
        if month == "July":
            month = "07"
        if month == "August":
            month = "08"
        if month == "September":
            month = "09"
        if month == "October":
            month = "10"
        if month == "November":
            month = "11"
        if month == "December":
            month = "12"
        article_date = f"{month}-{day}-{year}"
        article_auth_first = str(articleSoup.find("address","name")).split('firstname">')[1].split('</span>')[0]
        article_auth_last = str(articleSoup.find("address","name")).split('lastname">')[1].split('</span>')[0]
        article_auth = f"{article_auth_first} {article_auth_last}"

        ref = db.reference(f"stories/GB/{article_date}/{title}")
        gb_article = Article(title,article_date,article_auth,"",img_src,img_title, art_link,"www.GBNews.uk",storage_link)
        ref.set({
            'title':title,
            'date': article_date,
            'author': article_auth,
            'preview': "",
            'img_src': img_src,
            'img_name': img_title,
            'link': article_link,
            'outlet': "www.GBNews.uk",
            'storage_link': storage_link
        })
        print(title)
        print(today)
        print(img_src)
        print(img_title)
        print(art_link)
        print(storage_link)

