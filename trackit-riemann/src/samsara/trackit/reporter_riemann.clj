(ns samsara.trackit.reporter-riemann
  (:import  [java.util.concurrent TimeUnit]
            [com.codahale.metrics MetricFilter]
            [java.net InetAddress UnknownHostException])
  (:require [metrics.reporters.riemann :as riemann]))

(defn- local-host-name []
  (try
    (.getCanonicalHostName (InetAddress/getLocalHost))
    (catch UnknownHostException uhe
      "Unknown Hostname")))

(defn start-reporting
  [registry
   {:keys [local-host reporting-frequency-seconds host port prefix rate-unit duration-unit separator tags]
    :or  {local-host (local-host-name), reporting-frequency-seconds 10, host "localhost", port 5555
          prefix "trackit", rate-unit TimeUnit/SECONDS, duration-unit TimeUnit/MILLISECONDS
          separator "." tags {}} :as cfg}]
  (let [reporter (riemann/reporter (riemann/make-riemann host port) registry (assoc cfg :host-name local-host))]
    (riemann/start reporter reporting-frequency-seconds)
    (fn [] (riemann/stop reporter))))
