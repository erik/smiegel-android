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
    this._validateCredentials(creds);

    var oldCreds = this.getCredentials();

    $.extend(oldCreds, creds);

    store.set('creds', oldCreds);
    this.trigger();
  },

  setCredentials: function(creds) {
    this._validateCredentials(creds);

    // TODO: check all properties exist
    creds.isInitialized = true;

    store.set('creds', creds);
    this.trigger();
  },

  /** heads up this throws on error and returns nothing on success */
  _validateCredentials: function(creds) {
    window.atob(creds.auth_token || '');
    window.atob(creds.shared_key || '');
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
