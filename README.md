## Items Tracking

An Android based Todo app for tracking items. You can add items with their Title, Cost, Location, Description and Image.

## Prerequisites
Android SDK v24
Latest Android Build Tools
Android Support Repository

## Features

You can add data of each item
- Item title
- Item price
- Item cost
- Item location
- Item picture (You can take picture from Camera or choose from Gallery)
- Items can be synced with firebase cloud to save and access remotely
- Item can be edit/delete/update

# [Architecture used](https://github.com/googlesamples/android-architecture "Architecture used")

![](https://i.imgur.com/5fg5z5r.png)


### Dependencies used

    // App's dependencies
    implementation 'com.android.support:appcompat-v7:27.1.1'
    implementation 'com.android.support:cardviewv7:27.1.1'
    implementation 'com.android.support:design:27.1.1'
    implementation 'com.android.support:recyclerview-v7:27.1.1'
    implementation 'com.android.support:support-v4:27.1.'
    implementation 'com.mlsdev.rximagepicker:library:2.0.2'
    implementation 'com.github.bumptech.glide:glide:4.4.0'
    implementation 'com.google.firebase:firebase-database:15.0.1'


    implementation 'com.android.support.test.espresso:espresso-idling-resource:3.0.1'
    implementation 'com.google.guava:guava:18.0'
    implementation 'android.arch.persistence.room:runtime:1.0.0'
    annotationProcessor 'android.arch.persistence.room:compiler:1.0.0'

    // Dependencies for local unit tests
    testImplementation 'junit:junit:4.12'
    testImplementation 'org.mockito:mockito-all:1.10.19'
    testImplementation 'org.hamcrest:hamcrest-all:1.3'

    // Android Testing Support Library's runner and rules
    androidTestImplementation 'com.android.support.test:runner:1.0.1'
    androidTestImplementation 'com.android.support.test:rules:1.0.1'
    androidTestImplementation 'android.arch.persistence.room:testing:1.0.0'

    // For unit tests
    androidTestImplementation 'junit:junit:4.12'
    androidTestImplementation 'org.mockito:mockito-core:1.10.19'
    androidTestImplementation 'com.google.dexmaker:dexmaker:1.2'
    androidTestImplementation 'com.google.dexmaker:dexmaker-mockito:1.2'

    // Espresso UI Testing
    androidTestImplementation 'com.android.support.test.espresso:espresso-core:3.0.1'
    androidTestImplementation 'com.android.support.test.espresso:espresso-contrib:3.0.1'
    androidTestImplementation 'com.android.support.test.espresso:espresso-intents:3.0.1'
    androidTestImplementation 'com.android.support.test.espresso.idling:idling-concurrent:3.0.1'
    implementation 'com.android.support.test.espresso:espresso-idling-resource:3.0.1'


### Libraries used
##### [Glide](https://github.com/bumptech/glide "Glide")
For Image loading
##### [Rximagepicker](https://github.com/MLSDev/RxImagePicker "Rximagepicker")
For Image picking from Camera/Gallery


### [Firebase Realtime Database (JSON Structured)](https://firebase.google.com/docs/database/security/ "Firebase Realtime Database (JSON Structured)")

      {
         "items": [
             "-0f3b6efb-7689-4f2b-b6a0-e135b9a85fd8" : {
			 "id" : "123"
             "title" : "Buying Shoes",
             "cost" : "200"
			 "location" : "Gullberg III, Lahore, Pakistan"
			 "description" : "I am buying sports shoes from NIKE. "
			 "imagePath" : "...../shoesimage.png"
               }
		 ]
	  }
	  
### Database local table 
| items       | Value |
| --------- |-----|
| id  | 123 |
| title     |   Buying Shoes |
| cost      |    200$ |
| location      |    Gullberg III, Lahore, Pakistan |
| description      |    I am buying sports shoes from NIKE. |
| imagePath      |    ...../shoesimage.png |

### [Firebase database rules](https://firebase.google.com/docs/database/security/ "Firebase database rules")

      These rules allows everyone to read & write data without authentication. Currently for testing purposes we have applied these so everyone can read and write.
    {
       "rules": {
          ".read": true,
         ".write": true
        }
    }
	
	The below rules allow authenticated users only to read or write data.
     {
       "rules": {
           ".read": "auth != null",
          ".write": "auth != null"
     }
    }


## How to run a sample
- Clone or download the project open it with Android Studio compile and run it will work.


### Images
<img src="./screens/01.png" width="200"/> <img src="./screens/02.png" width="200"/> <img src="./screens/03.png" width="200"/>
<img src="./screens/04.png" width="200"/><img src="./screens/05.png" width="200"/>
<br/>


## Author
[Waheed Nazir (GreenProLogix)](https://www.linkedin.com/in/waheed-nazir-36521579/ "Waheed Nazir (GreenProLogix)")

## License
It is totally free to use. :)
