import os
import json
from utils.utilities import jsonFolder, similar, titleDeFormat, initialise, pushDoubleToDB, logFolder, titleFormat

double_log_gb = "/home/bencapper/src/News-Aggregator/scripts/doublelog/double.log"
double_gb_ref = list()

match_log = "/home/bencapper/src/News-Aggregator/scripts/doublelog/matches.log"
match_ref = list()

json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"

db_url = "https://news-a3e22-default-rtdb.firebaseio.com/"
bucket = "news-a3e22.appspot.com"
db_path = "doubles"

json_folder_path = "/home/bencapper/src/News-Aggregator/scripts/json/"
json_path = "/home/bencapper/src/News-Aggregator/scripts/news.json"

logFolder(double_log_gb)
jsonFolder(json_folder_path)
initialise(json_path, db_url, bucket)

right_log = ["blazedone.log","bonginodone.log","breitbartdone.log", "dailycallerdone.log",
            "gbdone.log", "griptdone.log", "GWPdone.log", "pmill.log",
            "rev.log", "spikedone.log", "timdone.log", "zerodone.log"]
right_json = ["blaze.json","bong.json","breit.json","caller.json","gb.json",
            "gript.json","gwp.json","rev.json","spiked.json","tim.json","zero.json"]
left_log = ["abcdone.log", "beastdone.log", "cbsdone.log", "euronewsdone.log",
            "globaldone.log", "guarddone.log", "huffdone.log", "politicodone.log",
            "rtedone.log", "skyukdone.log", "voxdone.log", "yahoodone.log"]
left_json = ["abc.json","beast.json","cbs.json","euron.json","global.json","guard.json",
            "huff.json","pol.json","rte.json","sky.json","vox.json","yah.json"]

# Open Right Log and add outlet to title
right_ref = list()
for i in right_log:
    path = f"/home/bencapper/src/News-Aggregator/scripts/log/{i}"
    open_temp = open(path, "r")
    read_temp = open_temp.read()
    split = read_temp.splitlines()
    for j in split:
        j = f"{j}--{i}"
        right_ref.append(j)

# Open Left Log and add outlet to title
left_ref = list()
for i in left_log:
    path = f"/home/bencapper/src/News-Aggregator/scripts/log/{i}"
    open_temp = open(path, "r")
    read_temp = open_temp.read()
    split = read_temp.splitlines()
    for j in split:
        j = f"{j}--{i}"
        left_ref.append(j)

# Create Double Log file if doesnt exist
# Split into lines
if os.path.exists(double_log_gb):
   open_temp = open(double_log_gb, "r")
   read_temp = open_temp.read()
   double_gb_ref = read_temp.splitlines()
else:
   os.mknod(double_log_gb)

if os.path.exists(match_log):
   open_temp = open(match_log, "r")
   read_temp = open_temp.read()
   double_gb_ref = read_temp.splitlines()
else:
   os.mknod(match_log)


right_titles = list()
left_titles = list()

# Copy Right Ref List into another var
for line in right_ref:
    right_titles.append(line)

# Copy Left Ref List into another var
for line in left_ref:
    left_titles.append(line)

matches = list()
# Nested loop to match titles
# Remove the outlet name
# Connect titles, readd outlets
# Add titles that match to matches list
left_matches = list()
right_matches = list()
for i in left_titles:
   loutlet = titleDeFormat(i).split('.log')[0]
   lnoout = i.split('--')[0]
   lformat = titleDeFormat(lnoout)
   for j in right_titles:
      routlet = titleDeFormat(j).split('.log')[0]
      rnoout = j.split('--')[0]
      rformat = titleDeFormat(rnoout)
      sim = False
      for m in matches:
         if similar(m, f"{routlet} // {loutlet}") > .7:
            sim = True
            break
      for line in match_ref:
         if rformat in line or lformat in line:
            sim = True
            break
         
      if similar(rformat, lformat) > .59 and sim is False and similar(rformat, lformat) < .97 and rformat not in match_ref and lformat not in match_ref: 
         matches.append(f"{routlet} // {loutlet}")
         open_temp = open(match_log, "a")
         open_temp.write(f"{rformat}" + "\n")
         open_temp.write(f"{lformat}" + "\n")
         print(f"{routlet} // {loutlet}")




# For each title match
for match in matches:
      # Outlet name - No Title
      right_outlet = match.split('//')[0].split('--')[1].lstrip().rstrip()
      # Article Title - No Outlet
      right_title = match.split('//')[0].split('--')[0].lstrip().rstrip()
      # Outlet name - No Title
      left_outlet = match.split('//')[1].split('--')[1].lstrip().rstrip()
      # Article Title - No Outlet
      left_title = match.split('//')[1].lstrip().rstrip().split('--')[0].lstrip().rstrip()


      title_words = list()

      for word in right_title.split(' '):
         rightword = word
         for word2 in left_title.split(' '):
            if rightword.lower() == word2.lower():
               title_words.append(rightword)
      
      stoplist = ["a","an","and","are","as","at",
            "be", "by", "for", "from", "has", "in", "it",
            "of", "on", "that", "the","to","was", "were",
            "will", "with", "should", "but", "could", "did", "is"]

      for word in title_words:
         if word in stoplist:
            while word in title_words:
               title_words.remove(word)
      count = 0
      for word in title_words:
         if word[0].islower():
            title_words[count] = word.capitalize()
         count += 1
      titlehead = ""
      for word in title_words:
         titlehead = f"{titlehead} {word}"
      titlehead = f"{titlehead} - {right_outlet} - {left_outlet}"
      titlehead = titlehead.lstrip().rstrip()
      if titlehead in double_gb_ref:
         print("Double Article Already in DB")
         pass
      else:
         right_articles = list()
         for i in right_json:
            articles = list()
            path = f"/home/bencapper/src/News-Aggregator/scripts/json/{i}"
            open_json = open(path, "r")
            read_json = json.load(open_json)
            articles = read_json['articles']
            for article in articles:
                right_articles.append(article)

         r_art = dict()
         for article in right_articles:
            if article:
               title = article['title']
               if title == right_title and title != "" and title != " ":
                  r_art = article

         left_articles = list()
         for i in left_json:
            articles = list()
            path = f"/home/bencapper/src/News-Aggregator/scripts/json/{i}"
            open_json = open(path, "r")
            read_json = json.load(open_json)
            articles = read_json['articles']
            for article in articles:
                left_articles.append(article)

         l_art = dict()
         for article in left_articles:
            if article:
               title = article['title']
               if title == left_title and title != "" and title != " ":
                  l_art = article

         if r_art and l_art:
            open_dlog = open(double_log_gb, "r")
            read_dlog = open_dlog.read()
            double_gb_ref = read_dlog.splitlines()

            pushDoubleToDB(
               db_path,
               titlehead,
               r_art['title'],
               r_art['date'],
               r_art['img_title'],
               r_art['link'],
               r_art["outlet"],
               r_art['storage_link'],
               l_art['title'],
               l_art['date'],
               l_art['img_title'],
               l_art['link'],
               l_art["outlet"],
               l_art['storage_link'],
               l_art['order']
            )
            double_gb_ref.append(titlehead)
            open_temp = open(double_log_gb, "a")
            open_temp.write(str(titlehead) + "\n")
            print("Double Article Added to DB")