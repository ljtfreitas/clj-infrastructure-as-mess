(ns infrastructure-as-mess.recipe
  (:require [infrastructure-as-mess.dsl :refer [defrecipe defingredient]]
            [infrastructure-as-mess.aws :as aws]))


(defn bucket
  [_]
  (aws/bucket :oi
              :description "ola"
              :name "my bucket"))

(defrecipe :my-recipe "i'm a recipe..."
  :resources    [bucket])
