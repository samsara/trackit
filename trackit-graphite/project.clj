(defproject samsara/trackit-graphite (-> "../trackit.version" slurp .trim)

  :description
  "TRACKit! A Clojure developer friendly wrapper for Yammer's Metrics library."

  :url "https://github.com/samsara/trackit"

  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :dependencies [[metrics-clojure-graphite "2.6.1"]])
