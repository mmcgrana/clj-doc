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
  ref-map-fetch
  "Returns the val in the ref if it exists, or computes the val, adds it, and
  then returns it if it does not allready. Assumes val is never nil."
  [ref-sym key-form val-form]
  `(let [key# ~key-form]
    (dosync
      (if-let [val# (get (deref ~ref-sym) key#)]
        val#
        (let [new-val# ~val-form]
          (alter ~ref-sym assoc key# new-val#)
          new-val#)))))