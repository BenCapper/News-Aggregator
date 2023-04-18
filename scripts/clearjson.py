import os

json_path = "/home/bencapper/src/News-Aggregator/scripts/json"
default = """{
    "articles": [
        {},
        {}
    ]
}"""

json_list = os.listdir(json_path)
for json in json_list:
    os.chdir(json_path)
    f = open(f"{json}", "w")
    f.write(default)
    f.close()
