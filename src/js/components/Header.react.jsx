var React = require('react');

var Header = React.createClass({
  render: function() {
    return (
      <header className="bar bar-nav">
        <h1 className="title">{this.props.title}</h1>
      </header>
    );
  }
});

module.exports = Header;
