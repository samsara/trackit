(ns samsara.trackit.reporter
  (:import  [java.util.concurrent TimeUnit]
            [com.codahale.metrics MetricFilter])
  (:require [metrics.reporters.console :as console]))


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

)

(comment
  (defmethod start-reporting :statsd
    [registry
     {:keys [reporting-frequency-seconds host port prefix rate-unit duration-unit]
      :or  {reporting-frequency-seconds 10, host "localhost", port 8125, prefix "trackit"
            rate-unit TimeUnit/SECONDS, duration-unit TimeUnit/MILLISECONDS} :as cfg}]

))


(defmethod start-reporting :riemann
  [registry
   {:keys [reporting-frequency-seconds host port prefix rate-unit duration-unit]
    :or  {reporting-frequency-seconds 10, host "localhost", port 5555, prefix "trackit"
          rate-unit TimeUnit/SECONDS, duration-unit TimeUnit/MILLISECONDS} :as cfg}]

)



(defmethod start-reporting :ganglia
  [registry
   {:keys [reporting-frequency-seconds host port prefix rate-unit duration-unit]
    :or  {reporting-frequency-seconds 60, host "localhost", port 8649, prefix "trackit"
          rate-unit TimeUnit/SECONDS, duration-unit TimeUnit/MILLISECONDS} :as cfg}]

)
