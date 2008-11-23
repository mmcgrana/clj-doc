(ns clj-doc.core
  (:use [clojure.contrib.def          :only (defvar- defmacro-)]
        [clojure.contrib.str-utils    :only (re-split str-join)]
        [clojure.contrib.seq-utils    :only (flatten)]
        [clojure.contrib.duck-streams :only (spit)])
  (:load "core_util" "core_source"))

(defn ensure-used
  "clojure/use all namspaces named by the given symbols to ensure that we are
  ready to generate docs for them."
  [ns-syms]
  (doseq [ns-sym ns-syms] (use ns-sym)))

(defn collect-var-tuples
  "Returns a seq of [ns-sym var-sym var-meta+] tuples corresponding to the given
  namespace symbols, where the var-meta+ tuples contain the regular var meta
  as well as source code data."
  [ns-syms src-dirs]
  (mapcat
    (fn [ns-sym]
      (map (fn [[var-sym the-var]]
             (let [var-meta  (meta the-var)
                   var-meta+ (merge var-meta (source-info var-meta src-dirs))]
               [ns-sym var-sym var-meta+]))
           (ns-publics ns-sym)))
    ns-syms))

(defn generate
  "Generate documation for the namespaces named by the given symbols, 
  looking for source code in the given src-dirs, and using given the generator 
  fn. Returns a seq of two elements, the first a list of directories to ensure
  relative to the doc root dir, the second a list of relative path, contents
  pairs for which to write corresponding files."
  [ns-syms src-dirs generator]
  (generator ns-syms (collect-var-tuples ns-syms src-dirs)))

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
