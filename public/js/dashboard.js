function deleteApplication(card) {
  $.ajax({
    type: 'POST',
    url: '/deleteapplication/' + card.getAttribute("id"),
    success: function(data) {
      card = card.parentNode.parentNode.parentNode.parentNode.parentNode;
      card.parentNode.removeChild(card);
    }
  });
}

function updateIdToken () {
  var auth2 = gapi.auth2.getAuthInstance();
  var user = auth2.currentUser.get();
  user.reloadAuthResponse().then(function(response) {
    $.ajax({
      type: 'POST',
      url: '/updatetoken/' + response.id_token
    });
  });
};
setInterval(updateIdToken, 300000);
