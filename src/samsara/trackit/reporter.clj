(ns samsara.trackit.reporter
  (:require [metrics.reporters.graphite :as graphite]
            [metrics.reporters.console :as console])
  (:import  [java.util.concurrent TimeUnit])
  (:import  [com.codahale.metrics MetricFilter]))

(defmulti start-reporting (fn [registry cfg] (:type cfg)))



(defmethod start-reporting :default [registry cfg]
  (println "TRACKit!: no reporting method selected."))



(defmethod start-reporting :console
  [registry
   {:keys [reporting-frequency-seconds stream rate-unit duration-unit]
    :or  {reporting-frequency-seconds 300, stream (System/err)
          rate-unit TimeUnit/SECONDS, duration-unit TimeUnit/MILLISECONDS} :as cfg}]
  (console/start
   (console/reporter registry cfg)
   reporting-frequency-seconds))



(defmethod start-reporting :graphite
  [registry
   {:keys [reporting-frequency-seconds host port prefix rate-unit duration-unit]
    :or  {reporting-frequency-seconds 10, host "localhost", port 2003, prefix "trackit"
          rate-unit TimeUnit/SECONDS, duration-unit TimeUnit/MILLISECONDS} :as cfg}]

  (graphite/start
   (graphite/reporter registry cfg)
   reporting-frequency-seconds))
