var populateVocabTiles = function() {
  var exercises = window.userHistory.userCreatedExercises;
  for ( var e = exercises.length -1; e >= 0; e--) {
    $(".tile_container").append(
        '<div class="tile well span1"><a href="vocab_item_view.html#id='+exercises[e].exerciseid+'"><img src="'
            + exercises[e].image_stimuli + '"/></a></div>');
  }
};

/*
 * Capturing user's play back of audio, and saving it and restoring it from
 * localstorage
 */
var userHistory = localStorage.getItem("userHistory");
if (userHistory) {
  userHistory = JSON.parse(userHistory);
//  alert("Welcome back " + userHistory.userProfile[0].firstName);
  window.populateVocabTiles();
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
  // OPrime.debug(JSON.stringify(window.userHistory));
};

// Android WebView is not calling the onbeforeunload to save the userHistory.
window.onbeforeunload = window.saveUser;