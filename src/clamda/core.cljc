(ns clamda.core
  "clamda is a ramda-inspired API which adds more functional primitives for
  clojure programs. The majority of clamda fns are curried and expect data last
  to encourage data-last threading, which we consider superior to data-first.
  Most of clojure's core functions operate data-last, this library covers for
  the sad remainder."
  (:refer-clojure
   :exclude [< > <= >= apply assoc assoc-in divide mod update update-in when])
  (:require
   [clamda.lib :as lib]
   #?@(:clj [[clamda.macro :refer [defcurry defcopy]]]
       :cljs [[clamda.macro :refer-macros [defcurry defcopy]]])))

(declare apply-spec evolve prop update-in to-pairs)

(def __
  "Placeholder to use in curried fns to save argument application for later.

  ```clojure
  (defcurry myvec [x y z] [x y z])
  ((myvec __ 2 __) 1 3)   ; => [1 2 3]
  (((myvec __ 2 __) 1) 3) ; => [1 2 3]
  (((myvec __ __ __) 1) 2 3) ; => [1 2 3]
  ```"
  lib/placeholder)

;; Re-export these vars as if they were defined in the core.
(defcopy curry lib/curry [:doc :arglists])
(defcopy curry-n lib/curry-n [:doc :arglists])

(defcurry <
  "Curried, binary version of [[clojure.core/<]]."
  [x y]
  (clojure.core/< x y))

(defcurry <=
  "Curried, binary version of [[clojure.core/<=]]."
  [x y]
  (clojure.core/<= x y))

(defcurry >
  "Curried, binary version of [[clojure.core/>]]."
  [x y]
  (clojure.core/> x y))

(defcurry >=
  "Curried, binary version of [[clojure.core/>=]]."
  [x y]
  (clojure.core/>= x y))

(defcurry add
  "Add two numbers together. Curried, binary version of [[clojure.core/+]]."
  [x y]
  (clojure.core/+ x y))

(defcurry all-pass?
  "Passes the data into each provided predicate in `preds`, returning true if
  all predicates pass."
  [preds data]
  (every? (fn [pred] (pred data)) preds))

(defcurry any-pass?
  "Passes the data into each provided predicate in `preds`, returning true if
  at least one of the predicates pass."
  [preds data]
  (some (fn [pred] (pred data)) preds))

(defcurry apply
  "Data-last, curried [[clojure.core/apply]]."
  [f data]
  (clojure.core/apply f data))

(defn- -apply-spec-mapper
  "Mapping function for iterative use with [[apply-spec]]'s map."
  ^{:private true
    :arglists '([args [key setter]])}
  [args [key setter]]
  (if (map? setter)
    [key (clojure.core/apply apply-spec setter args)]
    [key (clojure.core/apply setter args)]))

(def
  ^{:arglists '([spec & args])}
  apply-spec
  "Construct a map from the given `spec`, passing rest args into each value
  fn provided by the `spec`, storing the result in the given key. Supports
  map recursion."
  (curry-n
   2
   (fn apply-spec
     [spec & args]
     (->>
      spec
      (map (partial -apply-spec-mapper args))
      (into {})))))

(defcurry assoc
  "Data-last, curried [[clojure.core/assoc]]. Only supports one key-value pair.
  See [[assoc&]] for variadic cross-over support."
  [key value data]
  (clojure.core/assoc data key value))

(defcurry assoc&
  "Data-last, curried [[clojure.core/assoc]] supporting a form of variadic args
  by accepting a seq of key-value pairs as the first argument to be applied as
  rest args to [[cojure.core/assoc]]."
  [kvs data]
  (clojure.core/apply clojure.core/assoc data kvs))

(defcurry assoc-in
  "Data-last, curried [[clojure.core/assoc-in]]. Any additional args to be
  passed to `f` should be partially applied."
  [keys value data]
  (clojure.core/assoc-in data keys value))

(defcurry assoc-in&
  "Data-last, curried [[clojure.core/assoc-in]] supporting a form of variadic
  args by accepting a seq of key-value pairs as the first argument to be applied
  as rest args to [[cojure.core/assoc-in]]."
  [kvs data]
  (clojure.core/apply clojure.core/assoc-in data kvs))

(defcurry both?
  "Data-last, curried [[clojure.core/and]]. `left` and `right` are unary
  functions accepting the final data argument."
  [left right data]
  (and
   (some? (left data))
   (some? (right data))))

(defcurry default-to
  "Returns the given data if it isn't [[nil?]], otherwise returns the default.
  Supports currying."
  [default data]
  (if (some? data)
    data
    default))

(defcurry divide
  "Divide two numbers together. Curried, binary version of [[clojure.core//]]."
  [numerator denominator]
  (/ numerator denominator))

(defn flip
  "Flips the first two arguments of calls to `f`."
  ^{:arglists '([f])}
  [f]
  (fn [x y & args]
    (clojure.core/apply f y x args)))

(defcurry either?
  "Data-last, curried [[clojure.core/or]]. `left` and `right` are unary
  functions accepting the final data argument."
  [left right data]
  (or
   (some? (left data))
   (some? (right data))))

(defn- -evolve-reducer
  "Reduce data against a specification of functions to apply to the given keys.
  Utilized by [[evolve]]."
  ^{:private true
    :arglists '([{spec :spec data :data} [key updater]])}
  [{:keys [spec data] :as acc} [key updater]]
  (if-not (contains? data key)
    acc
    (if (map? (key data))
      (update-in [:data key] (evolve (key spec)) acc)
      (update-in [:data key] updater acc))))

(defcurry evolve
  "Evolve map values based on `spec` mapping where map values in `spec` are
  functions that will receive the current value for that key in `data`, the
  result the function will be updated at that key using [[update-in]]. Keys
  present in `spec` that are not present in `data` will be ignored. Supports
  nested updates of map values."
  [spec data]
  (->>
   (to-pairs spec)
   (reduce -evolve-reducer {:spec spec :data data})
   :data))

(defcurry exponent
  "Curried Math/pow."
  [base exp]
  (Math/pow base exp))

(def
  ^{:arglists '([])}
  f
  "Thunk that always returns false."
  (constantly false))

(defn from-pairs
  "Turn a collection of `[key value]` pairs into a map."
  ^{:arglists '([pairs])}
  [pairs]
  (into {} pairs))

(defcurry if-else
  "Data-last, curried [[clojure.core/if]]. `test`, `then` and `else` args are
  all unary functions accepting the final `data` arg."
  [test then else data]
  (if (test data)
    (then data)
    (else data)))

(defcurry mod
  "Curried [[clojure.core/mod]]."
  [num div]
  (clojure.core/mod num div))

(defcurry multiply
  "Multiply two numbers together. Curried, binary version of
  [[clojure.core/*]]."
  [x y]
  (* x y))

(defcurry none?
  "Returns true if none of the elements in `data` pass the given predicate."
  [pred data]
  (every? (complement pred) data))

(defcurry path
  "Data-last, curried [[clojure.core/get-in]] without default.
  See [[path-or]]."
  [keys data]
  (get-in data keys))

(defcurry path=
  "Data-last, curried [[=]] comparison of the value at path in the given map.
  See [[path]] and [[clojure.core/=]]."
  [keys x data]
  (->>
   data
   (path keys)
   (= x)))

(defcurry path-or
  "Data-last, curried [[clojure.core/get-in]] with default arg."
  [default keys data]
  (get-in data keys default))

(defcurry path-satisfies?
  "Retrieves the value using [[path]] and runs it against the given predicate."
  [pred keys data]
  (->> data (path keys) (pred)))

(defcurry paths
  "Fetches values from multiple paths through the given map. See [[path]]."
  [keys-keys data]
  (map (fn [keys] (path keys data)) keys-keys))

(defcurry pick
  "Data-last, curried [[clojure.core/select-keys]]."
  [keys data]
  (select-keys data keys))

(defcurry pluck
  "Get the named prop value from each given map. See [[prop]]."
  [key maps]
  (map (prop key) maps))

(defcurry project
  "[[pick]] `keys` from each map in `maps`. Analogous to a SQL select
  statement."
  [keys maps]
  (map (pick keys) maps))

(defcurry prop
  "Data-last, curried [[clojure.core/get]] without default. See [[prop-or]]."
  [key data]
  (get data key))

(defcurry prop=
  "Data-last, curried [[=]] comparison of the prop in the given map.
  See [[prop]] and [[clojure.core/=]]."
  [key x data]
  (->>
   data
   (prop key)
   (= x)))

(defcurry prop-or
  "Data-last, curried [[clojure.core/get]] with default arg."
  [default key data]
  (get data key default))

(defcurry prop-satisfies?
  "Retrieves the value using [[prop]] and runs it against the given predicate."
  [pred key data]
  (->> data (prop key) (pred)))

(defcurry props
  "Retrieve the values for the given keys from `data`."
  [keys data]
  (->> data (pick keys) (vals)))

(defcurry subtract
  "Subtract two numbers together. Curried, binary version of
  [[clojure.core/-]]."
  [x y]
  (- x y))

(def
  ^{:arglists '([])}
  t
  "Thunk that always returns true."
  (constantly true))

(defcurry tap
  "Invokes the given function with data (presumably for side-effects) and
  returns data, discarding the result of the function, if any."
  [tapper data]
  (tapper data)
  data)

(defn to-pairs
  "Get a collection of `[key value]` pairs from a map."
  ^{:arglists '([data])}
  [data]
  (map identity data))

(defcurry try-catch
  "Data-last, curried [[try]] + [[catch]]. `tryer` will received `data`, and if
  it does not throw will return its result. If it does throw, `catcher` will be
  invoked with the exception and the given `data` argument."
  [tryer catcher data]
  (try
    (tryer data)
    (catch Exception exception
      (catcher exception data))))

(defcurry update
  "Data-last, curried [[clojure.core/update]]. Any additional args to be passed
  to `f` should be partially applied."
  [key f data]
  (clojure.core/update data key f))

(defcurry update-in
  "Data-last, curried [[clojure.core/update-in]]. Any additional args to be
  passed to `f` should be partially applied."
  [keys f data]
  (clojure.core/update-in data keys f))

(defcurry when
  "Data-last, curried [[clojure.core/when]]. `test` and `then` args are all
  unary functions accepting the final `data` arg. Differs from
  [[clojure.core/when]] by treating the else clause as identity for the given
  `data` instead of returning `nil`."
  [test then data]
  (if-else test then identity data))
