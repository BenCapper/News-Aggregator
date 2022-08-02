import json
import os
 
import firebase_admin
import requests
from bs4 import BeautifulSoup
from difflib import SequenceMatcher
from firebase_admin import credentials, db
from datetime import datetime
 
 
def logFolder(log_folder_path):
   if os.path.exists(log_folder_path):
       pass
   else:
       os.mkdir(log_folder_path)
 
 
def initialise(json_path, db_url, bucket):
   with open(json_path) as file:
       data = json.load(file)
       cred = credentials.Certificate(data)
   firebase_admin.initialize_app(
       cred, {"databaseURL": db_url, "storageBucket": bucket}
   )
 
 
def pageSoup(page_url):
   page = requests.get(page_url).content
   soup = BeautifulSoup(page, features="lxml")
   return soup

def similar(a,b):
    return SequenceMatcher(None, a, b).ratio() 
 
def titleFormat(string):
   string = (
       string.replace(".", "(dot)")
       .replace("%", "(pc)")
       .replace("+", "(plus)")
       .replace(":", "(colon)")
       .replace("#", "(hash)")
       .replace("?", "(quest)")
       .replace(",", "(comma)")
       .replace("$", "(USD)")
       .replace("&amp;", "and")
       .replace("/","-")
       .replace("  ", " ")
   )
   return string
 
 
def imgTitleFormat(string):
   string = (
       string.replace(".", "(dot)")
       .replace("%", "(pc)")
       .replace("+", "(plus)")
       .replace(":", "(colon)")
       .replace("#", "(hash)")
       .replace("?", "(quest)")
       .replace(",", "(comma)")
       .replace("$", "(USD)")
       .replace("&amp;", "and")
       .replace("  ", " ")
       .replace(" ", "-")
   )
   string += ".png"
   return string
 
 
def imgFolder(img_path):
   if os.path.exists(img_path) == False:
       os.mkdir(img_path)
 
 
def pushToDB(
   db_path, title, date, img_src, img_name, link, outlet, storage_link, order
):
   ref = db.reference(f"{db_path}/{date}/{title}")
   ref.set(
       {
           "title": title,
           "date": date,
           "img_src": img_src,
           "img_name": img_name,
           "link": link,
           "outlet": outlet,
           "storage_link": storage_link,
           "order": order
       }
   )

def pushToDbNoImg(
   db_path, title, date, link, outlet, storage_link, order
):
   ref = db.reference(f"{db_path}/{date}/{title}")
   ref.set(
       {
           "title": title,
           "date": date,
           "link": link,
           "outlet": outlet,
           "storage_link": storage_link,
           "order": order
       }
   )
 
def addYearAndFormat(date):
   dates = date.split('/')
   month = dates[0]
   day = dates[1][:-1]
   if len(day) == 1:
       day = f"0{day}"
   if len(month) == 1:
       month = f"0{month}"
   year = datetime.today().strftime('%y')
   return f"{month}-{day}-{year}"
 
def formatDate(date):
   day = date[0]
   month = date[1]
   if month in "January":
       month = "01"
   if month in "February":
       month = "02"
   if month in "March":
       month = "03"
   if month in "April":
       month = "04"
   if month in "May":
       month = "05"
   if month in "June":
       month = "06"
   if month in "July":
       month = "07"
   if month in "August":
       month = "08"
   if month in "September":
       month = "09"
   if month in "October":
       month = "10"
   if month in "November":
       month = "11"
   if month in "December":
       month = "12"
   if len(day) < 2:
       day = f"0{day}"
   year = date[2][2:]
   return f"{month}-{day}-{year}"
