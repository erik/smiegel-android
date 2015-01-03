var Reflux = require('reflux');
var $ = require('jquery');

var store = require('../vendor/store');

var KeyStore = Reflux.createStore({
  isInitialized: function() {
    var creds = this.getCredentials();

    return creds.isInitialized;
  },

  clearCredentials: function() {
    store.set('creds', { isInitialized: false });
  },

  getCredentials: function() {
    return store.get('creds') || {};
  },

  updateCredentials: function(creds) {
    var oldCreds = this.getCredentials();

    $.extend(oldCreds, creds);

    // TODO: check all properties exist

    store.set('creds', oldCreds);
    this.trigger();
  },

  setCredentials: function(creds) {
    // TODO: check all properties exist
    creds.isInitialized = true;

    store.set('creds', creds);
    this.trigger();
  },

  getToken: function() {
    var tok = this.getCredentials().auth_token;
    return window.atob(tok);
  },

  getSecret: function() {
    var sec = this.getCredentials().shared_key;
    return window.atob(sec);
  },

  getUserId: function() {
    return this.getCredentials().user_id;
  },

  getUri: function() {
    return this.getCredentials().server;
  }
});


module.exports = KeyStore;
