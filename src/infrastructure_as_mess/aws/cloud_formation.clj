(ns infrastructure-as-mess.aws.cloud-formation
  (:require [camel-snake-kebab.core :refer [->PascalCaseString]]
            [clojure.data.json :as json]
            [cognitect.aws.client.api :as aws]))

(def ->pascal-case
  #(->PascalCaseString %))

(defn- ->cloud-formation-resource [{:keys [name properties]}]
  {name properties})

(defn- ->cloud-formation-output [{:keys [name]}]
  {(str name "-ref") {:value {:Ref (->pascal-case name)}}})

(defn- ->cloud-formation-resources [resources]
  (->> resources
       (map ->cloud-formation-resource)
       (into {})))

(defn- ->cloud-formation-outputs [resources]
  (->> resources
       (map ->cloud-formation-output)
       (into {})))

(defn- ->template [{:keys [description resources]}]
  (-> {:a-w-s-template-format-version "2010-09-09"
       :description description}
      (assoc :resources (->cloud-formation-resources resources))
      (assoc :outputs (->cloud-formation-outputs resources))))

(defn- ->json [template]
  (json/write-str template :key-fn ->pascal-case))

(defn- throw-anomaly! [response]
  (let [anomaly (:cognitect.anomalies/category response)]
    (when anomaly
      (throw (ex-info (:cognitect.anomalies/message response) {:cause anomaly})))))

(defn- handle-response! [response]
  (or (throw-anomaly response) response))

(defn- create! [template-body stack cloud-formation]
  (handle-response!
    (aws/invoke cloud-formation
                {:op      :CreateStack
                 :request {:StackName    (:name stack)
                           :TemplateBody template-body}})))

(defn- describe! [_ stack cloud-formation]
  (handle-response!
    (aws/invoke cloud-formation
                {:op      :DescribeStacks
                 :request {:StackName (:name stack)}})))

(defn apply! [stack cloud-formation]
  (-> stack
      ->template
      ->json
      (create! stack cloud-formation)
      (describe! stack cloud-formation)))
