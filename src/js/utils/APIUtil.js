var store = require('../vendor/store');
var $ = require('jquery');

var KeyStore = require('../stores/KeyStore');

var CryptoUtil = require('../utils/CryptoUtil');
var Util = require('../utils/Util');


module.exports = {
  recvMessage: function(message) {
    this._postData(
      '/api/message/receive',
      this.formatRequest(JSON.stringify(message)),
      function(response) {
        Util.alert(response);
      }
    );
  },

  sendContacts: function(contacts) {
    this._postData(
      '/api/contacts',
      this.formatRequest(JSON.stringify(contacts)),
      function(response) {
        Util.alert(response);
      }
    );
  },

  ping: function() {
    var rnd = CryptoUtil.genRandomBytes(16);
    var rndstr = CryptoUtil.arrayToBase64(rnd);

    var postData = {
      'user_id': KeyStore.getUserId(),
      'body': rndstr,
      'signature': CryptoUtil.sign(rndstr)
    };

    this._postData(
      '/api/ping',
      postData,
      function(response) {
        if (response === rndstr) {
          Util.alert('PING success');
        } else {
          Util.alert('PING failed! Received: "' + response +'" expected "' + rndstr);
        }
      }
    )
  },

  formatRequest: function(body) {
    var encrypted = JSON.stringify(CryptoUtil.encrypt(body));
    var signature = CryptoUtil.sign(encrypted);

    var msg = {
      'user_id': KeyStore.getUserId(),
      'body': encrypted,
      'signature': signature
    };

    return msg;
  },

  _postData: function(endpoint, body, cb, errorFn) {
    $.ajax({
      type: "POST",
      contentType: "application/json; charset=utf-8",
      url: KeyStore.getUri() + endpoint,
      data: JSON.stringify(body),
      dataType: "json",
      error: errorFn || function() {}
    }).done(function(data) {
      if (cb) {
        cb(data.body);
      }
    });
  }
};
