(defproject samsara/trackit-all (-> "../trackit.version" slurp .trim)

  :description
  "TRACKit! A Clojure developer friendly wrapper for Yammer's Metrics library."

  :url "https://github.com/samsara/trackit"

  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [samsara/trackit-core     #=(slurp "../trackit.version")]
                 [samsara/trackit-ganglia  #=(slurp "../trackit.version")]
                 [samsara/trackit-graphite #=(slurp "../trackit.version")]
                 [samsara/trackit-influxdb #=(slurp "../trackit.version")]
                 [samsara/trackit-riemann  #=(slurp "../trackit.version")]
                 [samsara/trackit-statsd   #=(slurp "../trackit.version")]
                 ])
