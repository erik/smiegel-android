module.exports = function() {
  console.log(arguments);
  // Keep gulp from hanging on this task
  this.emit('end');
};
