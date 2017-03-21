(ns samsara.trackit.reporter-newrelic
  (:import  [java.util.concurrent TimeUnit]
            [com.codahale.metrics MetricFilter])

  (:require [metrics.reporters :as mrep]
            [clojure.string :as str])
  (:import java.util.concurrent.TimeUnit
           [com.codahale.metrics Metric MetricRegistry]
           [com.palominolabs.metrics.newrelic
            MetricAttributeFilter]
           [samsara NewRelicReporter]))


(def ^:const all-metrics-attributes
  #{:timer-min
    :timer-max
    :timer-mean
    :timer-std-dev
    :timer-median
    :timer75th-percentile
    :timer95th-percentile
    :timer98th-percentile
    :timer99th-percentile
    :timer999th-percentile
    :timer-count
    :timer-mean-rate
    :timer1-minute-rate
    :timer5-minute-rate
    :timer15-minute-rate
    :histogram-min
    :histogram-max
    :histogram-mean
    :histogram-std-dev
    :histogram-median
    :histogram75th-percentile
    :histogram95th-percentile
    :histogram98th-percentile
    :histogram99th-percentile
    :histogram999th-percentile
    :meter-count
    :meter-mean-rate
    :meter1-minute-rate
    :meter5-minute-rate
    :meter15-minute-rate
    :counter-count
    :gauge-value})


(defn- metrics-attribute-filter* [f]
  (let [f (comp boolean f)]
    (proxy [MetricAttributeFilter] []
      (recordTimerMin [name, metric] (f name :timer-min))
      (recordTimerMax [name, metric] (f name :timer-max))
      (recordTimerMean [name, metric] (f name :timer-mean))
      (recordTimerStdDev [name, metric] (f name :timer-std-dev))
      (recordTimerMedian [name, metric] (f name :timer-median))
      (recordTimer75thPercentile [name, metric] (f name :timer75th-percentile))
      (recordTimer95thPercentile [name, metric] (f name :timer95th-percentile))
      (recordTimer98thPercentile [name, metric] (f name :timer98th-percentile))
      (recordTimer99thPercentile [name, metric] (f name :timer99th-percentile))
      (recordTimer999thPercentile [name, metric] (f name :timer999th-percentile))
      (recordTimerCount [name, metric] (f name :timer-count))
      (recordTimerMeanRate [name, metric] (f name :timer-mean-rate))
      (recordTimer1MinuteRate [name, metric] (f name :timer1-minute-rate))
      (recordTimer5MinuteRate [name, metric] (f name :timer5-minute-rate))
      (recordTimer15MinuteRate [name, metric] (f name :timer15-minute-rate))
      (recordHistogramMin [name, metric] (f name :histogram-min))
      (recordHistogramMax [name, metric] (f name :histogram-max))
      (recordHistogramMean [name, metric] (f name :histogram-mean))
      (recordHistogramStdDev [name, metric] (f name :histogram-std-dev))
      (recordHistogramMedian [name, metric] (f name :histogram-median))
      (recordHistogram75thPercentile [name, metric] (f name :histogram75th-percentile))
      (recordHistogram95thPercentile [name, metric] (f name :histogram95th-percentile))
      (recordHistogram98thPercentile [name, metric] (f name :histogram98th-percentile))
      (recordHistogram99thPercentile [name, metric] (f name :histogram99th-percentile))
      (recordHistogram999thPercentile [name, metric] (f name :histogram999th-percentile))
      (recordMeterCount [name, metric] (f name :meter-count))
      (recordMeterMeanRate [name, metric] (f name :meter-mean-rate))
      (recordMeter1MinuteRate [name, metric] (f name :meter1-minute-rate))
      (recordMeter5MinuteRate [name, metric] (f name :meter5-minute-rate))
      (recordMeter15MinuteRate [name, metric] (f name :meter15-minute-rate))
      (recordCounterCount [name, metric] (f name :counter-count))
      (recordGaugeValue [name, metric] (f name :gauge-value)))))


(defn ^NewRelicReporter newrelic-reporter
  ([^MetricRegistry reg {:keys [reporter-name reporting-frequency-seconds metrics-attribute-filter
                                prefix rate-unit duration-unit]
                         :or  {reporting-frequency-seconds 30,
                               reporter-name "trackit-reporter"
                               metrics-attribute-filter (constantly true)
                               prefix "trackit/",
                               rate-unit TimeUnit/SECONDS,
                               duration-unit TimeUnit/MILLISECONDS} :as cfg}]
   (let [b (NewRelicReporter/forRegistry reg)]
     (.name b reporter-name)
     (when-let [^String p (-> (str prefix "/") (str/replace #"\." "/") (str/replace #"//+" "/"))]
       (.metricNamePrefix b p))
     (when metrics-attribute-filter
       (.attributeFilter b (metrics-attribute-filter* metrics-attribute-filter)))
     (when rate-unit
       (.rateUnit b rate-unit))
     (when duration-unit
       (.durationUnit b duration-unit))
     (.build b))))

(defn start
  "Report all metrics to NewRelic periodically."
  [^NewRelicReporter r ^long seconds]
  (mrep/start r seconds))

(defn stop
  "Stops reporting."
  [^NewRelicReporter r]
  (mrep/stop r))



(defn start-reporting
  [registry
   {:keys [reporter-name reporting-frequency-seconds metrics-attribute-filter
           prefix rate-unit duration-unit]
    :or  {reporting-frequency-seconds 30, reporter-name "trackit-reporter"
          metrics-attribute-filter (constantly true)
          prefix "trackit/", rate-unit TimeUnit/SECONDS, duration-unit TimeUnit/MILLISECONDS
          } :as cfg}]
  (let [reporter (newrelic-reporter registry cfg)]
    (start reporter reporting-frequency-seconds)
    (fn [] (stop reporter))))
