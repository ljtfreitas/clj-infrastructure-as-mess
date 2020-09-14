(ns infrastructure-as-mess.reader
  (:require [clojure.tools.cli :as cli]
            [schema.coerce :as coerce]))

(defn args->map
  [names args]
  (zipmap (keys names) args))

(defn coerce!
  [schema args]
  ((coerce/coercer schema coerce/string-coercion-matcher) args))

(defn parse-arguments!
  [args {:keys [spec schema]}]
  (let [{:keys [options arguments]} (cli/parse-opts args spec)]
    (->> (args->map options arguments)
         (coerce! schema))))

(defn read-recipe!
  [recipe args]
  (assoc recipe :arguments (parse-arguments! args (:arguments recipe))))
