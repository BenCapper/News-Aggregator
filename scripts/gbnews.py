import json
import os
from uuid import uuid4

import firebase_admin
import requests
from bs4 import BeautifulSoup
from firebase_admin import credentials, db, storage


class Article:
    def __init__(
        self,
        headline,
        date,
        author,
        preview,
        image,
        image_title,
        link,
        outlet,
        storage_link,
    ):
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
    with open(path) as file:
        data = json.load(file)
        cred = credentials.Certificate(data)
    firebase_admin.initialize_app(
        cred, {"databaseURL": db_url, "storageBucket": bucket}
    )


ref_list = []
log_file_path = "/home/bencapper/src/News-Aggregator/scripts/log/gbdone.log"
log_folder_path = "/home/bencapper/src/News-Aggregator/scripts/log/"
if os.path.exists(log_folder_path):
    pass
else:
    os.mkdir(log_folder_path)

if os.path.exists(log_file_path):
    open_temp = open(log_file_path, "r")
    read_temp = open_temp.read()
    ref_list = read_temp.splitlines()
else:
    os.mknod(log_file_path)

initialise(path, db_url, bucket)
gbPage = requests.get("https://www.gbnews.uk/tag/news").content
gbSoup = BeautifulSoup(gbPage, features="lxml")
articles = gbSoup.find_all("div", "content")[:-2]
for article in articles:

    # get url
    a = article.select("a")
    url = str(a).split('href="')[1].split('" itemprop')[0]

    # get article page
    full_page = requests.get(url).content
    articleSoup = BeautifulSoup(full_page, features="lxml")

    # headline
    title = str(articleSoup.select("h1")).split('">')[1].split("</h1>")[0]

    # title with hyphon for storage
    alt_title = (
        title.replace("%", "pc")
        .replace("?", " ")
        .replace("+", " ")
        .replace(":", " ")
        .replace("+", " ")
        .replace("£", " ")
        .replace(".", " ")
        .replace("&amp;", "and")
        .replace("  ", " ")
        .replace(" ", "-")
    )

    # date
    date = str(articleSoup.select("time")).split("day ")[1].split(" -")[0].split(" ")
    day = date[0]
    month = date[1]
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
    year = date[2][2:]
    date = f"{month}-{day}-{year}"

    # author
    fname = (
        str(articleSoup.select("address")).split('firstname">')[1].split("</span>")[0]
    )
    author = (
        f"{fname} "
        + str(articleSoup.select("address")).split('lastname">')[1].split("</span>")[0]
    )

    gb_image_path = "/home/bencapper/src/News-Aggregator/scripts/GB"
    # create image folder locally
    if os.path.exists(gb_image_path) is False:
        os.mkdir(gb_image_path)

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
    img_title = (
        title.replace("%", "pc")
        .replace("?", " ")
        .replace(":", " ")
        .replace("+", " ")
        .replace("£", " ")
        .replace(".", " ")
        .replace("&amp;", "and")
        .replace("  ", " ")
        .replace(" ", "-")
    )
    img_title += ".jpg"

    bucket = storage.bucket()
    token = ""

    if alt_title not in ref_list:
        ref_list.append(title)
        open_temp = open(log_file_path, "a")
        # use images from folder to upload to storage
        with open(f"{gb_image_path}/{img_title}", "wb") as img:
            img.write(requests.get(src).content)
            blob = bucket.blob(f"GB/{img_title}")
            token = uuid4()
            metadata = {"firebaseStorageDownloadTokens": token}
            blob.upload_from_filename(f"{gb_image_path}/{img_title}")

        storage_link = f"https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o/GB%2F{img_title}?alt=media&token={token}"

        ref = db.reference(f"stories/GB/{date}/{alt_title}")
        gb_article = Article(
            title, date, author, "", src, img_title, url, "www.GBNews.uk", storage_link
        )
        ref.set(
            {
                "title": title,
                "date": date,
                "author": author,
                "preview": "",
                "img_src": src,
                "img_name": img_title,
                "link": url,
                "outlet": "www.GBNews.uk",
                "storage_link": storage_link,
            }
        )

        open_temp.write(str(alt_title) + "\n")
        print("GB story added to the database")
    else:
        print("Already in the database")
