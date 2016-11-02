(ns samsara.trackit.reporter-statsd
  (:import  [java.util.concurrent TimeUnit]
            [com.codahale.metrics MetricFilter])
  (:import  [com.bealetech.metrics.reporting StatsdReporter Statsd]))


(defn start-reporting
  [registry
   {:keys [reporting-frequency-seconds host port prefix rate-unit duration-unit]
    :or  {reporting-frequency-seconds 10, host "localhost", port 8125, prefix "trackit"
          rate-unit TimeUnit/SECONDS, duration-unit TimeUnit/MILLISECONDS} :as cfg}]

  (let [reporter (-> (StatsdReporter/forRegistry registry)
                     (.prefixedWith prefix)
                     (.convertDurationsTo duration-unit)
                     (.convertRatesTo rate-unit)
                     (.filter MetricFilter/ALL)
                     (.build (Statsd. host port)))]
    (.start reporter reporting-frequency-seconds TimeUnit/SECONDS)
    (fn [] (.stop reporter))))
