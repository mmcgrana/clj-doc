(require 'clj-doc.core 'clj-doc.generator.default)

(def clojure-libs '(core set xml zip main))
(def contrib-libs '(cond condt def duck-streams except fcase
                    import-static javadoc lazy-seqs
                    lazy-xml mmap ns-utils repl-ln repl-utils
                    seq-utils shell-out sql str-utils template test-is trace
                    walk zip-filter))

(def all-qualified-libs
  (concat (map #(symbol (str "clojure." %)) clojure-libs)
          (map #(symbol (str "clojure.contrib." %)) contrib-libs)))

(if-let [doc-root (first *command-line-args*)]
  (clj-doc.core/generate-and-write
    all-qualified-libs
    #(clj-doc.generator.default/generate  %1 %2
      {:title "clj-doc: Clojure Library Documentation"})
    doc-root)
  (println "Provide a doc root as a command line arg."))
