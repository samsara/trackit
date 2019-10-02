(ns samsara.trackit.reporter-jmx
  (:import  [java.util.concurrent TimeUnit]
            [com.codahale.metrics MetricFilter])
  (:require [metrics.reporters.jmx :as jmx]))


(defn start-reporting
  [registry
   {:keys [domain rate-unit duration-unit]
    :or  {domain "trackit"
          rate-unit TimeUnit/SECONDS
          duration-unit TimeUnit/MILLISECONDS} :as cfg}]

  (let [reporter (jmx/reporter registry cfg)]
    (jmx/start reporter)
    (fn [] (jmx/stop reporter))))
