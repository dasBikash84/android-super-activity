# android-super-activity

<h3>Library offering abstract parent activity for Single activity app.</h3>

[![](https://jitpack.io/v/dasBikash84/android-super-activity.svg)](https://jitpack.io/#dasBikash84/android-super-activity)

## Dependency

Add this in your root `build.gradle` file (**not** your module `build.gradle` file):

```gradle
allprojects {
	repositories {
        maven { url "https://jitpack.io" }
    }
}
```

Then, add the library to your module `build.gradle`
```gradle
dependencies {
    implementation 'com.github.dasBikash84:android-super-activity:latest.release.here'
}
```

## Features
- Offers an elegant interface to manage Fragment stack for a single activity application.
- By default new fragments will be loaded on stack. Optionally it could be loaded clearing back stack also.

## Usage example

##### Implementing child activity (for single activity application)
```
    class MainActivity : SingleFragmentSuperActivity() {
    
        override fun getLoneFrameId(): Int = R.id.lone_frame
    
        override fun getLayoutID(): Int  = R.layout.activity_main
    
        override fun getDefaultFragment(): Fragment = FragmentOne()
    }
```
##### Loading child fragment on back stack
```
    //from inside of fragment
    (activity as SingleFragmentSuperActivity).addFragment(fragment) 
```
##### Loading child fragment on clearing back stack
```
    //from inside of fragment
    (activity as SingleFragmentSuperActivity).addFragment(fragment,true)
```
##### Loading child fragment on back stack with post completion task
```
    //from inside of fragment
    (activity as SingleFragmentSuperActivity)
        .addFragment(fragment,doOnFragmentLoad = {
            //Task on fragment load completion
        })
```

License
--------

    Copyright 2020 Bikash Das(das.bikash.dev@gmail.com)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
