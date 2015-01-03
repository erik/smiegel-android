var React  = require('react');
var Reflux = require('reflux');

var KeyStore = require('../stores/KeyStore');
var Util = require('../utils/Util');


var Settings = React.createClass({
  mixins: [Reflux.ListenerMixin],

  getInitialState: function() {
    return this._getState();
  },

  componentDidMount: function() {
    this.listenTo(KeyStore, this._onChange);
  },

  _onChange: function() {
    this.setState(this._getState());
  },

  _getState: function() {
    if (!KeyStore.isInitialized()) {
      return {
        secret  : 'unset',
        token   : 'unset',
        user_id : 'unset',
        server  : 'unset',
        email   : 'unset'
      }
    }

    return {
      secret:  window.btoa(KeyStore.getSecret()),
      token:   window.btoa(KeyStore.getToken()),
      user_id: KeyStore.getUserId(),
      server:  KeyStore.getUri(),
      email:   KeyStore.getCredentials().email
    };
  },

  _renderToggle: function(label) {
    return (
      <li className="table-view-cell">
        { label }

        <div className="toggle">
          <div className="toggle-handle"></div>
        </div>
      </li>
    );
  },

  _renderServerConfInput: function(label, placeholder, confKey) {
    return (
      <div className="input-row">
        <label>{ label }</label>
          <input onBlur={ this._updateServerConfig(confKey) }
                 ref={ confKey }
                 type="text"
                 placeholder={ placeholder } />
      </div>
    );
  },

  _updateServerConfig: function(ref) {
    var self = this;

    if (ref === null || typeof (ref) === 'undefined') {
      return function() {};
    }

    return function() {
      var value = self.refs[ref].getDOMNode().value.trim();

      // Don't want to blank out everything.
      if (value === '') {
        return;
      }

      var conf  = {};
      conf[ref] = value;

      self.refs[ref].getDOMNode().value = '';
      try {
        KeyStore.updateCredentials(conf);
      } catch (e) {
        if (e instanceof InvalidCharacterError) {
          Util.alert('Value not in base64 format');
        } else {
          Util.alert('Bad value: ' + e);
        }
      }
    };
  },

  render: function() {
    var state = this.state;
    var self = this;

    return (
      <div id="settings" className="modal">
        <header className="bar bar-nav">
          <a className="icon icon-close pull-right" href="#settings"></a>
          <h1 className="title">Settings</h1>
        </header>

        <div className="content">
          <div className="card">
            <form className="input-group">
              <div className="input-row">
                <p className="content-padded">
                  Smiegel Server Configuration
                </p>
              </div>

              { this._renderServerConfInput('User Id', state.user_id, 'user_id')    }
              { this._renderServerConfInput('Email',   state.email,   'email')      }
              { this._renderServerConfInput('Server',  state.server,  'server')     }
              { this._renderServerConfInput('Token',   state.token,   'auth_token') }
              { this._renderServerConfInput('Secret',  state.secret,  'shared_key') }
            </form>

            <button onClick={function() { Util.scan(self._scanResult); }}
                    className="btn btn-block btn-outlined">
              Scan new QR
            </button>

          </div>

          <div className="card">
            <ul className="table-view">
              { this._renderToggle('Item 1') }
              { this._renderToggle('Item 2') }
              { this._renderToggle('Item 3') }
            </ul>
          </div>

          <div className="card">
            <p className="content-padded">
               Developer Options
               { this._renderButton('butts', function() { navigator.notification.confirm(
    'You are the winner!', // message
     null,                 // callback to invoke with index of button pressed
    'Game Over',           // title
    ['Restart','Exit']     // buttonLabels
); })}
            </p>

            <ul className="table-view">
              { this._renderToggle('Item 1') }
              { this._renderToggle('Item 2') }
              { this._renderToggle('Item 3') }
            </ul>
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


  _scanResult: function(result) {
    try {
      var map = JSON.parse(result);
      KeyStore.setCredentials(map);
    } catch(e) {
      alert('Failed to parse JSON. Are you scanning the right QR?');
      console.log(e);
    }
  }
});


module.exports = Settings;
