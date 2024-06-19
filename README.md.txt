# track´s fitnesstracker

## View the Video Demo: 
[track´s video demo](https://youtu.be/u22MDxSk6uY)


# Description
The reason I opted for creating a fitness tracker which can keep track of one´s workout plans, training dates as well as provide you with some statistics to help you keep track of how you are doing over time was because I was fed up with managing my training data via OneNote, which would at times not synchronize the data properly and did not provide me with a way of seeing how my strength increases over time. track´s as a easy to use application that still has advantages like keeping your data up to date across devices by implementing cloud storage is my approach to handle the problems I ran into with other tools over the course of the last few months. 

## In General

**Logo**
The logo for the application was created using 'FreeLogoDesign' with the goal of creating a minimalistic design which incorporates the dominant colors of the application: light blue (#A9D8FF) and dark blue (#004F8B). 

**Use of generative AI (ChatGPT 4o)**
	Throughout the development of this application ChatGPT 4o aided me first and foremost in debugging existing sourcecode using logging and providing me with new insights of how different classes can be structured to fit together seamlessly in the end. 
Altough it was useful at times for this it I noticed it quickly reaching limitations when trying to finding structural errors, where hallucinations as well as wrong reasoning occured on multiple occasions. 

**Testing**
	Testing for this application was performed on multiple devices, making sure there were no crashes and data could only be inputted so that it would not do harm somewhere in an operation. This was mainly done through trial and error, fixing the cause everytime an application crashed unexpectedly until those crashes did not occur anymore when using the app.
	
## First steps

When the decision was made to create an application that handles a users workout plans and their exercises as well as creates some dynamic statistics for them I created a concept in OneNote to wrap my head around a rough idea of what the application´s goal was and how I would set up the structure later on. 

While the concept was one also included the ability to add and store pictures, the 'final' product basically consists of these functions : 
+ creating and managing a users account, including:
	+ changing their email address and password
	+ verifying their email address by sending (and resending) a verification email
	+ data being stored user-specific
	+ delete their account
+ allowing the user to: 
	+ set up their own workout plans
	+ add exercises inside their workout plans, when creating an exercise the user can choose:
		+  the amount of sets to be added
		+ wether or not the exercise should get added to all workout plans (MultiTrain)
		+ assign values to weights, reps, name and notes that will get saved online in Firestore, allowing them to keep their data when switching devices 
	+ keeping track of how they are doing in the statistics menu, in which they can 
		+ switch between different timeframes 
		+ manage wether all available data or just the data of  a specific workout plan should be displayed
		+ switch between different metrics for the chart-values, including the max weight and average weight per rep

## Login and Registration

## Home

### Displaying the various workout plans
	
When MainActivity is opened, the user should be presented with a list of all of his workout plans which take him to the specific exercises when he clicks on one of them. Also, the date on which the exercise was last trained is displayed below each workout plan so the user does not have to remember what he trained last time but can just see it when opening the app. 

To achieve this, several steps are taken by the program when the Activity is opened: 
+ UI is set up by calling setContentView(R.layout.activity_main)
+ all of the UI components are initialized and the buttons are set with onClickListeners, which mean that some specific code will be executed when they are clicked (this will be taken as granted from now on, including when describing the function of the other classes)
+ an instance of WorkoutPlanViewModel is created, this will act as an intermediary between the UI and the WorkoutPlanRepository, which handles database operations related to the workout plans
+ the RecyclerView is set up with a LinearLayoutManager, one entry in this RecyclerView will later represent one workout plan
+ the adapter for the RecyclerView is set to WorkoutPlanAdapter, which will manage the data and provide the views for the RecyclerView
+ changes in Data that can occur when a workout plan is added, deleted or is changed in its name attribute are monitored by setting up an observer for the LiveData object 'getAllWorkoutPlans' from the ViewModel
	+ when the observed data changes, the attached Observer is notified and the RecyclerView refreshes with the revised list of workout plans so the data displayed to the user is always up to date
	
		
### **Adding a new workout plan**

 The process of adding a new workout plan is started when a user clicks on the button with the plus icon near the bottom of the screen and looks like this: 
+ Activity is changed to AddWorkoutPlanActivity, where an input field is set up
	+ the focus is automatically set on this field and the keyboard is opened
+ when pressing the enter key or hitting the 'Save'-button, the workoutplan is saved to the Firebase and the RecyclerView updates to also include the new workout plan as the listener is triggered (as described above)

### **Deleting & changing the name of a workout plan**
Managing a workoutplan is started by a click and hold on the wanted workout plan element in the RecyclerView
The database is updated according to the action taken by the user, this includes: 
+ deleting a workout plan, where all of the TrainingSessions and Exercises associated with the workoutPlanId are also deleted from Firebase
+ changing the name of a workout plan, which just changes the value of the 'name'-field associated with the workoutPlanId in Firebase

### **Hamburger menu inside the top bar**
The hamburger menu was created as a means to display some lesser used functionality without taking up too much screenspace and is an advancement of the original design choice, where the idea was to create a bottom navigation menu housing the 'Settings' as well as the 'About' elements.
The menu is powered by an ActionBarDrawerToggle.


## Inside each workout plan
 ### **Displaying the various exercises**
All of the exercises for the specific workout plan are retrieved from Firebase using the ExerciseRepository and then displayed in a Recyclerview. For improved handling and readability of large workoutplans the grid size was altered to two, so two exercises will get displayed side by side. 
A click listener is added to each item so when clicking on it, the EditExerciseActivity can be fed the correct values. 

As I personally do not perform each exercise in my workout plan in one training session and also want to keep track of which exercises I have already done in this session opted for a feature which sets the background of each exercise based on the date on which it was last trained. This assumes that you will not perform a workout plan twice over the course of 24 hours. 

	Trained this session -> background color green
	Trained last session but not this session -> background color very light grey (almost white)
	Trained neither last nor this session -> background color grey

The benefit of this is that when a user sets the focus on different exercises alternating between trainings it helps to keep track of which focus group was done last time as well as which exercises have not been trained the last time this workout plan was performed and should maybe be trained today. 
	
### **Adding a new exercise** 
Creating a new exercise is handled by the NewExerciseActivity Activity, in which a user can input and alter the exercise name, weights and reps for each set as well as add more sets and enable MultiTrain by clicking a switch, which solves the problem of having a exercise that is trained on multiple training days (e.g. abs training) and therefore out of date because when using a normal notes application. Enabling MultiTrain means that a boolean inside the Exercise class, which handles all attributes which a exercise has and contains Getters and Setters for of these attributes, will get set to true, which then plays together with a database query inside of the ExerciseRepository, where all exercises are loaded into the RecyclerView after adding the workoutplan-specific exercises aided by fetchExercises. The insertion of duplicate exercises into the RecyclerView is prevented by checking wether a similar exercise already exists inside the RecyclerView before inserting it. 
When saving the exercise and the user forgot to input either the reps or the weight for one field (not both!) the missing value will be set to zero so all user input acutally gets added to the database. 
The new exercise is not passed directly to the ExerciseRepository for insertion buth rather 'given back' to the WorkoutPlanActivity which intern passes it to the Repository through the ExerciseViewModel. 

### **Editing a exercise**
Done in EditExerciseActivity, where the the user can input new values or change his notes. When opened, the values are automatically inserted into the fields, the first weight´s field as well as the keyboard are opened and the original value from the field is deleted and set as its hint as a method of decreasing unneccesary actions needed by the user to reach the goal of quickly editing some values inside the exercise. Another function is the enter key serving as a tab key in a sense, enabling the user to quickly toggle between his entries. 

Upon clicking save, like with NewExerciseActicity, an Intent is passed to WorkoutPlanActivity, where a TrainingSession is created with the values from the current Exercise. This TrainingSession is only created once and then, for each time the exercise is trained (saved), specific metrics such as the maximum weight for a set and the date are added to specific list attributes. If an Exercise is saved twice in one day, only the recent values will get saved and the 'old' ones from that day will get overwritten. This check is performed inside the onActivityResult method inside WorkoutPlanActivity, which receives the data from EditExerciseActivity. 

When the user clicks the delete button, a Dialog window will open to make sure it was not an accidental click, after which the exercise will get deleted from the exercises database as well as all TrainingSessions with a matching exerciseId.

By using change listeners, the RecyclerView refreshes with the new data when the data of a exercise was saved or changed.

## Statistics

### Displaying charts using RecyclerView and MPAndroidChart
Very similarly to the displaying of workout plans and exercises, the statistics menu also utilizes a RecyclerView, in this case to show charts populated with data about specific exercises over a set timeframe in a certain way. 
The RecyclerView is populated with instances of TrainingSessionViewHolder which intern extends RecyclerView.ViewHolder and binds data to the charts in reference to the type passed (max weight/avg weight per rep/total weight). The charts are created using the MPAndroidChart library which is also avaiable on Github and was created by Philipp Jahoda. Each TrainingSessionViewHolder creates one LineChart using the library, which is then populated with passed values and customized a bit appearance-wise. For displaying the name, each RecyclerView item also holds a TextView that is set to the name of the passed exercise.

### Displaying data based on spinner values
Using the spinners, the user is enabled to change values about which exercises, timeframe and type of data to be displayed in the chart is displayed. This works by having change listeners that call the onItemSelected method in StatisticsActivity when the value of a spinner changes, after which the required TrainingSessions data is fetched from Firebase and the RecyclerView updates all the charts. 
At first each time an Exercise was saved there was a new TrainingSession created which was linked to the exercise- as well as the workoutPlanId, but this design turned out to not be a good fit for the usecase because over the lifecycle of the database it creates a ton of entries but it also repeatedly led to the creation of a chart for each TrainingSession instead for each exercise, which I was unable to fix even after trying to group the TrainingSessions by the exerciseId and then passing the grouped sessions to the Holder, which is when I decided to implement the current design which uses less space and requires less mathematical operations. 

## Settings
The 'Settings' activity was set up to be simple and without much fuss so I could focus my resources on implementing the features users a likely to use more often than logging out of their account or changing their password. As such the design consists of various buttons which will take the user to a Dialog to check if the click was valid and then executing the desired task. 

### Setting functionality for the various buttons using Dialog
The Dialog modules work kind of like popup windows that appear when the button they are linked with is clicked.  This is accomplished by using OnClickListeners, which then execute the method the Dialog lives in. When the Dialog is confirmed by the user (clicking the YES/SAVE option), it calls the method in which the operation is actually executed, in some cases passing data which was inputted into an EditText inside the Dialog itself (e.g. new email address)


## About 
There is not really much to describe here, the Activity basically consists of a few TextViews with padding added to them so they are more easy to read, without padding they would stretch edge to edge on the screen. It features a brief explanation on why the application was created and provides a user with my contact details should they have any suggestions for enhancing track´s capabilities.

# Database handling

## Original idea
After just getting started with creating the application I initially decided to use the Room database for storing all the data, thinking that I was going to be able to store all of the data in the Cloud somehow later. Not having a initial plan here turned out to be a mistake, as I essentially created many classes needed for Room like Dao´s for each database which I ended up deleting when learning about (and switching to) Google Firebase. Still, I was able to reuse my structure for saving data to the database in most classes and could also use my Entity classes, to which I just needed to add an empty construtor (no arguments passed to it) that is required for Firestore to work properly.

## Firestore & Firebase
As of this time, Firestore and Firebase handle login, registration and authentification as well as managing the three databases 'exercises', 'training_sessions' and 'workout_plans', which were really easy to integrate. Another nice feature is that I was able to 'outsource' some functions I would normally have to implement in greater detail to Firebase like changing a users password, which right now just sends them an email where they can do it in their browser by following a link creating by Firebase without me having to do anything. The whole registration and login process was also immensely simplified for me by using Firebase, with many neccesary methods being saved ready-to-use inside the various documentations provided. Firebase also natively prodives offline-support, which allows users to input data when they are offline that will get put in a queue and updated to the cloud when they are online again, which is neat.

 ## Using repository classes for database operations
While I could technically have done without specific repository classes, it was recommended to use them in various tutorial videos as they take away complexity and make the code more maintaineable, so that is what I did. Basically, each repository class consists of numerous methods that each contain a different database call. Depending on the input, they insert something into the database and return nothing or fetch data and return it for use in other classes. I have also found the repositories pracitcal for reusing code, which is more easier to do this way than having to call some random class that may or may not exist later on the development process.

# Ideas for the future
### These are some ideas for improving the capabilites of track´s as a fitnesstracker in the future.
### If you would like to share an idea with me or report a bug please contact me using: finn_reich@web.de

   - enabling the user to permanently change the position of exercises inside their workout plans with a long click and dragging them to the right position (kind of like switching the position of an app on your homescreen)
	- enabling the user to permanently delete datapoints from the LineData chart 
 - further beautifying the appearance of the LineData 
 - creating a form inside About where users can submit change requests or bugs without needing to leave the application
 - allow the user to input a custom timeframe in Statistics
 - implementing a |Start Training|-button in each workout plan, which the user can click to keep track of how long his training is going. 
	 + could be automated: training starts when element in workoutplan is clicked, stops when an exercise is saved
		 + if a exercise is saved after that in the following 3(?) hours, the end time gets updated
+ adding support for dropsets as well as warmup sets and enabling the user to choose wether or not they should be displayed inside the statistics menu


*This documentation was created as part of the final project assignment from CS50: An introduction to Computer Science using StackEdit*
