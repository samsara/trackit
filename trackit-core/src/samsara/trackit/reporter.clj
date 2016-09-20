(ns samsara.trackit.reporter
  (:import  [java.util.concurrent TimeUnit]
            [com.codahale.metrics MetricFilter])
  (:require [metrics.reporters.console :as console]))


(defmulti start-reporting (fn [registry cfg] (:type cfg)))


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



(defmethod start-reporting :default [registry cfg]
  (println "TRACKit!: no reporting method selected."))


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
  (console/start
   (console/reporter registry cfg)
   reporting-frequency-seconds))



(defmethod start-reporting :graphite
  [registry
   {:keys [reporting-frequency-seconds host port prefix rate-unit duration-unit]
    :or  {reporting-frequency-seconds 10, host "localhost", port 2003, prefix "trackit"
          rate-unit TimeUnit/SECONDS, duration-unit TimeUnit/MILLISECONDS} :as cfg}]

  (let [reporter (load-function-from-name "samsara.trackit.reporter-graphite/start-reporting")]
    (reporter registry cfg)))



(defmethod start-reporting :statsd
  [registry
   {:keys [reporting-frequency-seconds host port prefix rate-unit duration-unit]
    :or  {reporting-frequency-seconds 10, host "localhost", port 8125, prefix "trackit"
          rate-unit TimeUnit/SECONDS, duration-unit TimeUnit/MILLISECONDS} :as cfg}]

  (let [reporter (load-function-from-name "samsara.trackit.reporter-statsd/start-reporting")]
    (reporter registry cfg)))



(defmethod start-reporting :riemann
  [registry
   {:keys [reporting-frequency-seconds host port prefix rate-unit duration-unit]
    :or  {reporting-frequency-seconds 10, host "localhost", port 5555, prefix "trackit"
          rate-unit TimeUnit/SECONDS, duration-unit TimeUnit/MILLISECONDS} :as cfg}]

  (let [reporter (load-function-from-name "samsara.trackit.reporter-riemann/start-reporting")]
    (reporter registry cfg)))



(defmethod start-reporting :ganglia
  [registry
   {:keys [reporting-frequency-seconds host port prefix rate-unit duration-unit]
    :or  {reporting-frequency-seconds 60, host "localhost", port 8649, prefix "trackit"
          rate-unit TimeUnit/SECONDS, duration-unit TimeUnit/MILLISECONDS} :as cfg}]

  (let [reporter (load-function-from-name "samsara.trackit.reporter-ganglia/start-reporting")]
    (reporter registry cfg)))
