(defproject samsara/trackit-ganglia (-> "../trackit.version" slurp .trim)

  :description
  "TRACKit! A Clojure developer friendly wrapper for Yammer's Metrics library."

  :url "https://github.com/samsara/trackit"

  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :dependencies [[samsara/trackit-core #=(slurp "../trackit.version")]
                 ;;[metrics-clojure-ganglia  "2.7.0"] ;; bug #71
                 [io.dropwizard.metrics/metrics-ganglia "3.1.1"]
                 [info.ganglia.gmetric4j/gmetric4j "1.0.10"]])
