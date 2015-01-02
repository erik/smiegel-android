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
  },

  getContacts: function(callback) {

    function _formatContacts(contacts) {
      var filtered = [];

      contacts.forEach(function(c) {
        if (!c.name || !c.phoneNumbers) {
          return;
        }

        filtered.push({
          'name': c.displayName || c.name.formatted || c.id,
          'numbers': c.phoneNumbers.map(function(n) {
            return {
              'number': n.value,
              'type': n.type,
              'preferred': n.pref
            };
          })
        });
      });

      return callback(filtered);
    };

    navigator.contacts.find(['*'], _formatContacts);
  }
};
