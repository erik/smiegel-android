var React = require('react');

var KeyStore = require('../stores/KeyStore');

var APIUtil = require('../utils/APIUtil');
var Util = require('../utils/Util');

var Header   = require('./Header.react');
var Settings = require('./Settings.react');


var App = React.createClass({
  componentWillMount: function () {
  },

  componentDidMount: function() {
  },

  render: function() {
    var self = this;

    return (
      <div className="app">
        <Header title="Smiegel" />
        <Settings />

        <div className="content">
          <div className="content-padded">

            { this._renderButton('Send contacts', this._sendContacts) }
            { this._renderButton('Spoof SMS', this._spoofSms) }
            { this._renderButton('Ping server', this._pingServer) }
          </div>
        </div>
      </div>
    );
  },

  _renderButton: function(text, callback) {
    return (
      <button className="btn btn-primary btn-block"
              onClick={callback}>
        { text }
      </button>
    );
  },

  _sendContacts: function() {
    Util.getContacts(function(c) {
      APIUtil.sendContacts(c);
    });
  },

  _spoofSms: function() {
    var message = {
      'author': 'Sohn Jmith',
      'number': '123-456-7890',
      'text': 'Hey dude this is a spoofed message have a nice day',
      'timestamp': (new Date()).toJSON()
    };

    APIUtil.recvMessage(message);
  },

  _pingServer: function() {
    APIUtil.ping();
  }
});

module.exports = App;
