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

right_log = [ "bonginodone.log","rev.log", "timdone.log", "zerodone.log","blazedone.log", "dailycallerdone.log",
           "gbdone.log", "griptdone.log", "GWPdone.log", "pmill.log",
           "spikedone.log","breitbartdone.log"]
right_json = ["tim.json","caller.json","gb.json","spiked.json","zero.json",
           "gript.json","gwp.json","rev.json","blaze.json","bong.json","breit.json"]
left_log = ["politicodone.log","abcdone.log", "beastdone.log", "euronewsdone.log",
           "globaldone.log", "guarddone.log", "huffdone.log","rtedone.log","cbsdone.log",
           "skyukdone.log", "voxdone.log", "yahoodone.log"]
left_json = ["euron.json","global.json","guard.json","vox.json",
           "huff.json","pol.json","rte.json","yah.json",
           "abc.json","beast.json","cbs.json","sky.json"]

fullref_with_outlets = list()
right_titles = list()
left_titles = list()

for i in right_log:
   path = f"/home/bencapper/src/News-Aggregator/scripts/log/{i}"
   open_temp = open(path, "r")
   read_temp = open_temp.read()
   split = read_temp.splitlines()
   for j in split:
       j = f"{j}--{i}"
       fullref_with_outlets.append(j)
       right_titles.append(j)

# Open Left Log and add outlet to title
for i in left_log:
   path = f"/home/bencapper/src/News-Aggregator/scripts/log/{i}"
   open_temp = open(path, "r")
   read_temp = open_temp.read()
   split = read_temp.splitlines()
   for j in split:
       j = f"{j}--{i}"
       fullref_with_outlets.append(j)
       left_titles.append(j)

if os.path.exists(double_log_gb):
  open_temp = open(double_log_gb, "r")
  read_temp = open_temp.read()
  double_gb_ref = read_temp.splitlines()
else:
  os.mknod(double_log_gb)

if os.path.exists(match_log):
  open_temp = open(match_log, "r")
  read_temp = open_temp.read()
  match_ref = read_temp.splitlines()
else:
  os.mknod(match_log)

matches = list()
right_not_in_ref = list()
left_not_in_ref = list()
for title in right_titles:
   if title.split('--')[0] in match_ref:
       pass
   else:
       right_not_in_ref.append(title)
for title in left_titles:
   if title.split('--')[0] in match_ref:
       pass
   else:
       left_not_in_ref.append(title)
       
rtits = list()
ltits = list()
for title in right_not_in_ref:
   title_no_log = titleDeFormat(title).split('.log')[0]
   title_no_outlet = title.split('--')[0]
   title_formatted = titleDeFormat(title_no_outlet)
   if title_formatted in rtits:
      print(f"RIGHT TITLE = ${title_formatted}")
      print(f"RTITS = ${rtits}")
      break
   else:
      for ltitle in left_not_in_ref:
       ltitle_no_log = titleDeFormat(ltitle).split('.log')[0]
       ltitle_no_outlet = ltitle.split('--')[0]
       ltitle_formatted = titleDeFormat(ltitle_no_outlet)
       if similar(title_formatted,ltitle_formatted) > .7 and ltitle_formatted not in ltits and ltitle_formatted not in match_ref and title_formatted not in match_ref:
          matches.append(f"{title_no_log} // {ltitle_no_log}")
          rtits.append(title_formatted)
          ltits.append(ltitle_formatted)
          open_temp = open(match_log, "a")
          open_temp.write(f"{title_formatted}" + "\n")
          open_temp.write(f"{ltitle_formatted}" + "\n")
          print(f"{title_formatted} // {ltitle_formatted}")
          break
 
print(matches)
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
           title = titleDeFormat(article['title'])
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
           title = titleDeFormat(article['title'])
           if title == left_title and title != "" and title != " ":
              l_art = article
 
     if r_art and l_art:
        if r_art['link'] == l_art['link']:
           open_mlog = open(match_log,"r+")
           read_mlog = open_mlog.read()
           mlog = read_mlog.splitlines()
           print("SAME LINKS")
           if titleDeFormat(r_art['title']) in mlog or titleDeFormat(l_art['title']) in mlog:
              mlog.remove(r_art['title'])
              mlog.remove(l_art['title'])
              with open(match_log,"r+") as f:
                 f.truncate
                 for line in mlog:
                    f.write(line)
                    open_temp.write(str(line) + "\n")
 
        else:
           open_dlog = open(double_log_gb, "r")
           read_dlog = open_dlog.read()
           double_gb_ref = read_dlog.splitlines()
 
           pushDoubleToDB(
              db_path,
              titlehead,
              titleDeFormat(r_art['title']),
              r_art['date'],
              r_art['img_title'],
              r_art['link'],
              r_art["outlet"],
              r_art['storage_link'],
              titleDeFormat(l_art['title']),
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
     else:
        print("fuck")
