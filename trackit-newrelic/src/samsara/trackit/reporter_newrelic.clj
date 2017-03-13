(ns samsara.trackit.reporter-newrelic
  (:import  [java.util.concurrent TimeUnit]
            [com.codahale.metrics MetricFilter])

  (:require [metrics.reporters :as mrep]
            [clojure.string :as str])
  (:import java.util.concurrent.TimeUnit
           [com.codahale.metrics Metric MetricRegistry]
           [com.palominolabs.metrics.newrelic NewRelicReporter]))


(defn ^NewRelicReporter newrelic-reporter
  ([^MetricRegistry reg {:keys [reporter-name reporting-frequency-seconds prefix rate-unit duration-unit]
                         :or  {reporting-frequency-seconds 30, reporter-name "trackit-reporter"
                               prefix "trackit/", rate-unit TimeUnit/SECONDS, duration-unit TimeUnit/MILLISECONDS
                               } :as cfg}]
   (let [b (NewRelicReporter/forRegistry reg)]
     (.name b reporter-name)
     (when-let [^String p (-> (str prefix "/") (str/replace #"\." "/") (str/replace #"//+" "/"))]
       (.metricNamePrefix b p))
     (when rate-unit
       (.rateUnit b rate-unit))
     (when duration-unit
       (.durationUnit b duration-unit))
     (.build b))))

(defn start
  "Report all metrics to NewRelic periodically."
  [^NewRelicReporter r ^long seconds]
  (mrep/start r seconds))

(defn stop
  "Stops reporting."
  [^NewRelicReporter r]
  (mrep/stop r))



(defn start-reporting
  [registry
   {:keys [reporting-frequency-seconds prefix rate-unit duration-unit]
    :or  {reporting-frequency-seconds 10, prefix "trackit"
          rate-unit TimeUnit/SECONDS, duration-unit TimeUnit/MILLISECONDS} :as cfg}]
  (let [reporter (newrelic-reporter registry cfg)]
    (start reporter reporting-frequency-seconds)
    (fn [] (stop reporter))))
