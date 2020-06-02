(ns infrastructure-as-mess.dsl.ingredients.aws)

(defn- ->id [ns name]
  (keyword (clojure.core/name ns) (clojure.core/name name)))

(defn s3-bucket
  ([name]
   (s3-bucket name {}))
  ([name properties]
   (fn [_]
     {:id         (->id :s3 name)
      :name       name
      :target     :aws-platform
      :kind       :s3
      :properties (merge properties {:type        "AWS::S3::Bucket"
                                     :bucket-name name})})))

(defn cloud-formation-stack [name description & resources]
  (fn [{:keys [environment] :as dependencies}]
    {:id          (->id :cloud-formation name)
     :name        name
     :description description
     :environment environment
     :target      :aws-platform
     :kind        :cloud-formation
     :resources   (map #(apply % [dependencies]) resources)}))
