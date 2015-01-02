var store = require('../vendor/store');
var $ = require('jquery');

var CryptoUtil = require('../utils/CryptoUtil');

module.exports = {
  recvMessage: function(message) {
    this._postData(
      '/api/message/receive',
      this.formatRequest(1, JSON.stringify(message)),
      function(response) {
        var js = JSON.parse(response);
        alert(js);
      }
    );
  },

  formatRequest: function(user_id, body) {
    var encrypted = JSON.stringify(CryptoUtil.encrypt(body));
    var signature = CryptoUtil.sign(encrypted);

    var creds = store.get('creds') || {};

    var msg = {
      'user_id': creds.user_id,
      'body': encrypted,
      'signature': signature
    };

    return msg;
  },

  _postData: function(endpoint, body, cb) {
    $.ajax({
      type: "POST",
      contentType: "application/json; charset=utf-8",
      url: 'http://192.168.1.109:5000' + endpoint,
      data: JSON.stringify(body),
      dataType: "json",
    }).done(function(data) {
      if (cb) {
        cb(data.body);
      }
    });
  }
};
