(ns samsara.trackit.reporter-prometheus
  (:import  [java.util.concurrent TimeUnit]
            [com.codahale.metrics MetricFilter MetricRegistry ScheduledReporter]
            [io.prometheus.client.exporter PushGateway]
            [io.prometheus.client.dropwizard DropwizardExports]
            [samsara PrometheusReporter]
            [java.util HashMap])
  (:require [samsara.trackit :as t]
            [metrics.reporters :as mr]
            [clojure.tools.logging :as log]
            [clojure.string :as str]))

(defn sanitize-grouping-key
  "Prometheus does not like characters other than alpha-numeric and
  underscores. Replace every other character with _"
  [k]
  (str/replace k #"[^a-zA-Z0-9_]" "_"))


(defn sanitize-grouping-keys
  "sanitize the key and the value of the grouping key map to prometheus
  friendly values"
  [g]
  (reduce (fn [m [k v]]
       (assoc
        m
        (sanitize-grouping-key (name k))
        (sanitize-grouping-key v)))
     {}
     g))



(defn prometheus-reporter
  [^MetricRegistry reg
   {:keys [reporter-name  push-gateway-url rate-unit duration-unit grouping-keys]
    :or {rate-unit TimeUnit/SECONDS, duration-unit TimeUnit/MILLISECONDS} :as opts}]
  (let [^Collector collector         (DropwizardExports. reg)
        ^PushGateway push-gateway    (PushGateway. push-gateway-url)]
    (PrometheusReporter. reg
                         (sanitize-grouping-key reporter-name)
                         MetricFilter/ALL
                         rate-unit
                         duration-unit
                         push-gateway
                         collector
                         (sanitize-grouping-keys grouping-keys))))



(defn start-reporting
  [registry
   {:keys [reporting-frequency-seconds host port prefix rate-unit duration-unit]
    :or  {reporting-frequency-seconds 10 rate-unit TimeUnit/SECONDS,
          duration-unit TimeUnit/MILLISECONDS} :as cfg}]
  (let [^PrometheusReporter reporter (prometheus-reporter registry cfg)]
    (mr/start reporter reporting-frequency-seconds)
    (fn [] (.stop reporter))))
