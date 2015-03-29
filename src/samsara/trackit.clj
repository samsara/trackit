(ns samsara.trackit
  (:require [samsara.trackit.util :refer :all]
            [samsara.trackit.reporter :as rep])
  (:require [metrics.core :refer [new-registry]]
            [metrics.counters :as mc]
            [metrics.gauges :as mg]
            [metrics.meters :as mm]
            [metrics.histograms :as mh]
            [metrics.timers :as mt])
  (:require [clojure.pprint :refer [print-table] :as pp]))

(def ^:dynamic *registry* (new-registry))

(def ^:dynamic *base-name* ["app" "metrics"])

(defn set-base-metrics-name! [name1 name2]
  (alter-var-root #'*base-name* (constantly [name1 name2])))

(defn reset-registry! []
  (alter-var-root #'*registry* (constantly (new-registry))))

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

  If the step is bigger than 1 then you can specify a size:

      ;; in your request handler
      (defn request-handler [req]
        (track-request-size-rate (get-size req))
        (comment handle the request))
  "
  [name]
  (let [metric (mm/meter *registry* (namer name))]
    (fn
      ([]  (mm/mark! metric))
      ([n] (mm/mark! metric n)))))



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



(defn all-metrics
  "Returns a list of all metrics with their current value and a formatted value"
  ([] (all-metrics *registry*))
  ([registry]
   (->> (into {} (.getMetrics registry))
        (map (juxt first
                   (comp metric-type second)
                   (comp current-value-of second)
                   (comp display-short-value-of second)
                   (comp display-value-of second)))
        (map (partial zipmap [:metric :type :value :short :display]))
        (sort-by first))))



(defn show-stats
  "Prints the current list of metrics with their values.
  Optionally you can pass a parameter `format` which
  changes the level of information displayed for every metric.
  `format` can be: `:short` or `:full`, `:short` is the default.

     (show-stats)       ;; prints list of stats with short value
     (show-stats :full) ;; prints list of stats with detailed value
  "
  ([] (show-stats :short))
  ([format]
   (if (= :full format)
     (print-table [:metric :type :display] (all-metrics))
     (print-table [:metric :type :short]   (all-metrics)))))


(defn start-reporting!
  "Initialize reporting to selected backend"
  ([cfg]
   (start-reporting! *registry* cfg))
  ([registry cfg]
   (rep/start-reporting registry cfg)))
