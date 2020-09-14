(ns infrastructure-as-mess.main
  (:require [infrastructure-as-mess.meta.reader :as r]))

(defn apply-interceptors!
  [arguments interceptors]
  (reduce #(apply %2 [%1]) arguments (or interceptors [])))

(defn resolve-dependencies!
  [dependencies])

(defn eval-recipe
  [{:keys [arguments dependencies interceptors]}]
  (let [context      (apply-interceptors! arguments interceptors)
        dependencies (resolve-dependencies! dependencies)]))

(defn -main [& args]
  (eval-recipe
    (r/read-recipe! (load-file "src/infrastructure_as_mess/meta/recipe.clj") args)))
