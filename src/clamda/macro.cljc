(ns clamda.macro
  (:require
   [clamda.lib :as lib]))

;; Heavily simplified from https://gist.github.com/sunilnandihalli/745654
(defmacro defcurry
  "Simplified [[defn]] which wraps the body forms in an fn and passes that
  function to [[lib/curry]]. Meta support is limited to requiring a `doc`
  string. `:arglists` will automatically be applied to the var meta for
  improved docs."
  ^{:arglists '([name doc args & body])}
  [name doc args & body]
  {:pre [(not-any? #{'&} args)]}
  (if (empty? args)
    `(defn
       ~(with-meta name
          {:doc      doc
           :arglists '([])})
       ~args
       ~@body)
    `(def
       ~(with-meta name
          (macroexpand `{:doc ~doc :arglists '~(list args)}))
       (lib/curry-n ~(count args) (fn ~name ~args ~@body)))))

(defmacro defcopy
  "Copies a [[def]] from one namespace to another with a selection of the
  original def's meta. `fn-var` should come from a different ns than the calling
  ns. The selected meta map will be merged into the locally defined var's meta."
  [name fn-var keys]
  `(let [var-meta# (meta (var ~fn-var))
         selected-meta# (select-keys var-meta# ~keys)]
     (def ~name ~fn-var)
     (alter-meta! (var ~name) #(merge % selected-meta#))
     (var ~name)))
