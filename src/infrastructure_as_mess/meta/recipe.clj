(ns infrastructure-as-mess.meta.recipe
  (:require [infrastructure-as-mess.meta.dsl :refer [defrecipe]]
            [schema.core :as schema]))

(def Data {:environment schema/Keyword
           :bucket-name schema/Keyword})

(defn right-environment?
  [{:keys [environment] :as arguments}]
  (if (not= environment :test)
    (throw (ex-info "the environment must be :test." {:environment environment}))
    arguments))

(defn bucket
  [_])

(defrecipe :my-recipe "i'm a recipe..."
  :arguments    {:spec   [["-e" "--environment"]
                          ["-n" "--bucket-name"]]
                 :schema Data}
  :interceptors [right-environment?]
  :resources    [bucket])
