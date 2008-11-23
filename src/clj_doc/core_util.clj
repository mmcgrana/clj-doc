(in-ns 'clj-doc.core)

(defn- take-starting
  "Returns a lazy seq of items in coll start with the first element for which
  pred returns true"
  [pred coll]
  (drop-while #(not (pred %)) coll))

(defn- take-after
  "Returns a lazy seq of items in coll starting after the first
  element for which pred returns true."
  [pred coll]
  (rest (take-starting pred coll)))

(defn- select
  "Like (some f coll), but returns the found element from the seq and not the logical
  true that flagged it as such."
  [f coll]
  (when (seq coll)
    (if (f (first coll)) (first coll) (recur f (rest coll)))))

(defn- find-in-dirs [#^String path dirs]
  "Returns the absolute path of the first file in among the tree-seqs of dirs
  that end in the given path."
  (select #(.endsWith % path)
         (map #(.getAbsolutePath %)
              (flatten (map #(file-seq (java.io.File. %)) dirs)))))

(defn- file-join
  "Join the given seq-able path segments according to the platform's
  File.separator."
  [& segments]
  (str-join java.io.File/separator segments))

(defn- mkdir-p
  "Ensure that directories exists through the given path"
  [path]
  (.mkdirs (java.io.File. path)))