var Reflux = require('reflux');
var $ = require('jquery');

var store = require('../vendor/store');

var KeyStore = Reflux.createStore({
  isInitialized: function() {
    var creds = this.getCredentials();

    return creds.isInitialized;
  },

  clearCredentials: function() {
    store.set('creds', {isInitialized: false});
  },

  getCredentials: function() {
    return store.get('creds') || {};
  },

  setCredentials: function(creds) {
    var oldCreds = this.getCredentials();

    $.extend(oldCreds, creds);
    oldCreds.isInitialized = true;

    // TODO: check all properties exist

    store.set('creds', oldCreds);
  },

  getToken: function() {
    var tok = this.getCredentials().auth_token;
    return window.atob(tok);
  },

  getSecret: function() {
    var sec = this.getCredentials().shared_key;
    return window.atob(sec);
  }
});


module.exports = KeyStore;
