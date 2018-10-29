(defn ver []
  (-> "../trackit.version" slurp .trim))

(defproject samsara/trackit-newrelic (ver)

  :description
  "TRACKit! A Clojure developer friendly wrapper for Yammer's Metrics library."

  :url "https://github.com/samsara/trackit"

  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :global-vars {*warn-on-reflection* true}

  :java-source-paths ["src/java"]

  :dependencies [[samsara/trackit-core #=(ver)]
                 [com.palominolabs.metrics/metrics-new-relic "1.1.1"]
                 ]

  :repositories [["bintray" "https://dl.bintray.com/marshallpierce/maven/"]]
  )
