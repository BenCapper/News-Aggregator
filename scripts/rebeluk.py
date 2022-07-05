from datetime import datetime
import os
import re
from uuid import uuid4
import requests
import json
import firebase_admin
from firebase_admin import credentials, storage, db
from bs4 import BeautifulSoup
from timcast import Article

path = "/home/bencapper/src/News/news.json"
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
rebelPage = requests.get("https://www.rebelnews.com/tags/united_kingdom").content
rebelSoup = BeautifulSoup(rebelPage, features="lxml")
articles = rebelSoup.find_all("div","post_excerpt_horizontal.row.margintopmore")
print(f"ARTICLES = {articles}")

for article in articles:
    print(article)


