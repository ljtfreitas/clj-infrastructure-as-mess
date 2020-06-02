(ns infrastructure-as-mess.aws.aws-platform
  (:require [infrastructure-as-mess.protocols.platform :as protocols.platform]
            [infrastructure-as-mess.aws.resources :as aws.resources]))

(def ->aws
  (reify
    protocols.platform/Platform
    (deploy-all! [_ resources]
      (fn [components]
        (doall
          (->> resources
               (map aws.resources/apply!)
               (map #(apply % [components]))
               flatten))))))