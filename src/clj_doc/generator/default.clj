(ns clj-doc.generator.default
  (:import (java.io File))
  (:use [clj-html.core                :only (html map-str domap-str)]
        [clj-html.helpers             :only (doctype include-js include-css h)]
        [clojure.contrib.duck-streams :only (spit)]
        [clojure.contrib.str-utils    :only (str-join re-split)]
        [clojure.contrib.def          :only (defvar-)])
  (:load "default_util"))


(defvar- static-contents
  (let [root "/Users/mmcgrana/Desktop/git/clj-doc/src/clj_doc/generator/default-assets"]
    (map (fn [path] [path (slurp (file-join root path))])
         `("javascripts/default.js" "javascripts/jquery-1.2.6.js"
           "stylesheets/reset-min.css" "stylesheets/default.css"))))

(defvar- subdirs '("javascripts" "stylesheets" "snippets"))

(defvar- index-path "index.html")

(defvar- ns-var-ids (ref {}))

(defn- ns-var-id
  "Returns the CSS-safe id to use to represent the var names by the given
  namespace and var name strings."
  [ns-sym var-sym]
  (ref-map-fetch ns-var-ids [ns-sym var-sym] (gensym "var-")))

(defn- snippet-path
  [ns-sym var-sym]
  (file-join "snippets" (str (ns-var-id ns-sym var-sym) ".html")))

(defn- ns-var-link
  "Returns an html link named after the given namespace and var strings that,
  when clicked, will show the corresponding documention."
  [ns-sym var-sym needs-namespace]
  (html
    [:li.var-link
      [:a {:href "#"
           :onclick (str "showVar('" (snippet-path ns-sym var-sym) "')")}
        (h (str var-sym))
        (when needs-namespace
          (html [:span.namespace "(" (h (str ns-sym)) ")"]))]]))

(defn- var-argform
  "Returns an html snippet corresponding to the argform for the given function
  or macro var string and arguments list."
  [var-sym arglist]
  (let [arglist-strs (map str arglist)
        front-elem   (str "(<span class=\"var-name\">" (h (str var-sym)) "</span> ")
        middle-elems (str-join " " (map h (butlast arglist-strs)))
        last-elem    (str " " (h (last arglist-strs)) ")")]
    (str front-elem middle-elems last-elem)))

(defn- var-docstring
  "Returns an html snippet for the docstring."
  [docstring]
  (let [cleaned (h docstring)
        grouped (re-split #"\n\n+\s+" cleaned)]
    (map-str #(html [:p %]) grouped)))

(defn- index-template
  "Returns an html page for the main doc index."
  [ns-syms var-tuples]
  (let [duped-vars-syms (duped (map second var-tuples))]
    (html
      (doctype :xhtml-transitional)
      [:html {:xmlns "http://www.w3.org/1999/xhtml"}
        [:head
          [:meta {:http-equiv "Content-Type" :content "text/html;charset=utf-8"}]
          [:title "clj-doc"]
          (include-js
            "javascripts/jquery-1.2.6.js" "javascripts/default.js")
          (include-css
            "stylesheets/reset-min.css" "stylesheets/default.css"
            "stylesheets/highlight.css")]
        [:body
          [:div#content
            [:div#search
              [:div#search-box
                [:input#search-field {:type "text"}]]
              [:div#search-results
                [:ul
                  [:li#no-results.var-link
                    "(no matches)"]
                  (domap-str [[ns-sym var-sym var-info] var-tuples]
                    (ns-var-link ns-sym var-sym (duped-vars-syms var-sym)))]]]
            [:div#docs
              [:div.doc-item
                [:h2.doc-item-name "clj-doc"]
                [:p "Documented:"]
                [:ul#documenting
                  (domap-str [ns-sym ns-syms]
                    (html [:li (h (str ns-sym))]))]
                [:p "Search by var name to the left."]]]]]])))

(defn- snippet-template
  "Returns an html snippet for the documentation div for the given var tuple."
  [[ns-sym var-sym var-info]]
  (print ".") (flush)
  (html
    [:div.doc-item {:id (ns-var-id ns-sym var-sym)}
      [:h2.doc-item-name
        (h (str var-sym))
        [:span.namespace "(" (h (str ns-sym)) ")"]]
      (if (:macro var-info)
        (html [:p.macro "Macro"]))
      (if-let [arglists (:arglists var-info)]
        (html
          [:ul.var-arglists
            (domap-str [arglist arglists]
              (html [:li (var-argform var-sym arglist)]))]))
      (if-let [doc (:doc var-info)]
        (html
          [:div.var-docstring
            (var-docstring doc)]))
      (if-let [source (force (:source var-info))]
        (html
          [:div.var-source
            [:div.highlight
              [:pre
                (str "; " (:path var-info) ":" (:line var-info) "\n"
                (h source))]]]))]))

(defn generate [ns-syms var-tuples]
  (let [sorted-var-tuples
          (sort-by (fn [[ns-sym var-sym var-meta]] [var-sym ns-sym]) var-tuples)
        index-contents
          [index-path (index-template ns-syms sorted-var-tuples)]
        snippets-contents
          (map (fn [tuple] [(snippet-path (tuple 0) (tuple 1))
                            (snippet-template tuple)])
               sorted-var-tuples)]
    [subdirs `(~index-contents ~@static-contents ~@snippets-contents)]))