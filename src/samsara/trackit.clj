(ns samsara.trackit
  (:require [metrics.core :refer [new-registry]]
            [metrics.counters :as mc]
            [metrics.gauges :as mg]
            [metrics.meters :as mm]
            [metrics.histograms :as mh]
            [metrics.timers :as mt]
            [metrics.reporters.console :as console])
  (:require [metrics.reporters.graphite :as graphite])
  (:import  [java.util.concurrent TimeUnit])
  (:import  [com.codahale.metrics MetricFilter])
  (:require [clojure.pprint :refer [print-table] :as pp]))

(def ^:dynamic *registry* (new-registry))

(def ^:dynamic *base-name* ["app" "metrics"])

(defn set-base-metrics-name! [name1 name2]
  (alter-var-root #'*base-name* (constantly [name1 name2])))

(defn- namer [name]
  (if-not (string? name)
    name
    (let [sections  (clojure.string/split name #"\.")
          nsections (count sections)]
      (cond
       (= nsections 3) sections
       (> nsections 3) [(first sections) (second sections)
                        (clojure.string/join "." (nnext sections))]
       (= nsections 1) (conj *base-name* name)
       (= nsections 2) (concat [(first *base-name*)] sections)))))


;;
;; # Counting things
;;

(defn make-count-tracker
  "It returns a function which traces the number of times it is called
  or the number of items seen.
  A counter is a monotonically increasing number. All counters are
  updated atomically.

  Usage example:


     ;; create the counter
     (def track-orders (make-count-tracker \"orders.processed\"))

     ;; use the counter
     (defn mark-order-as-processed [& args]
       (track-orders) ;; increment and report the counter
       (comment do something else))


  When called without arguments it atomically increments
  the counter by 1. If you call the function with a number
  will increment the counter by that number.
  Eample:

     ;; create the counter
     (def track-order-items (make-count-tracker \"items.processed\"))

     ;; use the counter
     (defn mark-order-as-processed [{items :items :as order}]
       (track-order-items (count items)) ;; increment and report the counter
       (comment do something else))
  "
  [name]
  (let [metric (mc/counter *registry* (namer name))]
    (fn ([]  (mc/inc! metric))
       ([v] (mc/inc! metric v)))))



(defmacro track-count
  "It counts the number of time the body is executed.
  A counter is a monotonically increasing number. All counters are
  updated atomically.

  Usage example:

     ;; use the counter
     (defn mark-order-as-processed [& args]
       (track-count \"orders.processed\"    ;; count body executions
         (comment do something else)))

  "
  ([name & body]
   `(try
      ~@body
      (finally
        (mc/inc! (mc/counter *registry* (namer ~name)))))))



;;
;; # Tracking current value of something
;;

(defn track-value-of
  "It tracks the current value of a function.
  This is useful to measure stats at regular intervals,
  such as the number of active users, the connection pool size,
  the number of pending requests etc.

     ;; initialize tracker
     (track-value-of \"db.connection.pool.size\"
       (fn [] (get-current-pool-size db-pool)))

  Once initialized it will be reported at regular intervals,
  typically every 10s (reporter configuration) which means
  that every ~10s a call to the `tracker-fn` will be made.
  "
  [name tracker-fn]
  (mg/gauge-fn *registry* (namer name) tracker-fn))



(defmacro track-value
  "It tracks the current value of a function.
  This is useful to measure stats at regular intervals,
  such as the number of active users, the connection pool size,
  the number of pending requests etc.
  It is the same as `track-value-of`, it's just a convenience
  macro which wraps the body into a function.

     ;; initialize tracker
     (track-value \"db.connection.pool.size\"
       (get-current-pool-size db-pool))

  Once initialized it will be reported at regular intervals,
  typically every 10s (reporter configuration) which means
  that every ~10s a call to the `tracker-fn` will be made.
  "
  [name & body]
  `(track-value-of ~name (fn [] ~@body)))


;;
;; # Tracking how often something happens (rate and distribution)
;;

(defn make-rate-tracker
  "It returns a function which tracks how often an event happens.
  It is useful to track things such as: number of request per second,
  number of db-query per second, number of orders per minute etc.

  usage:

      ;; initialize tracker
      (def track-request-rate (make-rate-tracker \"user.requests\"))

      ;; in your request handler
      (defn request-handler [req]
        (track-request-rate)
        (comment handle the request))
  "
  [name]
  (let [metric (mm/meter *registry* (namer name))]
    (fn []  (mm/mark! metric))))


(defmacro track-rate
  "It tracks the rate of the body execution.
  It is useful to track things such as: number of request per second,
  number of db-query per second, number of orders per minute etc.

  usage:

      ;; in your request handler
      (defn request-handler [req]
        (track-rate \"user.requests\"
          (comment handle the request)))
  "
  [name & body]
  `(try
     ~@body
     (finally
       (mm/mark! (mm/meter *registry* (namer ~name))))))



;;
;; # Tracking a distribution (histograms/percentiles)
;;

(defn make-distribution-tracker
  "It returns a function which takes a value as parameter
  and it tracks its distribution.
  Whenever you are looking for an average, an histogram
  gives you more information.
  So rather than looking at:

  **The average search result is 120 items**

  with a distribution you get something like:

  **75% of all searches returned 100 or fewer results,  while 95% got 200 or fewer.**

  usage:

     ;; initialize tracker
     (def track-search-results (make-distribution-tracker \"search.results\"))

     ;; track searches
     (defn my-search [query]
       (let [results (execute query)
             _       (track-search-results (count results)]
         results)))

  "
  [name]
  (let [metric (mh/histogram *registry* (namer name))]
    (fn [v] (mh/update! metric v))))


(defmacro track-distribution
  "It tracks the distribution of a metric. It expect that the body
  returns either a number or a countable collection.
  Whenever you are looking for an average, an histogram
  gives you more information.
  So rather than looking at:

  **The average search result is 120 items**

  with a distribution you get something like:

  **75% of all searches returned 100 or fewer results,  while 95% got 200 or fewer.**

  usage:

     ;; track searches
     (defn my-search [query]
       (track-distribution \"search.results\"
         (execute query)))

  It returns the result of the `body` execution.
  "
  [name & body]
  `(let [result# (do ~@body)]
     (mh/update! (mh/histogram *registry* (namer ~name))
                 (if (number? result#) result# (count result#)))
     ;; return result
     result#))

;;
;; # Tracking how long it takes to do something
;;

(defmacro track-time
  "It track the distribution of the time it takes to
  execute the body and the rate at which it is processed.

  This is useful to measure things such as: the time it
  takes to query the database, or to process a request,
  or to send a request to another services.
  Anytime you what to know about *how long* it takes
  to run a part of your code use this tracker.

     (track-time \"db.search\"
       (let [connection (get-connection db)]
         (db-query connection a-query)))

  It returns the result of `body` execution.
  "
  [name & body]
  `(mt/time! (mt/timer *registry* (namer ~name))
             ~@body))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; helper functions

(defprotocol MetricsValue
  (current-value-of [metric]
    "Returns the current value of a metric as a map specification of the value")
  (str-current-value-of [metric]
    "Returns a string represantation of the current value good for display in a table"))


(extend-protocol MetricsValue
  com.codahale.metrics.Counter
  (current-value-of     [metric] {:count (mc/value metric)})
  (str-current-value-of [metric] (str (mc/value metric) )))


(extend-protocol MetricsValue
  com.codahale.metrics.Gauge
  (current-value-of     [metric] {:count (mg/value metric)})
  (str-current-value-of [metric] (str (mg/value metric) )))


(extend-protocol MetricsValue
  com.codahale.metrics.Meter
  (current-value-of     [metric] (mm/rates metric))
  (str-current-value-of [metric] (str (mm/rate-one metric)
                                      " evs/s (n: " (mm/count metric) ")")))


(extend-protocol MetricsValue
  com.codahale.metrics.Histogram
  (current-value-of     [metric] (mh/percentiles metric))
  (str-current-value-of [metric] (str (get (mh/percentiles metric) 0.99 0)
                                      " (99%/n:" (mh/number-recorded metric) ")")))


(extend-protocol MetricsValue
  com.codahale.metrics.Timer
  (current-value-of     [metric] (mt/percentiles metric))
  (str-current-value-of [metric] (str (quot (get (mt/percentiles metric) 0.99 0) 1000000)
                                      "ms (99%/n:" (mt/number-recorded metric) ")")))


(defn all-metrics []
  (->> (into {} (.getMetrics *registry*))
       (map (juxt first (comp current-value-of second) (comp str-current-value-of second)))
       (map (partial zipmap [:metric :value :display]))))


(defn show-stats []
  (print-table [:metric :display] (all-metrics)))

(defmulti start-reporting :type)

(defmethod start-reporting :default [cfg]
  (println "TRACKit!: no reporting method selected."))


(defmethod start-reporting :console
  [{:keys [seconds] :or {seconds 300}}]
  (console/start (console/reporter *registry* {}) seconds))


(defmethod start-reporting :graphite
  [{:keys [seconds host port prefix rate-unit duration-unit]
    :or  {seconds 10, host "localhost", port 2003, prefix "trackit"
          rate-unit TimeUnit/SECONDS, duration-unit TimeUnit/MILLISECONDS}}]

  (console/start
   (graphite/reporter *registry*
                      {:host host
                       :port port
                       :prefix prefix
                       :rate-unit rate-unit
                       :duration-unit duration-unit})
   seconds))
