(ns clj-doc.core
  (:use [clojure.contrib.seq-utils :only (flatten)]
        [clojure.contrib.def       :only (defvar-)]
        [clojure.contrib.str-utils :only (re-split str-join)]))

(defn- path-by-meta
  "Returns the qualified path of a file based on the given ns."
  [var-meta]
  (file-join
    (apply file-join (butlast (re-seq #"[^\.]+" (str (:ns var-meta)))))
    (:file var-meta)))

(defn- get-raw-source
  "Returns the string corresponding to the form in the file at the given path
  and at the given line."
  [abs-path line]
  (when abs-path
     (with-open [rdr (java.io.LineNumberReader.
                       (java.io.InputStreamReader.
                         (java.io.FileInputStream. abs-path)))]
       (dotimes [_ (dec line)]
         (.readLine rdr))
       (.mark rdr 20000)
       (read (java.io.PushbackReader. rdr))
       (let [cnt (+ 1 (- (.getLineNumber rdr) line))
             stb (StringBuilder.)]
         (.reset rdr)
         (dotimes [_ cnt]
           (.append stb (.readLine rdr))
           (.append stb "\n"))
         (.append stb (.readLine rdr))
         (.toString stb)))))

(defvar- start-metamap-line-p #"^\s*#")
(defvar- end-metamap-line-p   #"^.*}\s*$")
(defvar- start-doc-line-p     #"^\s*\"")
(defvar- start-nonmeta-line-p #"^\s*(\[)|(\(\[)")

(defn- without-dup-meta
  "Returns a source string corresponding to the given string but with docstrings
  and literal metadata removed. Uses the following heuristics:
  
  If literal metamap on second line
  => first line, next line after line ending with }
  If docstring on second line
  => first line, next line starting with [ ([      
  If neither metamap or docstring on second line
  => whole source
  
  Note that if the source is exactly one line long it will be returned as-is."
  [raw-source]
  (let [lines       (re-split #"\n" raw-source)
        first-line  (first lines)
        rest-lines  (rest lines)
        second-line (first rest-lines)]
    (if second-line
      (str-join "\n"
        (cons first-line
          (cond
            (re-find start-metamap-line-p second-line)
              (take-after    #(re-find end-metamap-line-p %) rest-lines)
            (re-find start-doc-line-p second-line)
              (take-starting #(re-find start-nonmeta-line-p %) rest-lines)
            :else
              rest-lines)))
      raw-source)))

(defn- get-source
  "Returns a string of the source of the form at the given path and line, but
  without and metadata-defining code."
  [abs-path line]
  (if-let [raw-source (get-raw-source abs-path line)]
    (without-dup-meta raw-source)))

(defn- source-info
  "Returns a Map <:path String, :source Delay<String>> corresponding to the 
  given var-meta and src-dirs."
  [var-meta src-dirs]
  (let [path       (path-by-meta var-meta)
        abs-path   (find-in-dirs path src-dirs)
        source     (delay (get-source abs-path (:line var-meta)))]
    {:path path :source source}))
