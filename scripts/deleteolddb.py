from utils.utilities import initialise, cutOffDate
from firebase_admin import db


cutdate = cutOffDate()
print(cutdate)
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"
image_path = "/home/bencapper/src/News/"
db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"


initialise(json_path, db_url, bucket)

ref = db.reference(f"stories/{cutOffDate}")
ref.delete()
