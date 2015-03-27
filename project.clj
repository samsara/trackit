(defproject samsara/trackit "0.1.0-SNAPSHOT"
  :description "TRACKit! A Clojure wrapper for Yammer Metric library"
  :url "https://github.com/samsara/trackit"
  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [metrics-clojure "2.4.0"]
                 [metrics-clojure-graphite "2.4.0"]
                 ]
  :plugins [[lein-marginalia "0.8.0"]])
