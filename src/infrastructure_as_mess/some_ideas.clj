(ns infrastructure-as-mess.some-ideas
  (:require [com.stuartsierra.dependency :as dep]))

(defrecord Raw [value])
(defrecord ResourceRef [id])

(defn reference
  [id]
  (->ResourceRef id))

(defn raw
  [value]
  (->Raw value))

(defn resource-id
  [ns n]
  (keyword ns (name n)))

(defn bucket
  [name description properties]
  {:object-id   (resource-id "aws.s3.bucket" name)
   :name        (raw name)
   :kind        :bucket
   :description (raw description)
   :properties  properties})

(defn bucket-policy
  [name bucket properties]
  {:object-id  (resource-id "aws.s3.bucket-policy" name)
   :name       (raw name)
   :kind       :bucket-policy
   :bucket     bucket
   :properties properties})

(defn cloudformation-stack
  [name description template]
  {:name        (raw name)
   :description (raw description)
   :kind        :cloudformation-stack
   :resources   (:resources template)
   :outputs     (:outputs template)})

(defn resources [& resources]
  {:resources (into {} (map #(hash-map (:object-id %) %) resources))})

(defn outputs [& outputs]
  {:outputs outputs})

(defn output [name value]
  {name {:value value}})

(defmacro ->template
 [& fs]
 `(reduce #(merge %1 %2) {} [~@fs]))

; build some resources

(def a-bucket
  (bucket :a-bucket "just a bucket" {}))

(def a-bucket-policy
  (bucket-policy :sample-policy (reference :aws.s3.bucket/a-bucket) {}))

(def other-bucket-policy
  (bucket-policy :other-policy (reference :aws.s3.bucket/a-bucket) {}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; build a cloudformation?
(def stack
  (cloudformation-stack :mystack "a simple stack"
    (->template
     (resources a-bucket
                a-bucket-policy)
     (outputs
      (output :bucket-name (reference :aws.s3.bucket/a-bucket))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; send to aws?
(defn aws-deploy
  [stack]
  stack)

(aws-deploy stack)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; build a graph using reference dependencies

(defn new-node
  [id graph ref]
  (dep/depend graph id ref))

(def reference?
  #(instance? ResourceRef %))

(defn reduce-resources
  [graph resource]
  (reduce (partial new-node (:object-id resource))
          graph
          (->> resource
               vals
               (filter reference?)
               (map :id))))

(reduce reduce-resources (dep/graph) [a-bucket a-bucket-policy other-bucket-policy])
