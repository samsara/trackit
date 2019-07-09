(defn ver []
  (-> "../trackit.version" slurp .trim))

(defproject samsara/trackit-prometheus (ver)

  :description
  "TRACKit! A Clojure developer friendly wrapper for Yammer's Metrics library."

  :url "https://github.com/samsara/trackit"

  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :global-vars {*warn-on-reflection* true}

  :java-source-paths ["src/java"]

  :dependencies [[samsara/trackit-core #=(ver)]

                 [io.prometheus/simpleclient "0.6.0"]
                 [io.prometheus/simpleclient_hotspot "0.6.0"]
                 [io.prometheus/simpleclient_common "0.6.0"]
                 [io.prometheus/simpleclient_pushgateway "0.6.0"]
                 [io.prometheus/simpleclient_dropwizard "0.6.0"]])
