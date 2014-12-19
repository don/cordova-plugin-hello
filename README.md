# Cordova Hello World Plugin

Simple plugin that returns your string prefixed with hello

# Install
Clone the project to <path> then install by typing:

```
    $ cordova plugin add <path>
```

Add some the following to index.js -> deviceReady to call the plugin

```js
	var	win = function (result) {
        alert(result);		
    }, 
    fail = function (error) {
        alert("ERROR " + error);
    };

	hello.greet("World", win, fail);
```