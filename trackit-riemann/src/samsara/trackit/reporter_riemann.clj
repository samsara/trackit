(ns samsara.trackit.reporter-riemann
  (:import  [java.util.concurrent TimeUnit]
            [com.codahale.metrics MetricFilter])
  (:require [metrics.reporters.riemann :as riemann]))


(defn start-reporting
  [registry
   {:keys [reporting-frequency-seconds host port prefix rate-unit duration-unit separator tags]
    :or  {reporting-frequency-seconds 10, host "localhost", port 5555, prefix "trackit"
          rate-unit TimeUnit/SECONDS, duration-unit TimeUnit/MILLISECONDS
          separator "." tags {}} :as cfg}]
  (let [reporter (riemann/reporter (riemann/make-riemann host port) registry (assoc cfg :host-name host))]
    (riemann/start reporter reporting-frequency-seconds)
    (fn [] (riemann/stop reporter))))
