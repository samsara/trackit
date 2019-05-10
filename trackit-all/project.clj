(defn ver []
  (-> "../trackit.version" slurp .trim))

(defproject samsara/trackit-all (ver)

  :description
  "TRACKit! A Clojure developer friendly wrapper for Yammer's Metrics library."

  :url "https://github.com/samsara/trackit"

  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :global-vars {*warn-on-reflection* true}

  :dependencies [[samsara/trackit-core       #=(ver)]
                 [samsara/trackit-cloudwatch #=(ver)]
                 [samsara/trackit-ganglia    #=(ver)]
                 [samsara/trackit-graphite   #=(ver)]
                 [samsara/trackit-influxdb   #=(ver)]
                 [samsara/trackit-newrelic   #=(ver)]
                 [samsara/trackit-riemann    #=(ver)]
                 [samsara/trackit-statsd     #=(ver)]
                 [samsara/trackit-prometheus #=(ver)]])
