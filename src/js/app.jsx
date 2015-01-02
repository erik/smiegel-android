var React = require('react');

var KeyStore = require('./stores/KeyStore');

var App = require('./components/App.react');
var SetupApp = require('./components/SetupApp.react');

var app;

if (KeyStore.isInitialized()) { app = <App />;      }
else                          { app = <SetupApp />; }

React.render(app, document.getElementById('react'));
