(ns infrastructure-as-mess.dependencies-graph
  (:require [com.stuartsierra.dependency :as dep]))

(def g1 (-> (dep/graph)
            (dep/depend :b :a)   ; "B depends on A"
            (dep/depend :c :b)   ; "C depends on B"
            (dep/depend :c :a)   ; "C depends on A"
            (dep/depend :d :c))) ; "D depends on C"

(defn dependencies-map [& key-fn]
  (apply array-map key-fn))

(defn vec->map [v]
  (into {} (map #(vector % %) v)))

(defn using
  ([fn]
   (using fn []))
  ([fn dependencies]
   (vary-meta fn update-in [:dependencies] (fnil concat []) dependencies)))

(defn some-environment-dependent-logic
  [environment]
  (str "environment " environment))

(defn other-environment-dependent-logic
  [environment other]
  (str "environment " environment " and other environment is " other))

(defn ->dependencies [d]
  (:dependencies (meta d)))

(defn reduce-graph
  [graph key d]
  (reduce #(dep/depend %1 key %2) graph (->dependencies d)))

(defn dependency-graph
  [m]
  (reduce-kv reduce-graph (dep/graph) m))

(def dependencies (dependencies-map
                    :environment (using (constantly :sample))
                    :environment-dependent (using some-environment-dependent-logic [:environment])
                    :other (using other-environment-dependent-logic [:environment :environment-dependent])))

(def graph (dependency-graph dependencies))

(defn apply-dependency-fn
  [f dependencies]
  dependencies
  (apply f (vals dependencies)))

(reduce (fn [m k]
          (assoc m k
                   (apply-dependency-fn (k m) (select-keys m (->dependencies (k m))))))
        dependencies
        (sort (dep/topo-comparator graph) (keys dependencies)))



