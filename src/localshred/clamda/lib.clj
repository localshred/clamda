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

(def placeholder
  "Placeholder to be used in curried functions to delay positional
  argument application."
  :clamda-placeholder)

(def placeholder?
  "Set predicate for placeholder value."
  (set [placeholder]))

(defn- combine-curried-args-reducer
  "Reducer which combines a set of \"received\" arguments and \"incoming\" args
  by building up the combined set from `received`, and replacing with top of
  `args` each time a placeholder value is encountered in `received`.
  Placeholders are left in place if there are no more arguments to replace them
  with for downstream replacement. See `combine-curried-args`."
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
  See `localshred.clamda.core/curry-n`."
  [received args]
  (let [any-placeholders (seq (filter placeholder? received))]
    (if any-placeholders
      (->>
       received
       (reduce
        combine-curried-args-reducer
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
