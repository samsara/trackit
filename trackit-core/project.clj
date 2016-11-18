(defn ver []
  (-> "../trackit.version" slurp .trim))

(defproject samsara/trackit-core (ver)

  :description
  "TRACKit! A Clojure developer friendly wrapper for Yammer's Metrics library."

  :url "https://github.com/samsara/trackit"

  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [metrics-clojure "2.7.0"]
                 [metrics-clojure-jvm "2.7.0"]]

  :plugins [[lein-marginalia "0.8.0"]])
