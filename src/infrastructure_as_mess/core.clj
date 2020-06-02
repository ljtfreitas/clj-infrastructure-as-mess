(ns infrastructure-as-mess.core
  (:require [cats.core :as m]
            [cats.monad.maybe :as maybe]
            [cats.monad.exception :as ex]
            [infrastructure-as-mess.protocols.platform :as protocols.platform]
            [infrastructure-as-mess.system :as system]))

(defn- resolve-dependency [components m lazy-dependency]
  (m/fmap (partial merge m)
          (ex/try-on
            (let [[name fn] lazy-dependency]
              {name (apply fn [components])}))))

(defn- dependencies [dependencies-map components]
  (m/foldm maybe/context
           (partial resolve-dependency components)
           {}
           (->> dependencies-map
                (map identity))))

(defn- ->ingredient [dependencies ingredient-fn]
  (apply ingredient-fn [dependencies]))

(defn prepare-ingredients [ingredients dependencies]
  (m/return
    (map (partial ->ingredient dependencies) ingredients)))

(defn- ->resources [ingredients]
  (group-by :target ingredients))

(defn- ->dish [recipe components]
  (m/mlet [dependencies (dependencies (:dependencies recipe) components)
           ingredients  (prepare-ingredients (:ingredients recipe) dependencies)]
          (m/return
            (merge recipe {:dependencies dependencies
                           :ingredients  (->resources ingredients)}))))

(defn- deploy-fn! [platform resources]
  (ex/wrap
    (protocols.platform/deploy-all! platform resources)))

(defn- deploy-all! [resources components]
  (for [platform-name (keys resources)]
    (apply (deploy-fn! (get components platform-name)
                       (get resources platform-name)) [components])))

(defn cook!* [dish components]
  (m/mlet [ingredients (m/fmap :ingredients dish)]
          (m/sequence (deploy-all! ingredients components))))

(defn cook! [recipe]
  (-> recipe
      (->dish system/components)
      (cook!* system/components)
      (#(m/fmap flatten %))))

(defn dish [recipe]
  (->dish recipe system/components))