(defproject samsara/trackit "0.2.3"
  :description "TRACKit! A Clojure developer friendly wrapper for Yammer's Metrics library."
  :url "https://github.com/samsara/trackit"
  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [metrics-clojure "2.5.0"]
                 [metrics-clojure-graphite "2.5.0"]
                 #_[metrics-clojure-ganglia  "2.5.0"] ;; bug #71
                 [io.dropwizard.metrics/metrics-ganglia "3.1.1"]
                 [info.ganglia.gmetric4j/gmetric4j "1.0.10"]
                 [com.aphyr/riemann-java-client "0.4.1"]
                 [com.aphyr/metrics3-riemann-reporter "0.4.1"]
                 ;; https://github.com/organicveggie/metrics-statsd
                 ;;[com.bealetech/metrics-statsd "3.0.0a"] ;; version not yet released
                 ]

  :plugins [[lein-marginalia "0.8.0"]])
