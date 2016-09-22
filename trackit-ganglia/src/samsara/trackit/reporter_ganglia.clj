(ns samsara.trackit.reporter-ganglia
  (:import  [java.util.concurrent TimeUnit]
            [com.codahale.metrics MetricFilter])
  (:import  [com.codahale.metrics.ganglia GangliaReporter]
            [info.ganglia.gmetric4j.gmetric GMetric GMetric$UDPAddressingMode]))


(defn start-reporting
  [registry
   {:keys [reporting-frequency-seconds host port prefix rate-unit duration-unit]
    :or  {reporting-frequency-seconds 60, host "localhost", port 8649, prefix "trackit"
          rate-unit TimeUnit/SECONDS, duration-unit TimeUnit/MILLISECONDS} :as cfg}]

  (-> (GangliaReporter/forRegistry registry)
      (.prefixedWith prefix)
      (.convertDurationsTo duration-unit)
      (.convertRatesTo rate-unit)
      (.filter MetricFilter/ALL)
      (.build (GMetric. host (int port) GMetric$UDPAddressingMode/UNICAST 300 true))
      (.start reporting-frequency-seconds TimeUnit/SECONDS)))
