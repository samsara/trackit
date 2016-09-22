(ns samsara.trackit.reporter-influxdb
  (:import  [java.util.concurrent TimeUnit]
            [com.codahale.metrics MetricFilter])
  ;; metrics.reporters.influxdb not release yet
  ;;(:require [metrics.reporters.influxdb :as influx])


  (:require [metrics.reporters :as mrep])
  (:import java.util.concurrent.TimeUnit
           [com.codahale.metrics Metric MetricRegistry]
           [com.google.common.collect ImmutableMap ImmutableSet]
           [com.izettle.metrics.influxdb InfluxDbReporter]
           [com.izettle.metrics.dw InfluxDbReporterFactory]
           [io.dropwizard.util Duration])
  )

(comment
  (defn start-reporting
    [registry
     {:keys [reporting-frequency-seconds host port prefix rate-unit duration-unit
             tags fields dbname auth connect-timeout read-timeout]
      :or  {reporting-frequency-seconds 10, host "localhost", port 8086, prefix "trackit"
            rate-unit TimeUnit/SECONDS, duration-unit TimeUnit/MILLISECONDS} :as cfg}]
    (influx/start
     (influx/reporter registry cfg)
     reporting-frequency-seconds)))


;;
;; Tanken from https://github.com/sjl/metrics-clojure/tree/master/metrics-clojure-influxdb
;;

(defn ^InfluxDbReporter reporter
  ([^MetricRegistry reg {:keys [host hostname port prefix tags fields auth connect-timeout read-timeout
                                group-guages duration-unit measurement-mappings db-name excludes] :as opts
                         :or {port 8086
                              db-name "metrics"}}]
   (let [b (InfluxDbReporterFactory.)]
     (.setHost b (or host hostname "localhost"))
     (.setPort b port)
     (.setDatabase b db-name)
     (when-let [^String p prefix]
       (.setPrefix b p))
     (when-let [^java.util.Map t tags]
       (.setTags b t))
     (when-let [^java.util.Map f fields]
       (.setFields b (ImmutableMap/copyOf f)))
     (when-let [^String a auth]
       (.setAuth b a))
     (when connect-timeout
       (.setConnectTimeout b connect-timeout))
     (when read-timeout
       (.setConnectTimeout b read-timeout))
     (when group-guages
       (.setGroupGuages b group-guages))
     (when duration-unit
       (.setPrecision b (Duration/milliseconds 1)))
     (when-let [^java.util.Map mm measurement-mappings]
       (.setMeasurementMappings b (ImmutableMap/copyOf mm)))
     (when-let [^java.util.Set e excludes]
       (.setExcludes b (ImmutableSet/copyOf e)))
     (.build b reg))))

(defn start
  "Report all metrics to influxdb periodically."
  [^InfluxDbReporter r ^long seconds]
  (mrep/start r seconds))

(defn stop
  "Stops reporting."
  [^InfluxDbReporter r]
  (mrep/stop r))



(defn start-reporting
  [registry
   {:keys [reporting-frequency-seconds host port prefix rate-unit duration-unit
           tags fields dbname auth connect-timeout read-timeout]
    :or  {reporting-frequency-seconds 10, host "localhost", port 8086, prefix "trackit"
          rate-unit TimeUnit/SECONDS, duration-unit TimeUnit/MILLISECONDS} :as cfg}]
  (start
   (reporter registry cfg)
   reporting-frequency-seconds))
