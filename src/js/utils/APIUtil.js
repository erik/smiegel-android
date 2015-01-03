var store = require('../vendor/store');
var $ = require('jquery');

var KeyStore = require('../stores/KeyStore');

var CryptoUtil = require('../utils/CryptoUtil');


module.exports = {
  recvMessage: function(message) {
    this._postData(
      '/api/message/receive',
      this.formatRequest(JSON.stringify(message)),
      function(response) {
        alert(response);
      }
    );
  },

  sendContacts: function(contacts) {
    this._postData(
      '/api/contacts',
      this.formatRequest(JSON.stringify(contacts)),
      function(response) {
        alert(response);
      }
    );
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

  _postData: function(endpoint, body, cb) {
    $.ajax({
      type: "POST",
      contentType: "application/json; charset=utf-8",
      url: KeyStore.getUri() + endpoint,
      data: JSON.stringify(body),
      dataType: "json",
    }).done(function(data) {
      if (cb) {
        cb(data.body);
      }
    });
  }
};
