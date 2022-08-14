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

def jsonFolder(log_folder_path):
   if os.path.exists(log_folder_path):
       pass
   else:
       os.mkdir(log_folder_path)

def dumpJson(json_dump_path, data):
    with open(json_dump_path, 'w') as f:
        json.dump(data, f, indent=4)

def appendJson(json_dump_path, data):
    with open(json_dump_path, 'r+') as file:
        file_data = json.load(file)
        file_data['articles'].append(data)
        file.seek(0)
        json.dump(file_data, file, indent=4)

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

def getHour():
    timenow = datetime.now()
    return timenow.hour
 
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

def titleDeFormat(string):
   string = (
       string.replace("(dot)", ".")
       .replace("(pc)", "%")
       .replace("(plus)", "+")
       .replace("(colon)", ":")
       .replace("(hash)", "#")
       .replace("(quest)", "?")
       .replace("(comma)", ",")
       .replace("(USD)", "$")
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

def dumpFolder(dump_path):
   if os.path.exists(dump_path) == False:
       os.mkdir(dump_path)
 
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

def pushDoubleToDB(
   db_path, titlehead, title1, date1, img_name1, link1, outlet1, storage_link1,
    title2, date2, img_name2, link2, outlet2, storage_link2 ,order
):
   ref = db.reference(f"{db_path}/{date1}/{titlehead}/{title1}")
   ref.set(
       {
           "titlehead": titlehead,
           "title1": title1,
           "date1": date1,
           "img_name1": img_name1,
           "link1": link1,
           "outlet1": outlet1,
           "storage_link1": storage_link1,
           "title2": title2,
           "date2": date2,
           "img_name2": img_name2,
           "link2": link2,
           "outlet2": outlet2,
           "storage_link2": storage_link2,
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
