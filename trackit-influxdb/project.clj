(defn ver []
  (-> "../trackit.version" slurp .trim))

(defproject samsara/trackit-influxdb (ver)

  :description
  "TRACKit! A Clojure developer friendly wrapper for Yammer's Metrics library."

  :url "https://github.com/samsara/trackit"

  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :dependencies [[samsara/trackit-core #=(ver)]
                 ;;[metrics-clojure-influxdb "2.7.0"]
                 [com.izettle/dropwizard-metrics-influxdb "1.1.6"]
                 ])
