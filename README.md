# ToDoReminderApp

A Todo list app with location reminders that remind the user to do something when he reaches a specific location. The app will require the user to create an account and login to set and access reminders.


# Dependencies

1. A created project on Firebase console.
2. A create a project on Google console.

# Project Instructions

1. Create a Login screen to ask users to login using an email address or a Google account.  Upon successful login, navigate the user to the Reminders screen.   If there is no account, the app should navigate to a Register screen.
2. Create a Register screen to allow a user to register using an email address or a Google account.
3. Create a screen that displays the reminders retrieved from local storage. If there are no reminders, display a   "No Data"  indicator.  If there are any errors, display an error message.
4. Create a screen that shows a map with the user's current location and asks the user to select a point of interest to create a reminder.
5. Create a screen to add a reminder when a user reaches the selected location.  Each reminder should include
    a. title
    b. description
    c. selected location
6. Reminder data should be saved to local storage.
7. For each reminder, create a geofencing request in the background that fires up a notification when the user enters the geofencing area.
8. Provide testing for the ViewModels, Coroutines and LiveData objects.
9. Create a FakeDataSource to replace the Data Layer and test the app in isolation.
10. Use Espresso and Mockito to test each screen of the app:
    a. Test DAO (Data Access Object) and Repository classes.
    b. Add testing for the error messages.
    c. Add End-To-End testing for the Fragments navigation.

# Built With

Kotlin

FirebaseUI Authentication - FirebaseUI provides a drop-in auth solution that handles the UI flows for signing

Broadcast Receiver

Notification

Google Maps

Geofenciening

MVVM architecture design pattern

Navgraph for Nativgation

# Screen shots
![Screenshot_2022-12-11-17-54-06-40_c3d04dcd0dbe6e67b5f0559813e08ab1](https://user-images.githubusercontent.com/104698688/206915393-2ef165bf-7ac2-44e7-bfb4-7e0665e30106.jpg)

![Screenshot_2022-12-11-17-54-11-30_c3d04dcd0dbe6e67b5f0559813e08ab1](https://user-images.githubusercontent.com/104698688/206915413-35d22a0d-d4c8-4b7b-8132-dca92b4c9fce.jpg)

![Screenshot_2022-12-11-17-54-25-36_c3d04dcd0dbe6e67b5f0559813e08ab1](https://user-images.githubusercontent.com/104698688/206915422-a335a37a-a4e2-485c-bff6-97daf93ec0ce.jpg)

![Screenshot_2022-12-11-17-54-35-70_c3d04dcd0dbe6e67b5f0559813e08ab1](https://user-images.githubusercontent.com/104698688/206915688-a2882331-0470-41d0-8563-5cb1f9310e02.jpg)

![Screenshot_2022-12-11-17-54-42-73_c3d04dcd0dbe6e67b5f0559813e08ab1](https://user-images.githubusercontent.com/104698688/206915696-20079231-feec-443f-a19b-de8632e44b60.jpg)

![Screenshot_2022-12-11-17-55-08-98](https://user-images.githubusercontent.com/104698688/206915733-a920952b-7a62-4040-8d4d-015a26ce1c8b.jpg)

![Screenshot_2022-12-11-17-55-39-43_c3d04dcd0dbe6e67b5f0559813e08ab1](https://user-images.githubusercontent.com/104698688/206915749-a2846d22-3cec-46bd-b8e3-056c9a7fe010.jpg)

![Screenshot_2022-12-11-17-55-48-14_c3d04dcd0dbe6e67b5f0559813e08ab1](https://user-images.githubusercontent.com/104698688/206915759-6ffc5fdf-5277-4d26-8ac4-acfa9fc681ad.jpg)

![Screenshot_2022-12-11-17-56-17-42_c3d04dcd0dbe6e67b5f0559813e08ab1](https://user-images.githubusercontent.com/104698688/206915772-653a32ce-17bc-43bd-9091-e74fdc67bd38.jpg)

![Screenshot_2022-12-11-17-56-23-27](https://user-images.githubusercontent.com/104698688/206915777-11e83969-8757-4b42-9bf5-1975edd375e8.jpg)

![Screenshot_2022-12-11-17-56-32-03_c3d04dcd0dbe6e67b5f0559813e08ab1](https://user-images.githubusercontent.com/104698688/206915790-a64c9881-525d-4ea1-8281-d8ff10e1a1b2.jpg)

![Screenshot_2022-12-11-17-57-35-94_c3d04dcd0dbe6e67b5f0559813e08ab1](https://user-images.githubusercontent.com/104698688/206915800-3814665f-76f6-4383-ba3d-411b411ca115.jpg)

![Screenshot_2022-12-11-17-59-17-86_c3d04dcd0dbe6e67b5f0559813e08ab1](https://user-images.githubusercontent.com/104698688/206915809-5808f283-a077-425e-bc8b-7ba9f525041d.jpg)

![Screenshot_2022-12-11-17-59-35-23_c3d04dcd0dbe6e67b5f0559813e08ab1](https://user-images.githubusercontent.com/104698688/206915814-3eedb32f-1ebd-4e93-bf6b-bbf66f3d649d.jpg)




\






