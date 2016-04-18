function deleteApplication(card) {
  console.log(card);
  console.log("Attempting to delete " + card.getAttribute("id"));
  $.ajax({
    type: 'POST',
    url: '/deleteapplication/' + card.getAttribute("id"),
    success: function(data) {
      card = card.parentNode.parentNode.parentNode.parentNode.parentNode;
      card.parentNode.removeChild(card);
    }
  });
}
