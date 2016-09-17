(ns samsara.trackit.reporter-ganglia
  (:import  [java.util.concurrent TimeUnit]
            [com.codahale.metrics MetricFilter])
  (:require [samsara.trackit.reporter]
            [metrics.reporters.ganglia :as ganglia])
  (:import  [com.codahale.metrics.ganglia GangliaReporter]
            [info.ganglia.gmetric4j.gmetric GMetric GMetric$UDPAddressingMode]))


(defmethod samsara.trackit.reporter/start-reporting :ganglia
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
