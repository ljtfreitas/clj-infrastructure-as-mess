(ns infrastructure-as-mess.system
  (:require [cognitect.aws.client.api :as aws]
            [infrastructure-as-mess.aws.aws-platform :as aws-platform]))


(defn aws-client [api]
  (aws/client {:api               api
               :region            :sa-east-1
               :endpoint-override {:protocol :http
                                   :hostname "localhost"
                                   :port     4566}}))

(def definitions
  {:buckets {:just-a-bucket {:name         :just-a-bucket
                             :squad        :platform
                             :environments #{:test :staging :prod}}}})

(def components {:aws-platform aws-platform/->aws
                 :cloud-formation (aws-client :cloudformation)
                 :definition definitions})