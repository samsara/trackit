(ns samsara.trackit.util
  (:require [metrics.counters :as mc]
            [metrics.gauges :as mg]
            [metrics.meters :as mm]
            [metrics.histograms :as mh]
            [metrics.timers :as mt]))


(defprotocol MetricsValue
  (metric-type      [metric]
    "Returns the type of the metric")
  (current-value-of [metric]
    "Returns the current value of a metric as a map specification of the value")
  (display-value-of [metric]
    "Returns a string representation of the current value good for display in a table")
  (display-short-value-of [metric]
    "Returns a short string representation of the current value good for display in a table"))


(extend-protocol MetricsValue
  com.codahale.metrics.Counter
  (metric-type      [metric] :counter)
  (current-value-of [metric] {:count (mc/value metric)})
  (display-value-of [metric] (str (mc/value metric)))
  (display-short-value-of [metric] (display-value-of metric)))


(extend-protocol MetricsValue
  com.codahale.metrics.Gauge
  (metric-type      [metric] :gauge)
  (current-value-of [metric] {:value (mg/value metric)})
  (display-value-of [metric] (str (mg/value metric)))
  (display-short-value-of [metric] (display-value-of metric)))


(extend-protocol MetricsValue
  com.codahale.metrics.Meter
  (metric-type      [metric] :rate)
  (current-value-of [metric]
    (-> {}
        (assoc :m1    (mm/rate-one metric))
        (assoc :m5    (mm/rate-five metric))
        (assoc :m15   (mm/rate-fifteen metric))
        (assoc :total (mm/count metric))
        (assoc :mean  (mm/rate-mean metric))))

  (display-value-of [metric]
    (let [{:keys [mean m1 total]} (current-value-of metric)]
      (format "mean: %.2f/s, 1m rate: %.2f/s, count: %d"
              mean m1 total)))

  (display-short-value-of [metric]
    (let [{mean :mean} (current-value-of metric)]
      (format "%.2f/s" mean))))



(extend-protocol MetricsValue
  com.codahale.metrics.Histogram
  (metric-type      [metric] :distribution)

  (current-value-of [metric]
    (-> (mh/percentiles metric)
        (assoc :min     (mh/smallest metric))
        (assoc :max     (mh/largest metric))
        (assoc :mean    (mh/mean metric))
        (assoc :std-dev (mh/std-dev metric))
        (assoc :total   (mh/number-recorded metric))))

  (display-value-of [metric]
    (let [{mean :mean p99 0.99 total :total min :min max :max} (current-value-of metric)]
      (format "mean: %.2f, 99%%ile: %.2f, count: %d, min: %.2f, max: %.2f"
              (double mean) (double p99) total (double min) (double max))))

  (display-short-value-of [metric]
    (let [{mean :mean} (current-value-of metric)]
      (format "%.2f" mean))))



(extend-protocol MetricsValue
  com.codahale.metrics.Timer
  (metric-type      [metric] :timer)

  (current-value-of [metric]
    (-> (mt/percentiles metric)
        (assoc :min       (mt/smallest metric))
        (assoc :max       (mt/largest metric))
        (assoc :mean      (mt/mean metric))
        (assoc :std-dev   (mt/std-dev metric))
        (assoc :total     (mt/number-recorded metric))
        (assoc :m1-rate   (mt/rate-one metric))
        (assoc :m5-rate   (mt/rate-five metric))
        (assoc :m15-rate  (mt/rate-fifteen metric))
        (assoc :mean-rate (mt/rate-mean metric))))

  (display-value-of [metric]
    (let [{:keys [mean total min max mean-rate m1-rate] p99 0.99} (current-value-of metric)
          millis #(double (/ % 1000000))]
      (format (str "mean: %.2f/ms, 99%%ile: %.2f/ms, count: %d, min: %.2f/ms, max: %.2f/ms, "
                   "@mean rate: %.2f/s, 1m rate: %.2f/s")
              (millis mean) (millis p99) total (millis min) (millis max)
              (double mean-rate) (double m1-rate))))

  (display-short-value-of [metric]
    (let [{mean :mean} (current-value-of metric)
          millis #(double (/ % 1000000))]
      (format "%.2fms" (millis mean)))))


(defn as-metric [name metric]
  (when metric
    (->> metric
         ((juxt (constantly name)
                metric-type
                current-value-of
                display-short-value-of
                display-value-of))
         (zipmap [:metric :type :value :short :display]))))


(defn metric-value [name metric]
  (:value (as-metric name metric)))
