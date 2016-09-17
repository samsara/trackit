(ns samsara.trackit.reporter-riemann
  (:import  [java.util.concurrent TimeUnit]
            [com.codahale.metrics MetricFilter])
  (:require [samsara.trackit.reporter])
  (:import  [com.codahale.metrics.riemann RiemannReporter Riemann]
            [com.aphyr.riemann.client RiemannClient TcpTransport]))


(defmethod samsara.trackit.reporter/start-reporting :riemann
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
