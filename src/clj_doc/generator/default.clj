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

(defvar- subdirs '("javascripts" "stylesheets" "vars" "namespaces"))

(defvar- index-path "index.html")

(defvar- ns-var-ids (ref {}))

(defvar- hstr (comp h str))

(defn- ns-var-id
  "Returns the CSS-safe id to use to represent the var names by the given
  namespace and var name strings."
  [ns-sym var-sym]
  (ref-map-fetch ns-var-ids [ns-sym var-sym] (gensym "doc-")))

(defn- var-path
  [ns-sym var-sym]
  (file-join "vars" (str (ns-var-id ns-sym var-sym) ".html")))

(defn- show-doc-js
  [ns-sym var-sym]
  (str "showDocItem('" (var-path ns-sym var-sym) "')" ))

(defn- var-argform
  "Returns an html snippet corresponding to the argform for the given function
  or macro var string and arguments list."
  [var-sym arglist]
  (let [arglist-strs (map str arglist)
        front-elem   (html "(" [:span.var-name (hstr var-sym)] " ")
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
  [ns-syms var-tuples options]
  (let [duped-vars-syms (duped (map second var-tuples))]
    (html
      (doctype :xhtml-transitional)
      [:html {:xmlns "http://www.w3.org/1999/xhtml"}
        [:head
          [:meta {:http-equiv "Content-Type" :content "text/html;charset=utf-8"}]
          [:title (or (:title options) "clj-doc")]
          (include-js
            "javascripts/jquery-1.2.6.js" "javascripts/default.js")
          (include-css
            "stylesheets/reset-min.css" "stylesheets/default.css")]
        [:body
          [:div#content
            [:div#listings
              [:div#search-box-vars [:input {:type "text"}]]
              [:ul
                [:li#no-results.listing-link.display-none "(no matches)"]
                (domap-str [[ns-sym var-sym _] var-tuples]
                  (html
                    [:li.listing-link
                      {:href "#" :onclick (show-doc-js ns-sym var-sym)}
                      (hstr var-sym)
                      (if (duped-vars-syms var-sym)
                         (html [:span.disambig-namespace
                                 "(" (hstr ns-sym) ")"]))]))]]
            [:div#doc-items
              [:div.doc-item
                [:h2.doc-item-name "clj-doc"]
                [:p "Documented:"]
                [:ul#documenting
                  (domap-str [ns-sym ns-syms]
                    (html [:li (h (str ns-sym))]))]
                [:p "Search by var name to the left."]]]]]])))

(defn- license-notice
  []
  (html
    [:div.license
      [:p "Copyright (c) Rich Hickey. All rights reserved."]
      [:p "The use and distribution terms for this software are covered by the "
          [:a {:href "http://www.opensource.org/licenses/cpl1.0.php"}
            "Common Public License 1.0"] ", which can be found in the file
          CPL.TXT at the root of this distribution. By using this software in
          any fashion, you are agreeing to be bound by the terms of this 
          license. You must not remove this notice, or any other, from this 
          software."]]))

(defn- var-template
  "Returns an html snippet for the documentation div for the given var tuple."
  [[ns-sym var-sym var-info]]
  (html
    [:div.doc-item {:id (ns-var-id ns-sym var-sym)}
      [:h2.doc-item-name (hstr var-sym)
        [:span.namespace "(" (hstr ns-sym) ")"]]
      (if (:macro var-info)
        (html [:p.macro "Macro"]))
      (if-let [arglists (:arglists var-info)]
        (html
          [:ul.var-arglists
            (domap-str [arglist arglists]
              (html [:li (var-argform var-sym arglist)]))]))
      (if-let [doc (:doc var-info)]
        (html
          [:div.var-docstring (var-docstring doc)]))
      (if-let [source (force (:source var-info))]
        (html
          [:div.var-source
            [:pre (str "; " (:path var-info) ":" (:line var-info) "\n"
                       (h source))]]))
      (license-notice)]))

(defn generate [ns-syms var-tuples options]
  "Generate the HTML documation. Returns a 2-tuple, the first containing
  relative paths of directories to ensure exists, the second of (relative path,
  contents pairs) representing to files to write at specified locations."
  (let [sorted-var-tuples
          (sort-by (fn [[ns-sym var-sym var-meta]] [var-sym ns-sym]) var-tuples)
        index-contents
          [index-path (index-template ns-syms sorted-var-tuples options)]
        var-contents
          (map (fn [[ns-sym var-sym _ :as tuple]]
                 (print ".") (flush)
                 [(var-path ns-sym var-sym) (var-template tuple)])
               sorted-var-tuples)]
    [subdirs
     `(~index-contents ~@static-contents ~@var-contents)]))