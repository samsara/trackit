(defn ver []
  (-> "../trackit.version" slurp .trim))

(defproject samsara/trackit-all (ver)

  :description
  "TRACKit! A Clojure developer friendly wrapper for Yammer's Metrics library."

  :url "https://github.com/samsara/trackit"

  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [samsara/trackit-core     #=(ver)]
                 [samsara/trackit-ganglia  #=(ver)]
                 [samsara/trackit-graphite #=(ver)]
                 [samsara/trackit-influxdb #=(ver)]
                 [samsara/trackit-riemann  #=(ver)]
                 [samsara/trackit-statsd   #=(ver)]
                 ])
