import os
from queue import Empty
from uuid import uuid4
import json
 
import requests
from bs4 import BeautifulSoup
from firebase_admin import storage
 
from utils.utilities import (formatDate, imgFolder, imgTitleFormat, initialise, jsonFolder,
                            logFolder, dumpJson,appendJson)

# Set Global Variables
ref_list = []
log_file_path = "/home/bencapper/src/News-Aggregator/scripts/log/abcdone.log"
json_dump_path = "/home/bencapper/src/News-Aggregator/scripts/json/abcdump.json"
json_folder_path = "/home/bencapper/src/News-Aggregator/scripts/json/"
log_folder_path = "/home/bencapper/src/News-Aggregator/scripts/log/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
page_url = "https://abcnews.go.com/US"
img_path = "/home/bencapper/src/News/Abc"
storage_path = "https://firebasestorage.googleapis.com/v0/b/news-a3e22.appspot.com/o"
db_path = "stories"
outlet = "AbcNews.go.com"
 
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

data = {
    "title": "title3",
    "date": "date3",
    "img_src": "img_src3",
    "img_title": "img_title3",
    "link": "link3",
    "outlet": "outlet3",
    "storage_link": "storage_link3",
    "order": "order3"
}


open_temp = open(json_dump_path, "r")
read_temp = open_temp.read()
if read_temp == "" or read_temp is Empty:
    dumpJson(json_dump_path,data)
else:
    appendJson(json_dump_path,data)


