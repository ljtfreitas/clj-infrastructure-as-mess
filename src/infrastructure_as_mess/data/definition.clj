(ns infrastructure-as-mess.data.definition)

(defn read! [resource resource-name]
  (fn [{:keys [definition]}]
    (get-in (-> definition resource) [resource-name])))
