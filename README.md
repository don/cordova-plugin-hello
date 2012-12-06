# Cordova Hello World Plugin

This was written as a sample plugin for my PhoneGap Day 2012 talk

PhoneGap Day US 2012 - http://pgday.phonegap.com/us2012/

PhoneGap Plugins - http://don.github.com/phonegap-plugins

# Install

## pluginstall

Android and iOS can be installed with [pluginstall](https://github.com/alunny/pluginstall) 

Pluginstall requires [node.js](http://nodejs.org) and is installed through [npm](https://npmjs.org).  If you're on a mac and use homebrew, install node and npm with `brew install node`.

    $ npm install -g pluginstall

## iOS

### pluginstall

    $ pluginstall ios /path/to/project /path/to/cordova-plugin-hello

### Manual Install

Copy HWPHello.h and HWPHello.m into the Plugins directory

Copy hello.js into the www/js folder

Edit Cordova.plist and map Hello to HWPHello in the plugins section

## Android

### pluginstall

    $ pluginstall android /path/to/project /path/to/cordova-plugin-hello

### Manual Install

Copy Hello.java to src/com/examlple/plugins/Hello.java

Copy hello.js to assets/www/js

Edit plugins.xml or cordova.xml and add a line for the plugin

	<plugin name="Hello" value="com.megster.plugin.Hello"/>

## Windows Phone

Copy Hello.cs into the plugins directory

Copy hello.js into the www/js folder

# Web

Include the Javascript file in you HTML

	<script type="text/javascript" charset="utf-8" src="js/hello.js"></script>

Add some Javascript to index.html or index.js to call the plugin

	var hello = cordova.require('cordova/plugin/hello');
	
	var	win = function (result) {
			alert(result);		
		}, 
		fail = function (error) {
			alert("ERROR " + error);
		};

	hello.greet("World", win, fail);
