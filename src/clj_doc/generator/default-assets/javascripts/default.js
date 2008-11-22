function showDocItem(snippetUrl) {
  $("#doc-items").load(snippetUrl);
}

function liveFuzzyFinder() {
  var searchField =    $("#search-box-vars input");
  var rows =           $("#listings ul li");
  var noResults =      $("#no-results");
  var varNamesIndex =  {};
  
  rows.each(function() {
    var row = $(this);
    var varName = row.html();
    varNamesIndex[varName] = row;
  });
  
  var finder = function(searched) {
    var searchedRe = new RegExp(searched, "i");
    var anyFound = false;
    
    jQuery.each(varNamesIndex, function(varName, row) {
      if (searchedRe.test(varName)) {
        row.show();
        anyFound = true;
      } else {
        row.hide();
      }
    });
    
    anyFound ? noResults.hide() : noResults.show();
  };
  
  searchField.bind("keyup", function(e) {
    finder(searchField.val());
  });
  
  searchField.focus();
  finder(searchField.val());
}

$(document).ready(function() {
  liveFuzzyFinder();
});