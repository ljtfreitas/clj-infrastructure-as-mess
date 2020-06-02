(ns infrastructure-as-mess.aws.resources
  (:require [infrastructure-as-mess.aws.cloud-formation :as aws.cloud-formation]))

(defmulti apply! :kind)

(defmethod apply! :s3 [s3-bucket]
  (fn [components]
    s3-bucket))

(defmethod apply! :cloud-formation [stack]
  (fn [{:keys [cloud-formation]}]
    (aws.cloud-formation/apply! stack cloud-formation)))

