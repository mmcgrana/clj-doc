(ns clj-doc.core-test
  (:use clj-unit.core clj-doc.core))

; If literal metamap on second line
; => first line, next line after line ending with }
; If docstring on second line
; => first line, next line starting with [ ([      
; If neither metamap or docstring on second line
; => whole source

(def wdm without-dup-meta)

(deftest "wdm: def plain 1 line"
  (assert= "(def foo :bar)" (wdm "(def foo :bar)")))

(deftest "wdm: def plain 2 lines"
  (assert= "(def foo\n  :bar)" (wdm "(def foo\n  :bar)")))

(deftest "wdm: def plain 2 lines string val"
  (assert= "(def foo\n  \"bar\")" (wdm "(def foo\n  \"bar\")")))

(deftest "wdm: defn plain 1 line"
  (assert= "(defn foo [a b] :bar)"
    (wdm "(defn foo [a b] :bar)")))

(deftest "wdm: defn plain 3 lines"
  (assert= "(defn foo\n  [a b]\n  :bar)"
    (wdm "(defn foo\n  [a b]\n  :bar)")))

(deftest "wdm: def metamap"
  (assert= "(def\n  fn (fn* fn [& decl] (cons 'fn* decl)))"
    (wdm "(def\n  #^{:macro true}\n  fn (fn* fn [& decl] (cons 'fn* decl)))")))

(deftest "wdm: defn docstring on 2nd line"
  (assert= "(def foo\n  [a b]\n  (+ a b))"
    (wdm "(def foo\n  \"some docs\"\n  [a b]\n  (+ a b))")))

(deftest "without-dup-meta: defn metamap on 2nd line"
  (assert= "(defn nil?\n  [x] (identical? x nil))"
    (wdm "(defn nil?\n  \"som docs\"\n  {:tag Boolean}\n  [x] (identical? x nil))")))

