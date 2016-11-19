(defn ver []
  (-> "../trackit.version" slurp .trim))

(defproject samsara/trackit-statsd (ver)

  :description
  "TRACKit! A Clojure developer friendly wrapper for Yammer's Metrics library."

  :url "https://github.com/samsara/trackit"

  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :dependencies [[samsara/trackit-core #=(ver)]
                 ;; https://github.com/organicveggie/metrics-statsd
                 [com.bealetech/metrics-statsd "2.3.0"]]

  :plugins [[lein-marginalia "0.8.0"]])
