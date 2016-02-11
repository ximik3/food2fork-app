# Food2Fork recipes application

Test application to apply for position Junior Android Engineer at http://teamvoy.com/ .

Completion time: ~11 days

Original task requirements:

    Implement an app with next functionality:

    As a user I can view a list/grid of trending/top rated recipes with a short info (title, image, etc)
    As a user I can select a recipe from the list and view details about it
    As a user I can do search a recipe
    Fetch all necessary info from the remote server by the url: http://food2fork.com/about/api
    

This App is made according to Android Restful Design Pattern with ContentProvider (Option B)
presented on Google I/O 2010 (https://www.youtube.com/watch?v=xHXn3Kg2IQE). 

><p><b>Activity</b> <--> <b>ContentProvider</b> <--> <b>REST Web Service</b></p>

The idea is to connect to RESTful web services via ContentProvider API, as they hassimilar CRUD interfaces.
Activity send queries to ContentProvider and get data available in database.
ContentProvider starts background service, update database with new data and
notify cursors when new data is available.
