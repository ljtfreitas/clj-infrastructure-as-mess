(ns infrastructure-as-mess.samples.s3
  (:require [clojure.string :as string]
            [infrastructure-as-mess.core :refer [dish cook!]]
            [infrastructure-as-mess.data.definition :as definition]
            [infrastructure-as-mess.dsl.ingredients :refer [ingredients blend]]
            [infrastructure-as-mess.dsl.ingredients.aws :as aws]
            [infrastructure-as-mess.dsl.recipe :refer [recipe dependencies]]))

(defn aws-cloud-formation-stack [environment]
  (partial aws/cloud-formation-stack (string/join "-" [(name environment) "s3-bucket"])
                                     (str "S3 Buckets on environment " (name environment))))

(defn -main [environment bucket-name]
  (cook!
    (recipe :create-s3-bucket "Create a new S3 bucket in some environment."
            (dependencies
              {:environment       (constantly environment)
               :bucket-definition (definition/read! :buckets bucket-name)})
            (ingredients
              (blend (aws-cloud-formation-stack environment)
                (aws/s3-bucket bucket-name)))))

  #_(dish
      (recipe :create-s3-bucket "Create a new S3 bucket in some environment."
        (blend (aws-cloud-formation-stack environment)
          (aws/s3-bucket :my-bucket {} (definition/read! :buckets))))))