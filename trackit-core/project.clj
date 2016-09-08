(defproject samsara/trackit-core (-> "../trackit.version" slurp .trim)

  :description
  "TRACKit! A Clojure developer friendly wrapper for Yammer's Metrics library."

  :url "https://github.com/samsara/trackit"

  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [metrics-clojure "2.6.1"]
                 [metrics-clojure-jvm "2.6.1"]]

  :plugins [[lein-marginalia "0.8.0"]])
