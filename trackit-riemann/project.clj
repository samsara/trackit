(defproject samsara/trackit-riemann (-> "../trackit.version" slurp .trim)

  :description
  "TRACKit! A Clojure developer friendly wrapper for Yammer's Metrics library."

  :url "https://github.com/samsara/trackit"

  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :dependencies [[samsara/trackit-core #=(slurp "../trackit.version")]
                 [metrics-clojure-riemann "2.7.0"]])
