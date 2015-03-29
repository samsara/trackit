(defproject samsara/trackit "0.1.0-SNAPSHOT"
  :description "TRACKit! A Clojure wrapper for Yammer Metric library"
  :url "https://github.com/samsara/trackit"
  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [metrics-clojure "2.5.0"]
                 [metrics-clojure-graphite "2.5.0"]
                 [metrics-clojure-ganglia  "2.5.0"]
                 [riemann-clojure-client "0.3.2"]
                 ;; https://github.com/organicveggie/metrics-statsd
                 ;;[com.bealetech/metrics-statsd "3.0.0a"] ;; version not yet released
                 ]

  :plugins [[lein-marginalia "0.8.0"]])
