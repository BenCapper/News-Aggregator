from uuid import uuid4
import os
from firebase_admin import storage
from utils.utilities import (initialise, cutOffDate)


cutdate = cutOffDate()
print(cutdate)
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"
image_path = "/home/bencapper/src/News/"
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"


initialise(json_path, db_url, bucket)


bucket = storage.bucket()
token = ""
outlet_list = os.listdir(image_path)
for outlet in outlet_list:
    if os.path.isfile(outlet):
        pass
    else:
        try:
            img_list = os.listdir(f"{image_path}{outlet}/{cutdate}")
            for img in img_list:
                try:
                    print(outlet)
                    print(cutdate)
                    print(img)
                    blob = bucket.blob(f"{outlet}/{cutdate}/{img}")
                    token = uuid4()
                    metadata = {"firebaseStorageDownloadTokens": token}
                    blob.delete()
                    os.remove(f"{image_path}{outlet}/{cutdate}/{img}")
                    print("Found and Deleted Cloud and Local")
                except:
                    print(outlet)
                    print(cutdate)
                    print(img)
                    os.remove(f"{image_path}{outlet}/{cutdate}/{img}")
                    print("Found and Deleted Local")
        except:
            print("folder doesnt exist")


