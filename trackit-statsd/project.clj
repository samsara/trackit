(defproject samsara/trackit-statsd (-> "../trackit.version" slurp .trim)

  :description
  "TRACKit! A Clojure developer friendly wrapper for Yammer's Metrics library."

  :url "https://github.com/samsara/trackit"

  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :dependencies [;; https://github.com/organicveggie/metrics-statsd
                 [com.bealetech/metrics-statsd "3.0.0a"] ;; version not yet released
                 ]

  :plugins [[lein-marginalia "0.8.0"]])
