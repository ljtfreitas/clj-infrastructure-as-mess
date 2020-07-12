(ns infrastructure-as-mess.some-ideas
  (:require [cats.core :as m]
            [cats.monad.exception :as ex]
            [com.stuartsierra.dependency :as dep]))

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

(defmacro ingredients
  [& is]
  `{:ingredients (flatten [~@is])})

(defmacro inputs
  [& keyvals]
  `{:inputs (apply array-map [~@keyvals])})

(defmacro recipe
  [id description & args]
  `(merge {:id          (keyword "recipe" (name ~id))
           :description ~description}
          (into {} [~@args])))

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

(def a-recipe
  (recipe :a-recipe "This recipe creates a lot of cool stuff"
    (inputs
      :a-value (constantly :some-value)
      :other-value (constantly :some-other-value))
    (ingredients stack)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn resolve-input!
  [components acc [k fn]]
  (m/fmap (partial merge acc)
          (ex/try-on
            {k (apply fn components)})))

(defn resolve-inputs!
  [inputs components]
  (m/foldm ex/context
           (partial resolve-input! components)
           {}
           inputs))

(defn resolve-ingredients
  [ingredients inputs]
  (m/return ingredients))

; prepare deploy first?
(defn deploy
  [recipe components]
  (m/mlet [inputs    (resolve-inputs! (:inputs recipe) components)
           resources (resolve-ingredients (:ingredients recipe) inputs)]
          {:inputs    inputs
           :resources resources}))

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

#_(reduce reduce-resources (dep/graph) [a-bucket a-bucket-policy other-bucket-policy])
