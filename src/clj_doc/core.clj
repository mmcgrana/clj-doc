(ns clj-doc.core
  (:use [clojure.contrib.def          :only (defvar- defmacro-)]
        [clojure.contrib.str-utils    :only (re-split str-join)]
        [clojure.contrib.seq-utils    :only (flatten)]
        [clojure.contrib.duck-streams :only (spit)]
        [clojure.contrib.repl-utils   :only (get-source)])
  (:load "core_util" "core_source"))

(defn ensure-required
  "clojure/require all namspaces named by ns-syms to ensure that we are ready to
  generate docs for them."
  [ns-syms]
  (doseq [ns-sym ns-syms] (require ns-sym)))

(defn collect-var-tuples
  "Returns a seq of [ns-sym var-sym var-meta+] tuples corresponding to the
  namespace symbols, where the var-meta+ tuples contain the regular var meta
  as well as source code data."
  [ns-syms]
  (mapcat
    (fn [ns-sym]
      (map (fn [[var-sym the-var]]
             (let [var-meta  (meta the-var)
                   var-meta+ (assoc var-meta :source
                               (parse-source ns-sym var-sym))]
               [ns-sym var-sym var-meta+]))
           (ns-publics ns-sym)))
    ns-syms))

(defn generate
  "Generate documation for the namespaces named by the ns-syms using the
  generator fn. Returns a seq of two elements, the first a list of directories 
  to ensure relative to the doc root dir, the second a list of relative path, 
  contents pairs for which to write corresponding files."
  [ns-syms generator]
  (generator ns-syms (collect-var-tuples ns-syms)))

(defn generate-and-write
  "Generate and write into the dir at path doc-root the documation for namespaces 
  named by the ns-syms, using given the generator fn."
  [ns-syms generator doc-root]
  (ensure-required ns-syms)
  (let [[subdirs path-contents] (generate ns-syms generator)]
    (doseq [subdir subdirs]
      (mkdir-p  (file-join doc-root subdir)))
    (doseq [[path contents] path-contents]
      (spit (file-join doc-root path) contents))
    (println "done")))
