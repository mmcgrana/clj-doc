(in-ns 'clj-doc.core)

(defn- take-starting
  "Returns a lazy seq of items in coll starting with the first element for which
  pred returns true"
  [pred coll]
  (drop-while #(not (pred %)) coll))

(defn- take-after
  "Returns a lazy seq of items in coll starting after the first
  element for which pred returns true."
  [pred coll]
  (rest (take-starting pred coll)))

(defn- file-join
  "Join the given seq-able path segments according to the platform's
  File.separator."
  [& segments]
  (str-join java.io.File/separator segments))

(defn- mkdir-p
  "Ensure that directories exists through the given path"
  [path]
  (.mkdirs (java.io.File. path)))