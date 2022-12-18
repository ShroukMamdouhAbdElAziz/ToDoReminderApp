# ToDoReminderApp

A Todo list app with location reminders that remind the user to do something when he reaches a specific location.

The app will require the user to create an account and login to set and access reminders.

1. Create a Login screen to ask users to login using an email address or a Google account.  Upon successful login, navigate the user to the Reminders screen.
If there is no account, the app should navigate to a Register screen.

2. Create a Register screen to allow a user to register using an email address or a Google account.

3. Create a screen that displays the reminders retrieved from local storage. If there are no reminders, display a   "No Data"  indicator.  If there are any errors, display an error message.

4. Create a screen that shows a map with the user's current location and asks the user to select a point of interest to create a reminder.

5. Create a screen to add a reminder when a user reaches the selected location.  Each reminder should include
    a. title
    b. description
    c. selected location
    
6. Reminder data should be saved to local storage.

7. For each reminder, create a geofencing request in the background that fires up a notification when the user enters the geofencing area.
    

#Dependencies

1. A created project on Firebase console.
2. A created project on Google console.


#Tools & Technologies Used

   Kotlin.
 
   FirebaseUI Authentication - FirebaseUI provides a drop-in auth solution that handles the UI flows for signing
  
   Google Maps & Geofencing
   
   BroadCastReceiver
   
   RecyclerView
   
   LiveData & MVVM
   
   Coroutine
   
   test the ViewModels and LiveData objects 
   
   Test DAO (Data Access Object) and Repository classes.
   
   Use Espresso and Mockito to test each screen of the app:
   
   
   #ScreenShots
    
    #Login Screen
    
  ![Screenshot_2022-12-18-23-43-44-54_c3d04dcd0dbe6e67b5f0559813e08ab1](https://user-images.githubusercontent.com/104698688/208321193-948d268e-acc6-42b4-bb26-bc4496bec3da.jpg)
  
  
  ![Screenshot_2022-12-18-23-46-21-44_c3d04dcd0dbe6e67b5f0559813e08ab1](https://user-images.githubusercontent.com/104698688/208321630-577a3c3e-72ad-44b6-890b-9e76a0a7d9c2.jpg)

  
  ![Screenshot_2022-12-18-23-46-32-57_c3d04dcd0dbe6e67b5f0559813e08ab1](https://user-images.githubusercontent.com/104698688/208321395-37fbb3f1-13df-45bb-9770-5317f1deff8a.jpg)
  
    #Sign up screen
    
  ![Screenshot_2022-12-18-23-48-35-64_c3d04dcd0dbe6e67b5f0559813e08ab1](https://user-images.githubusercontent.com/104698688/208321427-25a88744-e641-4fb6-b23d-b84a7847d4f6.jpg)
  
    
    #Add reminder item Flow
    
    
    
![Screenshot_2022-12-18-23-49-38-70_c3d04dcd0dbe6e67b5f0559813e08ab1](https://user-images.githubusercontent.com/104698688/208321723-9bf72640-14d5-4d14-8495-ba143bc7a087.jpg)



![Screenshot_2022-12-18-23-49-45-03](https://user-images.githubusercontent.com/104698688/208321729-3ffa5174-bf24-419a-819a-8651defa8b04.jpg)


![Screenshot_2022-12-18-23-49-54-00](https://user-images.githubusercontent.com/104698688/208321739-899e5047-8061-469f-a672-13d35ace6ddf.jpg)


 ![Screenshot_2022-12-19-00-04-23-06_c3d04dcd0dbe6e67b5f0559813e08ab1](https://user-images.githubusercontent.com/104698688/208321931-4dec4a08-aca9-4dc4-af5b-fabf273887cb.jpg)
 
 
 ![Screenshot_2022-12-19-00-04-54-42_c3d04dcd0dbe6e67b5f0559813e08ab1](https://user-images.githubusercontent.com/104698688/208321949-a0ff6744-8942-49bc-993e-50732597480c.jpg)
 
 
![Screenshot_2022-12-19-00-05-00-16](https://user-images.githubusercontent.com/104698688/208321958-75dbba5d-e7ea-4112-91fd-fb07491b9522.jpg)

   
![Screenshot_2022-12-19-00-05-05-02_c3d04dcd0dbe6e67b5f0559813e08ab1](https://user-images.githubusercontent.com/104698688/208321941-776fcaed-e60d-4300-a0d9-5e035aa90e26.jpg)

    
  
 
