var React = require('react');

var KeyStore = require('../stores/KeyStore');

var APIUtil = require('../utils/APIUtil');
var Util = require('../utils/Util');

var Header = require('./Header.react');

var SetupApp = React.createClass({
  componentWillMount: function () {
  },

  componentDidMount: function() {
  },

  render: function() {
    var self = this;

    return (
      <div className="app">
        <Header title="Smiegel Setup" />

        <div className="content">
          <p className="content-padded"> hello world. I am app.</p>
          <button className="btn btn-block"
                  onClick={function() { Util.scan(self._scanResult); }}>
            Scan QR
          </button>
        </div>
      </div>
    );
  },

  _scanResult: function(result) {
    try {
      var map = JSON.parse(result);
      KeyStore.setCredentials(map);

      window.location.reload();
    } catch(e) {
      alert('Failed to parse JSON. Are you scanning the right QR?');
      console.log(e);
    }
  }
});

module.exports = SetupApp;
