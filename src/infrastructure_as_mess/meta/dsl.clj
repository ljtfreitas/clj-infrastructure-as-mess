(ns infrastructure-as-mess.meta.dsl
  (:require [clojure.java.io :as io]))

(defn ->map
  [args]
  (apply array-map args))

(defmacro defrecipe
  [name description & args]
  `(merge {:name        ~name
           :description ~description}
          ~(->map args)))



