(ns localshred.clamda.core-test
  (:require [localshred.clamda.core :as core]
            [clojure.test :as t]))

(defn mul
  [x y] (* x y))

(defn add
  [x y] (+ x y))

(defn add-variadic
  [x y z & more]
  (apply + x y z more))

(t/deftest both
  (let [foo-and-bar (core/both :foo :bar)]
    (t/is (= true (foo-and-bar {:foo 123 :bar 456})))
    (t/is (= false (foo-and-bar {:foo 123})))
    (t/is (= false (foo-and-bar {:bar 456})))
    (t/is (= false (foo-and-bar {:not-foo 123 :not-bar 456})))
    (t/is (= false (foo-and-bar {})))))

(t/deftest curry-n
  (let [add-variadic-curried (core/curry-n 3 #'add-variadic)]
    (t/is (= 6 (add-variadic-curried 1 2 3)))
    (t/is (= 6 ((add-variadic-curried 1) 2 3)))
    (t/is (= 6 ((add-variadic-curried 1 2) 3)))
    (t/is (= 6 (((add-variadic-curried 1) 2) 3)))
    (t/is (= 55 (((add-variadic-curried 1) 2) 3 4 5 6 7 8 9 10)))
    (t/is (fn? (add-variadic-curried 1)))
    (t/is (fn? (add-variadic-curried 1 2)))
    (t/is (fn? ((add-variadic-curried 1) 2)))))

(t/deftest default-to
  (t/is (= 123 (core/default-to 'default 123)))
  (t/is (= "hello" (core/default-to 'default "hello")))
  (t/is (= {} (core/default-to 'default {})))
  (t/is (= [] (core/default-to 'default [])))
  (t/is (= "" (core/default-to 'default "")))
  (t/is (= true (core/default-to 'default true)))
  (t/is (= false (core/default-to 'default false)))
  (t/is (= 'default (core/default-to 'default nil))))

(t/deftest either
  (let [foo-or-bar (core/either :foo :bar)]
    (t/is (= true (foo-or-bar {:foo 123 :bar 456})))
    (t/is (= true (foo-or-bar {:bar 456})))
    (t/is (= false (foo-or-bar {})))
    (t/is (= false (foo-or-bar {:not-foo 123 :not-bar 456})))))

(t/deftest if-else
  (let [tester (core/if-else pos? (partial add 2) (partial mul -1))]
    (t/is (= 7 (tester 5)))
    (t/is (= 5 (tester -5)))))

(t/deftest path
  (t/is (= 123 (core/path [:foo :bar :baz] {:foo {:bar {:baz 123}}})))
  (t/is (= 123 (core/path [:foo :bar :baz] {:foo {:bar {:baz 123 :hey 456}}})))
  (t/is (= nil (core/path [:foo :bar :baz] {:foo {:bar 123}})))
  (t/is (= nil (core/path [:foo :bar :baz] {:foo {:baz 123}})))
  (t/is (= nil (core/path [:foo :bar :baz] {:foo {}})))
  (t/is (= nil (core/path [:foo :bar :baz] {:foo 123})))
  (t/is (= nil (core/path [:foo :bar :baz] {:baz 123})))
  (t/is (= nil (core/path [:foo :bar :baz] {})))
  (t/is (= nil (core/path [:foo :bar :baz] [])))
  (t/is (= nil (core/path [:foo :bar :baz] "")))
  (t/is (= nil (core/path [:foo :bar :baz] nil))))

(t/deftest path=
  (t/is (true? (core/path= [:foo :bar :baz] 123 {:foo {:bar {:baz 123}}})))
  (t/is (false? (core/path= [:foo :bar :baz] 123 {:foo {:bar {:baz 456}}})))
  (t/is (false? (core/path= [:foo :bar :baz] 123 {})))
  (t/is (false? (core/path= [:foo :bar :baz] 123 [])))
  (t/is (false? (core/path= [:foo :bar :baz] 123 "")))
  (t/is (false? (core/path= [:foo :bar :baz] 123 nil))))

(t/deftest path-or
  (t/is (= 123 (core/path-or 'default [:foo :bar :baz]
                             {:foo {:bar {:baz 123}}})))
  (t/is (= 123 (core/path-or 'default [:foo :bar :baz]
                             {:foo {:bar {:baz 123 :hey 456}}})))
  (t/is (= nil (core/path-or 'default [:foo :bar :baz]
                             {:foo {:bar {:baz nil}}})))
  (t/is (= 'default (core/path-or 'default [:foo :bar :baz] {:foo {:bar 123}})))
  (t/is (= 'default (core/path-or 'default [:foo :bar :baz] {:foo {:baz 123}})))
  (t/is (= 'default (core/path-or 'default [:foo :bar :baz] {:foo {}})))
  (t/is (= 'default (core/path-or 'default [:foo :bar :baz] {:foo 123})))
  (t/is (= 'default (core/path-or 'default [:foo :bar :baz] {:baz 123})))
  (t/is (= 'default (core/path-or 'default [:foo :bar :baz] {})))
  (t/is (= 'default (core/path-or 'default [:foo :bar :baz] [])))
  (t/is (= 'default (core/path-or 'default [:foo :bar :baz] "")))
  (t/is (= 'default (core/path-or 'default [:foo :bar :baz] nil))))

(t/deftest paths
  (t/is (= [123 456] (core/paths [[:foo :bar] [:hey :you]] {:foo {:bar 123}
                                                            :hey {:you 456}})))
  (t/is (= [123 456] (core/paths [[:foo :bar] [:hi]] {:foo {:bar 123}
                                                    :hi  456})))
  (t/is (= [123 nil] (core/paths [[:foo :bar] [:thing]] {:foo {:bar 123}})))
  (t/is (= [nil nil] (core/paths [[:foo :bar] [:thing]] {})))
  (t/is (= [] (core/paths [] {}))))

(t/deftest path-satisfies
  (t/is (true? (core/path-satisfies pos? [:foo :bar] {:foo {:bar 123}})))
  (t/is (true? (core/path-satisfies nil? [:foo :bar] {:foo {:bar nil}})))
  (t/is (false? (core/path-satisfies pos? [:foo :bar] {:foo {:bar -123}})))
  (t/is (false? (core/path-satisfies nil? [:foo :bar] {:foo {:bar "not pos"}})))
  (t/is (false? (core/path-satisfies some? [:foo :bar] {:foo 123})))
  (t/is (false? (core/path-satisfies some? [:foo :bar] {:foo -123})))
  (t/is (false? (core/path-satisfies some? [:foo :bar] {:foo "not pos"})))
  (t/is (false? (core/path-satisfies some? [:foo :bar] {:foo nil}))))

(t/deftest pick
  (t/is (= {:foo 123 :bar 456} (core/pick [:foo :bar] {:foo 123
                                                       :bar 456
                                                       :baz 789})))
  (t/is (= {} (core/pick [:foo :bar] {:baz 789}))))

(t/deftest pluck
  (t/is (= [18 20] (core/pluck :age [{:name "Jeff" :age 18}
                                     {:name "Bill" :age 20}])))
  (t/is (= [nil nil] (core/pluck :age [{:name "Jeff" :not-age 18}
                                       {:name "Bill" :not-age 20}]))))

(t/deftest project
  (t/is (=
         [{:name "Jeff"} {:name "Bill"}]
         (core/project [:name] [{:name "Jeff" :age 18}
                                {:name "Bill" :age 20}])))
  (t/is (=
         [{:name "Jeff"} {}]
         (core/project [:name] [{:name "Jeff" :age 18}
                                {:something :else}]))))

(t/deftest prop
  (t/is (= 123 (core/prop :foo {:foo 123})))
  (t/is (= nil (core/prop :foo {:bar 123})))
  (t/is (= nil (core/prop :foo {})))
  (t/is (= nil (core/prop :foo [])))
  (t/is (= nil (core/prop :foo "")))
  (t/is (= nil (core/prop :foo nil))))

(t/deftest prop-or
  (t/is (= 123 (core/prop-or 'default :foo {:foo 123})))
  (t/is (= nil (core/prop-or 'default :foo {:foo nil})))
  (t/is (= 'default (core/prop-or 'default :foo {:bar 123})))
  (t/is (= 'default (core/prop-or 'default :foo {})))
  (t/is (= 'default (core/prop-or 'default :foo [])))
  (t/is (= 'default (core/prop-or 'default :foo "")))
  (t/is (= 'default (core/prop-or 'default :foo nil))))

(t/deftest prop-satisfies
  (t/is (true? (core/prop-satisfies pos? :foo {:foo 123})))
  (t/is (true? (core/prop-satisfies nil? :foo {:foo nil})))
  (t/is (false? (core/prop-satisfies pos? :foo {:foo -123})))
  (t/is (false? (core/prop-satisfies nil? :foo {:foo "not pos"})))
  (t/is (false? (core/prop-satisfies some? :foo {:bar 123})))
  (t/is (false? (core/prop-satisfies some? :foo {:bar -123})))
  (t/is (false? (core/prop-satisfies some? :foo {:bar "not pos"})))
  (t/is (false? (core/prop-satisfies some? :foo {:bar nil}))))

(t/deftest tap
  (let [tap-value (atom nil)
        set-tap-value! (partial reset! tap-value)]
    (t/is (nil? @tap-value))
    (t/is (= {:some :thing} (core/tap set-tap-value! {:some :thing})))
    (t/is (= {:some :thing} @tap-value))))

(t/deftest from-pairs
  (t/is (= {:foo 123 :bar 456} (core/from-pairs [[:foo 123] [:bar 456]])))
  (t/is (= {} (core/from-pairs []))))

(t/deftest to-pairs
  (t/is (= [[:foo 123] [:bar 456]] (core/to-pairs {:foo 123 :bar 456})))
  (t/is (= [] (core/to-pairs {}))))

(t/deftest try-catch
  (t/is (= 123 (core/try-catch
                identity
                (fn [err data] {:err err :data data})
                123)))
  (let [{:keys [err data]}
        (core/try-catch (fn [data] (throw (ex-info "test throw" {:data data})))
                        (fn [err data] {:err err :data data})
                        123)]
    (t/is (= "test throw" (ex-message err)))
    (t/is (= 123 (:data (ex-data err))))
    (t/is (= 123 data))))

(t/deftest when
  (let [tester (partial core/when pos? (partial add 2))]
    (t/is (= 7 (tester 5)))
    (t/is (= -5 (tester -5)))
    (t/is (= {:foo 123} (core/when int? (partial add 2) {:foo 123})))))

