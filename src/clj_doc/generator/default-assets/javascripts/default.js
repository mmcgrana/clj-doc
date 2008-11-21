// Show in the main doc div the documentation for the function with id.
function showVar(snippetUrl) {
  $("#docs").load(snippetUrl);
}

// Enable live searching of var names list.
function liveFuzzyFind() {
  var searchField =    $("#search-field");
  var container =      $("#search-results");
  var rows =           $("ul > li", container);
  var noResults =      $("#no-results", container);
  var varNamesIndex =  {};
  
  rows.each(function() {
    var row = $(this);
    var link = $("a:first", row);
    var varName = row.children().html();
    varNamesIndex[varName] =   [row, link];
  });
  
  var finder = function(searched) {
    var searchedRe = new RegExp(searched, "i");
    var anyFound = false;
    
    jQuery.each(varNamesIndex, function(varName, tuple) {
      if (searchedRe.test(varName)) {
        tuple[0].show();
        anyFound = true;
      } else {
        tuple[0].hide();
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
  liveFuzzyFind();
});