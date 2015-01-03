var React = require('react');

var KeyStore = require('./stores/KeyStore');

var App = require('./components/App.react');
var SetupApp = require('./components/SetupApp.react');

var Util = require('./utils/Util');


function onDeviceReady() {
  Util.initialize();

  var elem = document.getElementById('react');

  if (KeyStore.isInitialized()) {
    React.render(<App />, elem);
    return;
  }

  React.render(<SetupApp />, elem);
}

document.addEventListener("deviceready", onDeviceReady, false);
