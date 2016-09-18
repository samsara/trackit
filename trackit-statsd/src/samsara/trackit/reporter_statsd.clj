(ns samsara.trackit.reporter
  (:import  [java.util.concurrent TimeUnit]
            [com.codahale.metrics MetricFilter])
  (:require [metrics.reporters.console :as console]
            #_[metrics.reporters.ganglia :as ganglia])
  #_(:require [metrics.reporters.graphite :as graphite]
            #_[metrics.reporters.ganglia :as ganglia])
  #_(:import  [com.codahale.metrics.ganglia GangliaReporter]
            [info.ganglia.gmetric4j.gmetric GMetric GMetric$UDPAddressingMode])
  #_(:import  [com.codahale.metrics.riemann RiemannReporter Riemann]
            [com.aphyr.riemann.client RiemannClient TcpTransport])

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


(comment
  (defmethod start-reporting :graphite
    [registry
     {:keys [reporting-frequency-seconds host port prefix rate-unit duration-unit]
      :or  {reporting-frequency-seconds 10, host "localhost", port 2003, prefix "trackit"
            rate-unit TimeUnit/SECONDS, duration-unit TimeUnit/MILLISECONDS} :as cfg}]
    (require 'metrics.reporters.graphite)
    (graphite/start
     (graphite/reporter registry cfg)
     reporting-frequency-seconds)))

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


(comment
  (defmethod start-reporting :riemann
    [registry
     {:keys [reporting-frequency-seconds host port prefix rate-unit duration-unit]
      :or  {reporting-frequency-seconds 10, host "localhost", port 5555, prefix "trackit"
            rate-unit TimeUnit/SECONDS, duration-unit TimeUnit/MILLISECONDS} :as cfg}]

    (let [;; tell Riemann client to do not cache the dns name
          rc (Riemann.
              (RiemannClient.
               (doto
                   (TcpTransport. host (int port))
                 (-> .-cacheDns (.set false)))))]
      (-> (RiemannReporter/forRegistry registry)
          (.prefixedWith prefix)
          (.convertDurationsTo duration-unit)
          (.convertRatesTo rate-unit)
          (.useSeparator ".")
          (.filter MetricFilter/ALL)
          (.build rc)
          (.start reporting-frequency-seconds TimeUnit/SECONDS))))



  (defmethod start-reporting :ganglia
    [registry
     {:keys [reporting-frequency-seconds host port prefix rate-unit duration-unit]
      :or  {reporting-frequency-seconds 60, host "localhost", port 8649, prefix "trackit"
            rate-unit TimeUnit/SECONDS, duration-unit TimeUnit/MILLISECONDS} :as cfg}]

    (-> (GangliaReporter/forRegistry registry)
        (.prefixedWith prefix)
        (.convertDurationsTo duration-unit)
        (.convertRatesTo rate-unit)
        (.filter MetricFilter/ALL)
        (.build (GMetric. host (int port) GMetric$UDPAddressingMode/MULTICAST 1))
        (.start reporting-frequency-seconds TimeUnit/SECONDS)))
  )
