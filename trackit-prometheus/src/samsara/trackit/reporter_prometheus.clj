(ns samsara.trackit.reporter-prometheus
  (:require [clojure.string :as str]
            [metrics.reporters :as mr])
  (:import [com.codahale.metrics MetricFilter MetricRegistry]
           io.prometheus.client.dropwizard.DropwizardExports
           io.prometheus.client.exporter.PushGateway
           java.net.URL
           java.util.concurrent.TimeUnit
           samsara.PrometheusReporter))

;; Metrics sent to a PushGateway need to be distinguishable process by process
;; this ensures that all processes have their unique process id
;; this will be injected as a grouping key `:pid`.
(defonce pid (str (java.util.UUID/randomUUID)))


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


(defn push-gateway
  [push-gateway-url]
  (if (re-matches #"^(http|https)://.*" push-gateway-url)
    (PushGateway. ^URL (URL. push-gateway-url))
    (PushGateway. ^String push-gateway-url)))



(defn prometheus-reporter
  [^MetricRegistry reg
   {:keys [reporter-name  push-gateway-url rate-unit duration-unit grouping-keys inject-pid?]
    :or {rate-unit TimeUnit/SECONDS, duration-unit TimeUnit/MILLISECONDS inject-pid? true} :as opts}]
  (let [^Collector collector         (DropwizardExports. reg)
        ^PushGateway push-gateway    (push-gateway push-gateway-url)
        ;; inject unique `:pid` if not provided
        grouping-keys (if inject-pid? (update grouping-keys :pid (fnil identity pid)) grouping-keys)]
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
