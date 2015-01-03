var React = require('react');

var Header = React.createClass({
  render: function() {
    return (
      <header className="bar bar-nav">
        <a href="#settings" className="icon icon-gear pull-right"></a>

        <h1 className="title">{this.props.title}</h1>
      </header>
    );
  }
});

module.exports = Header;
