(ns infrastructure-as-mess.dsl.recipe)

(defmacro recipe [name description dependencies & ingredients]
  {:name        name
   :description description
   :dependencies dependencies
   :ingredients `(flatten [~@ingredients])})

(defmacro dependencies [& forms]
  `(into {} (flatten [~@forms])))