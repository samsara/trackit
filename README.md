# TRACKit!

A Clojure wrapper for Yammer's Metrics library.
The objective of this library is to try to make as simple as possible
to track metrics inside your app.

## Usage

To use TRACKit! you need to add the following dependency to your
`project.clj` file.

```
[samsara/trackit "0.1.0"]
```

Load the namespace in the REPL

```clojure
(use 'samsara.trackit)
```
or as part of your namespace

```clojure
(ns my.project
  (:require [samsara.trackit :refer :all])
```

Now you can start count, track rates, track values etc.

### Counting things

The function `count-tracker` is used to produce counters.
A counter is a monotonically increasing (or decreasing) number. All counters are updated atomically.
It returns a function which traces the number of times it is called or the number of items seen. 

Usage example:

```clojure
 ;; create the counter
 (def track-orders (count-tracker "orders.processed"))

 ;; use the counter
 (defn mark-order-as-processed [& args]
   (track-orders) ;; increment and report the counter
   (comment do something else))

```

When called without arguments it atomically increments the counter by 1. If you call the function with a number will increment the counter by that number.

Eample:

```clojure
 ;; create the counter
 (def track-order-items (count-tracker "items.processed"))

 ;; use the counter
 (defn mark-order-as-processed [{items :items :as order}]
   (track-order-items (count items)) ;; increment and report the counter
   (comment do something else))
```

A convenience marco is also available which counts the number of time the body is executed. A counter is a monotonically increasing number. All counters are updated atomically.

```clojure
 ;; use the counter
 (defn mark-order-as-processed [& args]
   (track-count "orders.processed"    ;; count body executions
     (comment do something else)))
```

### Tracking current value of something

To track a value which can vary over time you can use the `track-value-of` function.
It tracks the current value of a function. This is useful to measure stats at regular intervals, such as the number of active users, the connection pool size, the number of pending requests etc.

```clojure
 ;; initialize tracker
 (track-value-of "db.connection.pool.size"
   (fn [] (get-current-pool-size db-pool)))
```

Once initialized it will be reported at regular intervals, typically every 10s (reporter configuration) which means that every ~10s a call to the tracker-fn will be made in a separate thread.

If you have a function which returns a value which you want to track,
then you can use the marco which shortens the code.
It tracks the current value of a function. This is useful to measure stats at regular intervals, such as the number of active users, the connection pool size, the number of pending requests etc. It is the same as track-value-of, it's just a convenience macro which wraps the body into a function.

```clojure
 ;; initialize tracker
 (track-value "db.connection.pool.size"
   (get-current-pool-size db-pool))
```

## Tracking how often something happens (rate)

If you have to track the frequency of something, you can use the `rate-tracker`.
It returns a function which tracks how often an event happens. It is useful to track things such as: number of request per second, number of db-query per second, number of orders per minute etc.

usage:

```clojure
  ;; initialize tracker
  (def track-request-rate (rate-tracker "user.requests"))

  ;; in your request handler
  (defn request-handler [req]
    (track-request-rate)
    (comment handle the request))
```

If you are handling a batch of item rather than a single request you
can pass the a number in the returned function like:

```clojure
(def track-documents-rate (rate-tracker "indexer.documents.indexed"))

(defn store-documents [ documents-batch ]
  (track-documents-rate (count documents-batch)
  (comment then do store the batch of documents in db))
```

With the macro you can do the same over the execution of a block of code.
It tracks the rate of the body execution. It is useful to track things such as: number of request per second, number of db-query per second, number of orders per minute etc.

usage:

```clojure
  ;; in your request handler
  (defn request-handler [req]
    (track-rate "user.requests"
      (comment handle the request)))
```

### Tracking a distribution (histograms/percentiles)

If you want to know the average of some quantity then the distribution tracker provides
a better result.
`distribution-tracker` returns a function which takes a value as parameter and it tracks its distribution.
Whenever you are looking for an average, an histogram gives you more information. So rather than looking at:

*The average search result is 120 items*

with a distribution you get something like:

*75% of all searches returned 100 or fewer results, while 95% got 200 or fewer.*

usage:

```clojure
 ;; initialize tracker
 (def track-search-results (distribution-tracker "search.results"))

 ;; track searches
 (defn my-search [query]
   (let [results (execute query)
         _       (track-search-results (count results)]
     results)))
```

Again you can achieve the same with the convenience macro.
If you have a code block which return a collection or a number
you can track the distribution as:

```clojure
 ;; track searches
 (defn my-search [query]
   (track-distribution "search.results"
     (execute query)))
```

The macro will take the result of the code block, and
if it is a number will update the distribution with the result,
if the value is a collection then will count the number of elements.
Finally, it returns the result of the body execution.

### Tracking how long it takes to do something

If you want to track how long a query takes then a macro similar to `time` will provide what you are looking for.
`track-time` tracks the distribution of the time it takes to execute the body and the rate at which it is processed.

This is useful to measure things such as: the time it takes to query the database, or to process a request, or to send a request to another services. Anytime you what to know about how long it takes to run a part of your code use this tracker.

```clojure
 (track-time "db.search"
   (let [connection (get-connection db)]
     (db-query connection a-query)))
```
It returns the result of body execution.

### Read the stats

To obtain the current value of all the metrics you are collecting
you can use `all-metrics` which will return a list with all metrics.

With `show-stats` you'll be able to print all the metrics in the std-output
in a tabular format. By default it displays a short version. For a more
complete output use:

```clojure

;; get the list of all metrics
(def metrics (all-mentrics))

;; display stats wil only main metrics values
(show-stats)

;; display stats with more details
(show-stats :full)
```

### Start reporting

TRACKit! supports several reporting methods.

Reporting is activated with:

```clojure
(start-reporting! cfg)
```

The configuration will contain a element `:type` which will define
which backend system will be used to report the metrics.
Each different backend has a different set of configuration options.
See here the details.

#### Console

```clojure
(start-reporting!
   {:type                        :console 
    ;; how often the stats will be displayed
    :reporting-frequency-seconds 300
    ;; which output stream should be used stdout or stderr
    :stream                      (System/err)
    ;; unit to use to display rates
    :rate-unit                   TimeUnit/SECONDS
    ;; unit to use to display durations
    :duration-unit               TimeUnit/MILLISECONDS })
```

#### Graphite

```clojure
(start-reporting!
   {:type                        :graphite
    ;; how often the stats will be reported to the server
    :reporting-frequency-seconds 10
    ;; graphite host and port
    :host                        "localhost"
    :port                        2003
    ;; unit to use to display rates
    :rate-unit                   TimeUnit/SECONDS
    ;; unit to use to display durations
    :duration-unit               TimeUnit/MILLISECONDS
    ;; prefix to add to all metrics
    :prefix                      "trackit"})
```

#### Statsd

```clojure
(start-reporting!
   {:type                        :statsd
    ;; how often the stats will be reported to the server
    :reporting-frequency-seconds 10
    ;; statsd host and port
    :host                        "localhost"
    :port                        8125
    ;; unit to use to display rates
    :rate-unit                   TimeUnit/SECONDS
    ;; unit to use to display durations
    :duration-unit               TimeUnit/MILLISECONDS
    ;; prefix to add to all metrics
    :prefix                      "trackit"})
```

#### Riemann

```clojure
(start-reporting!
   {:type                        :riemann
    ;; how often the stats will be reported to the server
    :reporting-frequency-seconds 10
    ;; riemann host and port
    :host                        "localhost"
    :port                        5555
    ;; unit to use to display rates
    :rate-unit                   TimeUnit/SECONDS
    ;; unit to use to display durations
    :duration-unit               TimeUnit/MILLISECONDS
    ;; prefix to add to all metrics
    :prefix                      "trackit"})
```

#### Ganglia

```clojure
(start-reporting!
   {:type                        :ganglia
    ;; how often the stats will be reported to the server
    :reporting-frequency-seconds 60
    ;; ganglia host and port
    :host                        "localhost"
    :port                        8649
    ;; unit to use to display rates
    :rate-unit                   TimeUnit/SECONDS
    ;; unit to use to display durations
    :duration-unit               TimeUnit/MILLISECONDS
    ;; prefix to add to all metrics
    :prefix                      "trackit"})
```

## License

Copyright © 2015 Samsara's authors.

Distributed under the Apache License v 2.0 (http://www.apache.org/licenses/LICENSE-2.0)

