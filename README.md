# Sample Hello World Plugin for PhoneGap Day 2012 talk

PhoneGap Day US 2012 - http://pgday.phonegap.com/us2012/
PhoneGap Plugins - http://don.github.com/phonegap-plugins

# Install

## iOS

Copy HWPHello.h and HWPHello.m into the Plugins directory
Copy hello.js into the www folder

Edit Cordova.plist and map Hello to HWPHello in the plugins section

## Android

Copy Hello.java to src/com/examlple/plugins/Hello.java
Copy hello.js to assets/www

Edit plugins.xml or cordova.xml and add a line for the plugin

	<plugin name="Hello" value="com.megster.plugin.Hello"/>

## Windows Phone

Copy Hello.cs into the plugins directory
Copy hello.js into the www folder

# Web

Include the Javascript file in you HTML

	<script type="text/javascript" charset="utf-8" src="hello.js"></script>

Add some Javascript to index.html or main.js to call the plugin

	@@@ javascript
	var hello = cordova.require('cordova/plugin/hello');
	
	var	win = function (result) {
			alert(result);		
		}, 
		fail = function (error) {
			alert("ERROR " + error);
		};

	hello.greet("World", win, fail);

# pluginstall

You can try installing this plugin with [pluginstall](https://github.com/alunny/pluginstall) for Android and iOS.



