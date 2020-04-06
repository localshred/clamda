(ns localshred.clamda.lib)

(defn count-arity
  "Attempts to count the arity of the given function. Counts the first :arglists
  vector if present, otherwise reflects on the given fn using the Java API.
  Variadic functions will return 0.

    (count-arity #'inc) ; 1
    (count-arity #'map) ; 1 - map's first :arglists entry has one arg

    (defn adder [x y z] (+ x y z))
    (count-arity #'adder)                          ; 3
    (count-arity (fn [x y z] (+ x y z)))           ; 3
    (count-arity (fn [x & more] (apply + x more))) ; 0 "
  [f]
  (if-let [metadata (-> f meta :arglists)]
    (-> metadata first count)
    (-> f class .getDeclaredMethods first .getParameterCount)))
