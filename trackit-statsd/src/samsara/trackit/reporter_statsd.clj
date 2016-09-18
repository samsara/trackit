(ns samsara.trackit.reporter-statsd
  (:import  [java.util.concurrent TimeUnit]
            [com.codahale.metrics MetricFilter])
  (:require [samsara.trackit.reporter])
  (:import  [com.bealetech.metrics.reporting StatsdReporter Statsd]))


(defmethod samsara.trackit.reporter/start-reporting :statsd
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
      (.start reporting-frequency-seconds TimeUnit/SECONDS)))
