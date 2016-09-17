(ns samsara.trackit.reporter-graphite
  (:import  [java.util.concurrent TimeUnit]
            [com.codahale.metrics MetricFilter])
  (:require [samsara.trackit.reporter]
            [metrics.reporters.graphite :as graphite]))


(defmethod samsara.trackit.reporter/start-reporting :graphite
  [registry
   {:keys [reporting-frequency-seconds host port prefix rate-unit duration-unit]
    :or  {reporting-frequency-seconds 10, host "localhost", port 2003, prefix "trackit"
          rate-unit TimeUnit/SECONDS, duration-unit TimeUnit/MILLISECONDS} :as cfg}]
  (require 'metrics.reporters.graphite)
  (graphite/start
   (graphite/reporter registry cfg)
   reporting-frequency-seconds))
