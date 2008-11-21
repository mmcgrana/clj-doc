(ns clj-doc.core
  (:use [clojure.contrib.duck-streams :only (spit)])
  (:load "core_util" "core_source"))

(defn ensure-used
  "clojure/use all namspaces named by the given symbols to ensure that we are
  ready to generate docs for them."
  [ns-syms]
  (doseq [ns-sym ns-syms] (use ns-sym)))

(defn collect-tuples [ns-syms]
  "Returns a seq of [ns-sym var-sym var-meta] tuples corresponding to the given
  namespace symbols."
  (mapcat
    (fn [ns-sym]
      (map
        (fn [[var-sym the-var]] (vector ns-sym var-sym (meta the-var)))
        (ns-publics ns-sym)))
    ns-syms))

(defn process-tuples
  "Return a seq of tuples corresponding to the given seq but with the
  metadata augmented with source information."
  [tuples src-dirs]
  (map
    (fn [tuple]
      (let [var-meta (get tuple 2)]
        (assoc tuple 2 (merge var-meta (source-info var-meta src-dirs)))))
    tuples))

(defn generate
  "Generate documation for the namespaces named by the given symbols, 
  looking for source code in the given src-dirs, and using given the generator 
  fn. Returns a seq of two elements, the first a list of directories to ensure
  relative to the doc root dir, the second a list of relative path, contents
  pairs for which to write corresponding files."
  [ns-syms src-dirs generator]
  (let [tuples  (collect-tuples ns-syms)
        ptuples (process-tuples tuples src-dirs)]
    (generator ptuples)))

(defn generate-and-write
  "Generate and write into the given doc-dir the documation for the namespaces 
  named by the given symbols, looking for source code in the given src-dirs, 
  and using given the generator fn."
  [ns-syms src-dirs generator doc-dir]
  (ensure-used ns-syms)
  (let [[subdirs path-contents] (generate ns-syms src-dirs generator)]
    (doseq [subdir subdirs]
      (mkdir-p  (file-join doc-dir subdir)))
    (doseq [[path contents] path-contents]
      (spit (file-join doc-dir path) contents))))
