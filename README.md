# Humtum Android SDK


## Install
Step 1. Add the JitPack repository to your build file


Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
Step 2. Add the dependency

	dependencies {
	        implementation 'com.github.humtum-platform:HumtumAndroidSDK:v0.1-alpha'
	}


## Usage

1. Define the Humtum config in strings.xml
```xml
    <string name="humtum_appid"><Your app ID></string>
    <string name="humtum_ip"><Humtum platform URL></string>
    <string name="humtum_websocket"><Humtum platform websocket></string>
    <string name="com_auth0_client_id"><Auth0 client ID></string>
    <string name="com_auth0_domain"><Auth0 domain></string>
```
2. Initialize library before usage in your Activity class or your Application class
```kotlin
  HumtumApp.initialize(<Context>)
```
3. Use ```HumtumAuth.launchLoginUI``` to login
4. After login is successful, call `HumtumApp.currentInstance` to get your Humtum instance to call the Humtum platform API.

  
