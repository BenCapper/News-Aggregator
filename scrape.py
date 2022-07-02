from datetime import datetime
import os
import re
import requests
import json
import firebase_admin
from firebase_admin import credentials, storage, db
from bs4 import BeautifulSoup


class Article:
  def __init__(self, id, headline, date, author, preview, image, image_title, link):
      self.id = id
      self.headline = headline
      self.date = date
      self.author = author
      self.preview = preview
      self.image = image
      self.image_title = image_title
      self.link = link
   
with open ("/home/bencapper/src/News/news.json") as file:
  data = json.load(file)
cred = credentials.Certificate(data)
firebase_admin.initialize_app(cred, {
  'databaseURL': "https://news-a3e22-default-rtdb.firebaseio.com/",
  'storageBucket': "news-a3e22.appspot.com"
})
timPage = requests.get("https://timcast.com/news/").content
timSoup = BeautifulSoup(timPage, features="lxml")
articles = timSoup.find_all("div","article-block")
counter = 0
for article in articles:
   counter += 1

   # Get article date, format day if < 10
   found_date = ""
   match_pattern1 = "[0-9]{2}.[0-9]{1}.[0-9]{2}"
   match_pattern2 = "[0-9]{2}.[0-9]{2}.[0-9]{2}"
   date = article.find("div", "summary")
   date = str(date)
   found_date = re.findall(match_pattern1,date)
   found_date = str(found_date).replace(".", "-")
   found_date = found_date.lstrip("['").rstrip("']")
   if found_date == "" :
       found_date = re.findall(match_pattern2,date)
       found_date = str(found_date).replace(".", "-")
       found_date = found_date.lstrip("['").rstrip("']")
   else :
       days = found_date.split("-")
       day = f"0{days[1]}"
       found_date = f"{days[0]}-{day}-{days[2]}"

   # Get link to article
   link = article.find("a")
   link = str(link)[23:]
   link = link.split('"')
   link = link[0]

   #Isolate author name
   auth = article.find("span", "auth")
   auth = str(auth).split("|")
   auth = auth[1].split("</span>")
   author = auth[0].strip()

   #Some article blocks dont have previews
   article_preview = article.find("p")
   preview = str(article_preview)
   lines = preview.split("\t")
   first_line = ""
   for line in lines:
       if len(line) > 10:
           first_line = line

   #Use img tag to get the Headline and src location
   image = article.find("img")
   image = str(image)
   img_list = image.split(" src=")
   title = img_list[0][10:-1]
   img_link = img_list[1][1:-3]

   #download the image then upload to firestorage
   if os.path.exists("/home/bencapper/src/News/Timcast") == False:
       os.mkdir("/home/bencapper/src/News/Timcast")
   bucket = storage.bucket()
   img_title = title.replace(" ", "-")
   image_title = found_date + "_" + str(counter) + "_" + img_title
   with open(f"/home/bencapper/src/News/Timcast/tim_image_{image_title}.png", "wb") as img:
       img.write(requests.get(img_link).content)
       blob = bucket.blob(f"{image_title}.png")
       blob.upload_from_filename(f"/home/bencapper/src/News/Timcast/tim_image_{image_title}.png")

   # Push the article to firebase as json object
   title = title.replace(".", " ")
   title = title.replace(","," ")
   title = title.replace("$", " ")
   ref = db.reference(f"stories/Timcast/{found_date}/{title}")
   tim_article = Article(counter,title,found_date,author,first_line,img_link, image_title,link)
   ref.set({
       'title':title,
       'date': found_date,
       'author': author,
       'preview': first_line,
       'img_src': img_link,
       'img_name': image_title,
       'link': link
   })

   print(f"Title = {tim_article.headline}")
   print(f"Image Link = {tim_article.image}")
   print(f"Line = {tim_article.preview}")
   print(f"Author = {tim_article.author}")
   print(f"Date = {tim_article.date}")
   print(f"Link = {tim_article.link}")
   print()
   print()