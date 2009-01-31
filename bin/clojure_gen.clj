(require 'clj-doc.core 'clj-doc.generator.default)

(def libs
  '(clojure.core
    clojure.set
    clojure.xml
    clojure.zip
    clojure.main
    clojure.contrib.cond
    clojure.contrib.condt
    clojure.contrib.def
    clojure.contrib.duck-streams
    clojure.contrib.except
    clojure.contrib.fcase
    clojure.contrib.seq-utils
    clojure.contrib.shell-out
    clojure.contrib.str-utils
    clojure.contrib.template
    clojure.contrib.walk))

(let [doc-root (first *command-line-args*)]
  (if-not doc-root
    (println "Usage: clj clojure_gen.clj doc-root")
    (clj-doc.core/generate-and-write
      libs
      #(clj-doc.generator.default/generate  %1 %2
        {:title "clj-doc: Clojure Library Documentation"})
      doc-root)))
