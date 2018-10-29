(defn ver []
  (-> "../trackit.version" slurp .trim))

(defproject samsara/trackit-riemann (ver)

  :description
  "TRACKit! A Clojure developer friendly wrapper for Yammer's Metrics library."

  :url "https://github.com/samsara/trackit"

  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :global-vars {*warn-on-reflection* true}

  :dependencies [[samsara/trackit-core #=(ver)]
                 [metrics-clojure-riemann "2.10.0"]])
