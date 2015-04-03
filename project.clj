(defproject samsara/trackit "0.1.0"
  :description "TRACKit! A Clojure wrapper for Yammer Metric library"
  :url "https://github.com/samsara/trackit"
  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [metrics-clojure "2.5.0"]
                 [metrics-clojure-graphite "2.5.0"]
                 #_[metrics-clojure-ganglia  "2.5.0"] ;; bug #71
                 [io.dropwizard.metrics/metrics-ganglia "3.1.1"]
                 [info.ganglia.gmetric4j/gmetric4j "1.0.10"]
                 [riemann-clojure-client "0.3.2"]
                 ;; https://github.com/organicveggie/metrics-statsd
                 ;;[com.bealetech/metrics-statsd "3.0.0a"] ;; version not yet released
                 ]

  :plugins [[lein-marginalia "0.8.0"]]

  :deploy-repositories[["clojars" {:url "https://clojars.org/repo/"                                                       :sign-releases false}]])
