(ns infrastructure-as-mess.aws
  (:require [infrastructure-as-mess.dsl :refer [defingredient]]
            [schema.core :as s]))

(defingredient aws bucket "S3 buckets" {:name s/Str})
