(ns infrastructure-as-mess.dsl)

(defn ->map
  [args]
  (apply array-map args))

(defn ->namespaced
  [& args]
  (keyword (clojure.string/join "." (map name (butlast args))) (name (last args))))

(defmacro defrecipe
  [name description & args]
  `(merge {:name        ~name
           :description ~description}
          ~(->map args)))

(defmacro defingredient
  [platform resource-name description schema]
  `(defn ~resource-name ~description
     [id# & args#]
     {:id          (->namespaced '~platform '~resource-name id#)
      :kind        (->namespaced '~platform '~resource-name)
      :description ~description
      :spec        (->map args#)}))
