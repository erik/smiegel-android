/* Odds and ends. */

module.exports = {
  scan: function(callback) {
    var scanner = cordova.require("cordova/plugin/BarcodeScanner");

    scanner.scan(
      function (result) {
        if (! result.cancelled) {
          callback(result.text);
        }
      },
      function (error) { alert("Scanning failed: " + error); }
    );
  }
};
