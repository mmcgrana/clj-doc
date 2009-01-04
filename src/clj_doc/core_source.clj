(in-ns 'clj-doc.core)

(defvar- start-metamap-line-p #"^\s*#")
(defvar- end-metamap-line-p   #"^.*}\s*$")
(defvar- start-doc-line-p     #"^\s*\"")
(defvar- start-nonmeta-line-p #"^\s*(\[)|(\(\[)")

(defn without-dup-meta
  "Returns a source string corresponding to the given string but with docstrings
  and literal metadata removed. Uses the following heuristics:
  
  If literal metamap on second line
  => first line, next line after line ending with }
  If docstring on second line
  => first line, next line starting with [ ([      
  If neither metamap or docstring on second line
  => whole source
  
  Note that if the source is exactly one or two lines long it will be returned 
  as-is."
  [raw-source]
  (let [lines       (re-split #"\n" raw-source)]
    (if (> (count lines) 2)
      (let [first-line  (first lines)
            rest-lines  (rest lines)
            second-line (first rest-lines)]
        (str-join "\n"
          (cons first-line
            (cond
              (re-find start-metamap-line-p second-line)
                (take-after    #(re-find end-metamap-line-p %) rest-lines)
              (re-find start-doc-line-p second-line)
                (take-starting #(re-find start-nonmeta-line-p %) rest-lines)
              :else
                rest-lines))))
      raw-source)))

(defn parse-source
  "Returns a Delay<String> corresponding to the var for the ns and var syms, or
  nil if the source cannot be found."
  [ns-sym var-sym]
  (delay
    (let [qualified-sym (symbol (str ns-sym "/" var-sym))]
      (if-let [raw-source (and (:line ^(resolve qualified-sym))
                               (get-source qualified-sym))]
             (without-dup-meta raw-source)))))
