(defn ver []
  (-> "../trackit.version" slurp .trim))

(defproject samsara/trackit-statsd (ver)

  :description
  "TRACKit! A Clojure developer friendly wrapper for Yammer's Metrics library."

  :url "https://github.com/samsara/trackit"

  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :dependencies [[samsara/trackit-core #=(ver)]
                 ;; https://github.com/readytalk/metrics-statsd
                 [com.readytalk/metrics3-statsd "4.1.2"]]

  :repositories [["bintray" "http://dl.bintray.com/readytalk/maven"]])
