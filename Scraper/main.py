'''
Created on Apr 15, 2014

@author: Dan

SERVER: data.cs.purdue.edu:9012
DB: data.cs.purdue.edu:50399
'''

import string, urllib2, re, sys
import httplib
#import MySQLdb

def getPageRecipes(letter, page):
    recipes = []
    response = urllib2.urlopen("http://www.food.com/browse/allrecipes/?letter=%c&pg=%d" % (letter, page))
    page_source = response.read()
    page_recipes = page_source.split("<h3>"+letter.upper()+"</h3>")[1].split("<ul class=\"list\">")[1]
    page_recipes = re.sub("</(.+)>", "", page_recipes)
    page_recipes = page_recipes.split("<li>")
    for recipe in page_recipes:
        recipe = re.findall("<a href=\"(.+)\" title", recipe)
        recipes.append(recipe)
    response.close()
    return recipes[1:]
    
def getItems(url):
    photo = ""
    ingredients = []
    directions = []
    url = url[0]
    response = urllib2.urlopen(url)
    page_source = response.read()
    
    title = page_source.split("<h3 class=\"title\">")[1].split("</h3>")[0]
    title = "".join(title.split("&amp;"))
    title = "".join(title.split("Nutritional Facts for"))
    title = " ".join(title.split())
    
    page_time = page_source.split("<div class=\"ct-e\">")[1].split("</div>")[0]
    page_time = re.sub("<(\D+)>", "", page_time).split("</h3>")[0].split(">")[2].split("<")[0]
    page_time = re.sub("\D+", "", page_time)
    time = int(page_time.strip())

    if (re.search("This recipe has no photos", page_source)):
        photo = "no photo"
    else:
        page_photo = page_source.split("class=\"smallPageImage")[1]
        photo = page_photo.split("src=\"")[1].split("\"")[0]
        
    page_ingredients = page_source.split("<h2>Ingredients:</h2>")[1].split("<ul>")[1].split("</div>")[0]
    page_ingredients = re.sub("</(\w+)>", "", page_ingredients)
    page_ingredients = page_ingredients.split("<li class=\"ingredient\"  itemprop=\"ingredients\">")
    i = True
    for ingredient in page_ingredients:
        if i is True:
            i = False
        else:
            ingredient = ingredient.split("<span class=\"value\">")[1]
            ingredient = re.sub("<span class=\"type\">", "", ingredient)
            ingredient = re.sub("<span class=\"name\">", "", ingredient)
            ingredient = re.sub("<a href=\"(.+)\">", "", ingredient)
            ingredient = ingredient.strip()
            ingredient = " ".join(ingredient.split())
            ingredients.append(ingredient)
            
    page_directions = page_source.split("<h2>Directions:</h2>")[1].split("<span class=\"instructions\"  itemprop=\"recipeInstructions\">")[1]
    page_directions = page_directions.split("<ol>")[1].split("</ol>")[0]
    for direction in page_directions.split("<li><div class=\"num\">"):
        direction = re.sub("</(\w+)>", "", direction)
        direction = re.sub("<(.+)>", "", direction)
        direction = direction.strip()
        direction = " ".join(direction.split())
        directions.append(direction)
        
    response.close()
    return title, time, photo, ingredients, directions[1:]
    
def sendData(title, time, photo, ingredients, directions):
    directions = str(directions).replace("\\\"", "'")
    ingredients = str(ingredients).replace("\\\"", "'")
    directions = str(directions).replace("\\\"", "\"")
    ingredients = str(ingredients).replace("\\\"", "\"")
    
    directions = removeTags(directions)
    ingredients = removeTags(ingredients)
    title = removeTags(title)
    photo = removeTags(photo)
    
    conn = httplib.HTTPConnection("data.cs.purdue.edu", 9012, timeout=10)
    data = "{\"option\":\"upload_recipe\", \"ingredients\":%s, \"steps\":%s, \"user_id\":-1, \"title\":\"%s\", \"time\":%d, \"photo_url\":\"%s\"}" % \
            (ingredients, directions, title, time, photo)
    print data
    try:
        conn.send(data)
        print "Sent!"
    except:
        print "Sending FAILED"
        print sys.exc_info()
    conn.close()
    
    ''' OLD DB CONNECTION '''
    '''
    db = MySQLdb.connect(host="data.cs.purdue.edu", port=50399, user="my_user", passwd="abc", db="lab6")
    cursor = db.cursor()    
    sql = "INSERT INTO recipes (user_id, steps, photo_url, ingredients, title, time) VALUES (-1, \"%s\", \"%s\", \"%s\", \"%s\", %d)" % \
            (directions, photo, ingredients, title, time)
    print sql
    try:
        print "Sending data"
        cursor.execute(sql)
        db.commit()
        print "Operation successful"
    except:
        print "SQL Insert FAILED -- Rolling back changes"
        print sys.exc_info()
        print sql
        db.rollback()
        print "Revert complete"
    db.close()
    '''
    
def removeTags(text):
    TAG_RE = re.compile(r'<[^>]+>')
    return TAG_RE.sub('', text)

if __name__ == '__main__':
    for letter in string.ascii_lowercase:
        print "------------ Letter " + str(letter)
        for page in range(5, 22):
            print "------------ Page " + str(page)
            lastItem = None
            for recipe in getPageRecipes(letter, page):
                try:
                    title, time, photo, ingredients, directions = getItems(recipe)
                    if (lastItem == title):
                        continue
                    if (photo == "no photo"):
                        continue
                    '''
                    print title
                    print "Time (minutes): " + str(time)
                    print "Photo: " + str(photo)
                    print "Ingredients (" + str(len(ingredients)) + "):"
                    for ingredient in ingredients: print ingredient
                    print "Directions (" + str(len(directions)) + "):"
                    for direction in directions: print direction
                    '''
                    lastItem = title
                except:
                    continue
                sendData(title, time, photo, ingredients, directions)
                break