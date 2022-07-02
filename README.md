# FirebaseNotes

Firebase Notes app is an android application build using Java and android studio to save notes on the cloude storage just by using a simple google authenication.
Also, using google Machine learning models to enhance user experience.

## FUNCTIONALITIES
As soon as user opens the application user is asked to login or signup using google authentication. For which user can use any of their google account.

After login/signup, the user is prompted to Home screen where the user can see all of their saved notes for their are any (for existing users)

<img src="https://github.com/amritagg/FirebaseNotes/blob/master/screenshots/HOME_SCREEN.jpg" height=1000 />

On the home screen user is given option to create a new note using the FAB given at bottom right corner of the screen.

### ADD NOTE

In this activity the user can write their text whatever they want to save and they can also give title to the same.
In this screen user can use ML models.

<b>Features given..</b>
<ul>
    <li>User can just speak and it will get converted into text</li>
    <img src="https://github.com/amritagg/FirebaseNotes/blob/master/screenshots/SPEECH_TO_TEXT.jpg" height=1000 />
    <li>There is a camera option from which the user can click images and also save them with notes. On top of that the text given in that image will be detected and added to note body using ML model</li>
    <li>There is a drawing option from which the user can draw anything and save them with notes. Also, if there is any language text in the drawing, that can be detected and added to note body.</li>
    <li>User can also pick the image from gallery and can detect text from that as well</li>
    <img src="https://github.com/amritagg/FirebaseNotes/blob/master/screenshots/TEXT_DETECT.jpg" height=1000 />
    <li>If the body of note is in english language then the user can translate all of that into hindi and that text will also be appended to note body.</li>
    <img src="https://github.com/amritagg/FirebaseNotes/blob/master/screenshots/TRANSLATION.jpg" height=1000 />
    <li>If the body of note is consists of only one language then user can detect that language also which can be beneficial when user copied text from somewhere but don't know about which language is that.</li>
    <img src="https://github.com/amritagg/FirebaseNotes/blob/master/screenshots/LANGUAGE_DETECTION.jpg" height=1000 />
</ul>

For <b>saving</b> the notes, user just need to back press and all the notes will be saved automatically

For <b>updating</b> the notes, user can just click on the note from home screen and can make changes, note will be updated automatically

For <b>deleting</b> the notes, user can just remove all the text from the required note and then it will be deleted.

### HOME SCREEN

Some of the above features can be accessed directly from home screen and those are
<ul>
    <li>Capture images to detect Text</li>
    <li>Add Drawing to detect Text</li>
    <li>Add Images from gallery to detect Text</li>
    <li>Log Out functionality, If user to use their other account to continue using the application</li>
</ul>

## WHAT DID I USED

I have used different APIs, Models...
<ul>
    <li>For creation of the application I used JAVA and android studio</li>
    <li>For the purpose of data storage and authentication I used Firebase</li>
    <li>The notes on the home screen are showed in RecyclerView and Adapter</li>
    <li>For Text Detection, Language Detection, Translation I used the Machine learning Models provided by Google for Android Applications.</li>
    <li>For Showing the images I used Glide Libraray</li>
    <li>For loading the images from internal storage I used Image picker for reducing the complexity of the app.</li>
    <li>To capture the Images I used the CameraX API of the android</li>
    <li>For transferring data across the activities I used <b>ActivityResultLauncher&lt;Intent&gt;</b> which helps to send data back to parent Activity</li>
    <li>For the user permissions, to use their camera and mic permission can be granted simply by alert dialog which will be promted they are used</li>
</ul>

Using all these Methods, this app is build successfully

<H2>THANK YOU!!</H2>