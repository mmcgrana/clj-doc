(require 'clj-doc.core 'clj-doc.generator.default)

(clj-doc.core/generate-and-write
  `(clojure.core clojure.set clojure.xml
    clojure.contrib.cond  clojure.contrib.def  clojure.contrib.duck-streams
    clojure.contrib.fcase clojure.contrib.pred clojure.contrib.seq-utils
    clojure.contrib.str-utils)
  `("/Users/mmcgrana/Desktop/remote/clojure/src/clj",
    "/Users/mmcgrana/Desktop/remote/clojure-contrib/src")
  clj-doc.generator.default/generate
  "/Users/mmcgrana/Desktop/git/clj-doc/doc")