(ns samsara.trackit.reporter-graphite
  (:import  [java.util.concurrent TimeUnit]
            [com.codahale.metrics MetricFilter])
  (:require [metrics.reporters.graphite :as graphite]))


(defn start-reporting
  [registry
   {:keys [reporting-frequency-seconds host port prefix rate-unit duration-unit]
    :or  {reporting-frequency-seconds 10, host "localhost", port 2003, prefix "trackit"
          rate-unit TimeUnit/SECONDS, duration-unit TimeUnit/MILLISECONDS} :as cfg}]
  (graphite/start
   (graphite/reporter registry cfg)
   reporting-frequency-seconds))
