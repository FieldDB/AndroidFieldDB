/*
 * Capturing user's play back of audio, and saving it and restoring it from
 * localstorage
 */
var userHistory = localStorage.getItem("userHistory");
if (userHistory) {
  userHistory = JSON.parse(userHistory);
  alert("Welcome back " + userHistory.userProfile[0].usersname);
} else {
  window.location.replace("new_user.html");
  userHistory = {};
  userHistory.id = Date.now();
}
OPrime.hub.subscribe("playbackCompleted", function(filename) {
  window.userHistory[filename] = window.userHistory[filename] || [];
  window.userHistory[filename].push(JSON.stringify(new Date()));
  window.saveUser();
}, userHistory);

window.saveUser = function() {
  localStorage.setItem("userHistory", JSON.stringify(window.userHistory));
  //OPrime.debug(JSON.stringify(window.userHistory));
};

// Android WebView is not calling the onbeforeunload to save the userHistory.
window.onbeforeunload = window.saveUser;