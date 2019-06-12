/**
 * Get error from the current page URL
 */
function checkErrorFromUrl() {
  let url = window.location.href;
  let error_description = url.match('[?&#]error_description=([^&]*)');
  if (error_description) {
    let newNode = document.createElement('div');
    newNode.className = "alert alert-danger";
    newNode.innerHTML = decodeURI(error_description[1]);
    let referenceNode = document.getElementById('title');
    referenceNode.parentNode.insertBefore(newNode, referenceNode.nextSibling);
  }
}

checkErrorFromUrl();
