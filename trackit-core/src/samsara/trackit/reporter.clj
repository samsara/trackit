(ns samsara.trackit.reporter
  (:import  [java.util.concurrent TimeUnit]
            [com.codahale.metrics MetricFilter])
  (:require [metrics.reporters.console :as console]
            [clojure.tools.logging :as log]))


(defmulti start-reporting
  "Starts the reporting with the given configuration for the `registry`.
   It returns a function which when called it stops the reporter."
  (fn [registry cfg] (:type cfg)))



(defn- load-function-from-name
  ([fqn-fname]
   (if (string? fqn-fname)
     (let [[_ fns ff] (re-find #"([^/]+)/([^/]+)" fqn-fname)]
       (when (not (and fns ff))
         (throw (ex-info (str "function '" fqn-fname "' is invalid format. must be \"namespace/fun-name\".") {})))
       (load-function-from-name fns ff))
     fqn-fname))
  ([fn-ns fn-name]
   (when (not (and fn-ns fn-name))
     (throw (ex-info (str "function '" fn-ns "/" fn-name "' not found.") {})))
   ;; requiring the namespace
   (require (symbol fn-ns))
   (let [fn-symbol (resolve (symbol fn-ns fn-name))]
     (when-not fn-symbol
       (throw (ex-info (str "function '" fn-ns "/" fn-name "' not found.") {})))
     fn-symbol)))



(defn- reporting-error [type cause]
  (throw
   (ex-info
    (str "Unable to load appropriate reporter."
         " Please ensure you have the followin dependency "
         "[samsara/trackit-" (name type) " \"x.y.z\"]"
         " in your project.clj")
    {:type type} cause)))



(defn- load-dynamic-reporter
  [reporter-name registry cfg]
  (try
    (let [reporter (load-function-from-name reporter-name)]
      (reporter registry cfg))
    (catch Exception x
      (reporting-error (:type cfg) x))))



(defmethod start-reporting :default [registry cfg]
  (log/warn "TRACKit!: Invalid or no reporting method selected."))



(defmethod start-reporting :custom
  [registry
   {:keys [fqn-start-function] :as cfg}]

  (let [reporter (load-function-from-name fqn-start-function)]
    (reporter registry cfg)))



(defmethod start-reporting :console
  [registry
   {:keys [reporting-frequency-seconds stream rate-unit duration-unit]
    :or  {reporting-frequency-seconds 300, stream (System/err)
          rate-unit TimeUnit/SECONDS, duration-unit TimeUnit/MILLISECONDS} :as cfg}]
  (let [reporter (console/reporter registry cfg)]
    (console/start reporter reporting-frequency-seconds)
    (fn [] (console/stop reporter))))



(defmethod start-reporting :graphite
  [registry
   {:keys [reporting-frequency-seconds host port prefix rate-unit duration-unit]
    :or  {reporting-frequency-seconds 10, host "localhost", port 2003, prefix "trackit"
          rate-unit TimeUnit/SECONDS, duration-unit TimeUnit/MILLISECONDS} :as cfg}]

  (load-dynamic-reporter "samsara.trackit.reporter-graphite/start-reporting"
                         registry
                         cfg))



(defmethod start-reporting :statsd
  [registry
   {:keys [reporting-frequency-seconds host port prefix rate-unit duration-unit]
    :or  {reporting-frequency-seconds 10, host "localhost", port 8125, prefix "trackit"
          rate-unit TimeUnit/SECONDS, duration-unit TimeUnit/MILLISECONDS} :as cfg}]

  (load-dynamic-reporter "samsara.trackit.reporter-statsd/start-reporting"
                         registry
                         cfg))



(defmethod start-reporting :riemann
  [registry
   {:keys [reporting-frequency-seconds host port prefix rate-unit duration-unit]
    :or  {reporting-frequency-seconds 10, host "localhost", port 5555, prefix "trackit"
          rate-unit TimeUnit/SECONDS, duration-unit TimeUnit/MILLISECONDS} :as cfg}]

  (load-dynamic-reporter "samsara.trackit.reporter-riemann/start-reporting"
                         registry
                         cfg))



(defmethod start-reporting :ganglia
  [registry
   {:keys [reporting-frequency-seconds host port prefix rate-unit duration-unit]
    :or  {reporting-frequency-seconds 60, host "localhost", port 8649, prefix "trackit"
          rate-unit TimeUnit/SECONDS, duration-unit TimeUnit/MILLISECONDS} :as cfg}]

  (load-dynamic-reporter "samsara.trackit.reporter-ganglia/start-reporting"
                         registry
                         cfg))



(defmethod start-reporting :influxdb
  [registry
   {:keys [reporting-frequency-seconds host port prefix rate-unit duration-unit
           tags fields dbname auth connect-timeout read-timeout]
    :or  {reporting-frequency-seconds 10, host "localhost", port 8086, prefix "trackit"
          rate-unit TimeUnit/SECONDS, duration-unit TimeUnit/MILLISECONDS} :as cfg}]

  (load-dynamic-reporter "samsara.trackit.reporter-influxdb/start-reporting"
                         registry
                         cfg))


(defmethod start-reporting :newrelic
  [registry
   {:keys [reporter-name reporting-frequency-seconds metrics-attribute-filter
           prefix rate-unit duration-unit]
    :or  {reporting-frequency-seconds 30, reporter-name "trackit-reporter"
          metrics-attribute-filter (constantly true)
          prefix "trackit/", rate-unit TimeUnit/SECONDS, duration-unit TimeUnit/MILLISECONDS
          } :as cfg}]

  (load-dynamic-reporter "samsara.trackit.reporter-newrelic/start-reporting"
                         registry
                         cfg))

(defmethod start-reporting :cloudwatch
  [registry
   {:keys [namespace async-client reporting-frequency-seconds rate-unit duration-unit
           dry-run]
    :or   {reporting-frequency-seconds 300
           rate-unit TimeUnit/SECONDS duration-unit TimeUnit/MILLISECONDS}
    :as   cfg}]
  (load-dynamic-reporter "samsara.trackit.reporter-cloudwatch/start-reporting"
                         registry
                         cfg))
