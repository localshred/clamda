(ns clamda.lib)

(defn count-arity
  "Attempts to count the arity of the given function. Counts the first :arglists
  vector if present, otherwise reflects on the given fn using the Java API.
  Variadic functions will return 0.

  ```clojure
  (count-arity #'inc) ; 1
  (count-arity #'map) ; 1 - map's first :arglists entry has one arg

  (defn adder [x y z] (+ x y z))
  (count-arity #'adder)                          ; 3
  (count-arity (fn [x y z] (+ x y z)))           ; 3
  (count-arity (fn [x & more] (apply + x more))) ; 0
  ```"
  [f]
  (if-let [metadata (-> f meta :arglists)]
    (-> metadata first count)
    #?(:clj  (-> f class .getDeclaredMethods first .getParameterCount)
       :cljs (.length f))))

(def placeholder
  "Placeholder to be used in curried functions to delay positional
  argument application."
  :clamda-placeholder)

(def placeholder?
  "Set predicate for placeholder value."
  (set [placeholder]))

(defn- -combine-curried-args-reducer
  "Reducer which combines a set of \"received\" arguments and \"incoming\" args
  by building up the combined set from `received`, and replacing with top of
  `args` each time a placeholder value is encountered in `received`.
  Placeholders are left in place if there are no more arguments to replace them
  with for downstream replacement. See [[combine-curried-args]]."
  ([] [])
  ([{:keys [args received]} received-value]
   (if (and
        (seq args)
        (placeholder? received-value))
     {:received (concat received (take 1 args))
      :args     (drop 1 args)}
     {:received (concat received [received-value])
      :args     args})))

(defn combine-curried-args
  "Concats `received` and `args` together if `received` doesn't have any
  placeholder values. Otherwise, interleaves `args` values in positional order
  for any `received` values that are placeholders, concating remaining `args` on
  the end and returning the whole vector.
  See [[clamda.core/curry-n]]."
  [received args]
  (let [any-placeholders (seq (filter placeholder? received))]
    (if any-placeholders
      (->>
       received
       (reduce
        -combine-curried-args-reducer
        {:received   []
         :args       args})
       ((fn [acc] (select-keys acc [:received :args])))
       (vals)
       (apply concat))
      (concat received args))))

(defn count-args
  "Count the number of elements in the collection that aren't placeholders."
  [args]
  (count (remove placeholder? args)))

(defn curry-n
  "Curry up to `arity` arguments, afterwards invoking `f` with any other
  available args. Allows passing one or more args on any call. `f` will only
  be invoked after `arity` total args are provided. Supports currying variadic
  functions as long as `arity` doesn't include the variadic argument, which
  means we won't curry any arguments past `arity`, but will pass any args beyond
  `arity` via [[clojure.core/apply]].

  Using [[__]] as a placeholder, args can be interleaved
  wherever you wish and curry will continue to provide a callable function until
  all args are provided. See [[combine-curried-args]] for more info
  on the order args are applied when one or more placeholder values are used.

  ```clojure
  (defn adder [x y z] (+ x y z))
  (def curried-adder (curry #'adder))
  (curried-adder 1)         ; no invocation
  (curried-adder 1 2)       ; no invocation
  (curried-adder 1 2 3)     ; 6
  ((curried-adder 1 2) 3)   ; 6
  ((curried-adder 1) 2 3)   ; 6
  (((curried-adder 1) 2) 3) ; 6
  ```"
  ^{:arglists '([arity f] [arity received f])}
  ([arity f]
   (curry-n arity [] f))
  ([arity received f]
   (if (= arity (count-args received))
     (clojure.core/apply f received)
     (fn [& args]
       (let [combined  (combine-curried-args received args)
             remaining (- arity (count-args combined))]
         (if (clojure.core/>= 0 remaining)
           (clojure.core/apply f combined)
           (curry-n arity combined f)))))))

(defn curry
  "Curries the given `f` based on the arity count. Currently only takes the
  first item in :arglists for the count. Variadic anonymous functions also
  report an arity of 0, but will work as expected if you use [[curry-n]] with
  the number of positional args (ignoring the variadic arg)."
  ^{:arglists '([f])}
  [f]
  (curry-n (count-arity f) f))

