(in-ns 'clj-doc.generator.default)

(defn- duped
  "Returns a set of elements of the given coll that appear in the coll two or
  more times."
  [coll]
  (first
    (reduce
      (fn [[duped seen] x]
        (if (contains? seen x)
          [(conj duped x) seen]
          [duped (conj seen x)]))
      [#{} #{}]
      coll)))

(defn- file-join
  "Join the given seq-able path segments according to the platform's
  File.separator."
  [& segments]
  (str-join java.io.File/separator segments))

(defmacro-
  atom-map-fetch
  "Returns the val in the atom's map if it exists, or computes the val, adds it,
  and then returns it if it does not allready. Assumes the val is never nil."
  [atom-sym key-form val-form]
  `(let [key# ~key-form]
    (if-let [val# (get (deref ~atom-sym) key#)]
      val#
      (let [new-val# ~val-form]
        (swap! ~atom-sym assoc key# new-val#)
        new-val#))))

(defn- classpath-slurp
  "Returns the text for the asset file in the classpath."
  [asset-path]
  (let [cl (RT/baseLoader)]
    (with-open [asset-strm (.getResourceAsStream cl asset-path)]
      (slurp* asset-strm))))
