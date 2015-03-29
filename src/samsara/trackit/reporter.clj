(ns samsara.trackit.reporter
  (:require [metrics.reporters.graphite :as graphite]
            [metrics.reporters.console :as console]
            [metrics.reporters.ganglia :as ganglia])
  (:import  [java.util.concurrent TimeUnit])
  (:import  [com.codahale.metrics MetricFilter])
  (:import  [java.util.concurrent TimeUnit]
            [com.codahale.metrics.riemann RiemannReporter Riemann]
            [com.aphyr.riemann.client RiemannClient])

  #_(:import  [com.bealetech.metrics.reporting StatsdReporter Statsd]))

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

(comment
  (defmethod start-reporting :statsd
    [registry
     {:keys [reporting-frequency-seconds host port prefix rate-unit duration-unit]
      :or  {reporting-frequency-seconds 10, host "localhost", port 8125, prefix "trackit"
            rate-unit TimeUnit/SECONDS, duration-unit TimeUnit/MILLISECONDS} :as cfg}]

    (-> (StatsdReporter/forRegistry registry)
        (.prefixedWith prefix)
        (.convertDurationsTo duration-unit)
        (.convertRatesTo rate-unit)
        (.filter MetricFilter/ALL)
        (.build (Statsd. host port))
        (.start reporting-frequency-seconds TimeUnit/SECONDS))))



(defmethod start-reporting :riemann
    [registry
     {:keys [reporting-frequency-seconds host port prefix rate-unit duration-unit]
      :or  {reporting-frequency-seconds 10, host "localhost", port 5555, prefix "trackit"
            rate-unit TimeUnit/SECONDS, duration-unit TimeUnit/MILLISECONDS} :as cfg}]

    (-> (RiemannReporter/forRegistry registry)
        (.prefixedWith prefix)
        (.convertDurationsTo duration-unit)
        (.convertRatesTo rate-unit)
        (.useSeparator ".")
        (.filter MetricFilter/ALL)
        (.build (Riemann. host (int port)))
        (.start reporting-frequency-seconds TimeUnit/SECONDS)))



(comment
 (defmethod start-reporting :ganglia
   [registry
    {:keys [reporting-frequency-minutes host port prefix rate-unit duration-unit]
     :or  {reporting-frequency-minutes 1, host "localhost", port 8649, prefix "trackit"
           rate-unit TimeUnit/SECONDS, duration-unit TimeUnit/MILLISECONDS} :as cfg}]

   (ganglia/start
    (ganglia/reporter registry cfg)
    reporting-frequency-minutes)))
