# TRACKit!

A Clojure developer friendly wrapper for Yammer's Metrics library.
The objective of this library is to try to make as simple as possible
to track metrics inside your app.

It can publish the metrics in any of the following systems:

  - Console
  - Ganglia
  - Graphite
  - Statsd
  - Infuxdb
  - Reimann
  - NewRelic

## How to build

To build all the packages at once run:

``` bash
./bin/lein-all.sh do clean, check, install
```

## Usage

To use TRACKit! you need to add the following dependency to your
`project.clj` file.

```
[samsara/trackit-all "0.7.1"]
```

Latest version: [![Clojars Project](https://img.shields.io/clojars/v/samsara/trackit-all.svg)](https://clojars.org/samsara/trackit-all)


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
 (def track-orders (count-tracker "myapp.orders.processed"))

 ;; use the counter
 (defn mark-order-as-processed [& args]
   (track-orders) ;; increment and report the counter
   (comment do something else))

```

When called without arguments it atomically increments the counter by 1. If you call the function with a number will increment the counter by that number.

Example:

```clojure
 ;; create the counter
 (def track-order-items (count-tracker "myapp.items.processed"))

 ;; use the counter
 (defn mark-order-as-processed [{items :items :as order}]
   (track-order-items (count items)) ;; increment and report the counter
   (comment do something else))
```

A convenience fucntion is also available which counts the number of time the body is executed. A counter is a monotonically increasing number. All counters are updated atomically.

```clojure
;; use the counter
(defn mark-order-as-processed [& args]
  (track-count "myapp.orders.processed")
  (comment do something else))

;; use the counter with a give increment
(defn process-order [{items :items :as order}]
  (track-count "myapp.orders.items.count" (count items))
  (comment do something else))

```

If you are only interested in counting how many times a piece of code
is executed then you can use the following macro.

```Clojure
;; use the counter
(defn mark-order-as-processed [& args]
  (track-pass-count "myapp.orders.processed"    ;; count body executions
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
(def track-request-rate (rate-tracker "myapp.user.requests"))

;; in your request handler
(defn request-handler [req]
  (track-request-rate)
  (comment handle the request))
```

If you are handling a batch of item rather than a single request you
can pass the a number in the returned function like:

```clojure
(def track-documents-rate (rate-tracker "myapp.indexer.documents.indexed"))

(defn store-documents [ documents-batch ]
  (track-documents-rate (count documents-batch))
  (comment then do store the batch of documents in db))
```

You can inline your tracker in with the `track-rate` function.

```clojure
;; in your request handler
(defn request-handler [req]
  (track-rate "myapp.user.requests")
  (comment handle the request))
```

Here with an arbitrary size.

```clojure
;; track the number of doc indexed
(defn index-documents [documents]
  (track-rate "myapp.document.indexed" (count documents))
  (comment handle the request))
```

With the macro you can do the same over the execution of a block of
code.  It tracks the rate of the body execution. It is useful to track
things such as: number of request per second, number of db-query per
second, number of orders per minute etc.

usage:

```clojure
  ;; in your request handler
  (defn request-handler [req]
    (track-pass-rate "myapp.user.requests"
      (comment handle the request)))
```

### Tracking a distribution (histograms/percentiles)

If you want to know the average of some quantity then the distribution
tracker provides a better result.  `distribution-tracker` returns a
function which takes a value as parameter and it tracks its
distribution.  Whenever you are looking for an average, an histogram
gives you more information. So rather than looking at:

*The average search result is 120 items*

with a distribution you get something like:

*75% of all searches returned 100 or fewer results, while 95% got 200 or fewer.*

usage:

```clojure
 ;; initialize tracker
 (def track-search-results (distribution-tracker "myapp.search.results"))

 ;; track searches
 (defn my-search [query]
   (let [results (execute query)
         _       (track-search-results (count results)]
     results)))
```

Again you can achieve the same with the convenience function.
If you have a code block which return a collection or a number
you can track the distribution as:

```clojure
;; track searches
(defn my-search [query]
  (let [results (execute query)]
    (track-distribution "myapp.search.results" (count results))
    results))
```

Typically the thing you want to track is going to be either
a straight number or something countable.
So rather than having to wrap the the result into a let,
you can pass the "thing" you want to track as the `value`
parameter. If it is a number it will use its value,
if values is a `seq`, a collection or anything you can `count`
on it, it will run `(count value)` or an exception will be raised.
Finally the `value` will be returned as result of the function.
The following code is equivalent to the previous one,
but much clearer.

```clojure
;; track searches
(defn my-search [query]
  (track-distribution "myapp.search.results"
    (execute query)))
```

It returns the result of the `body` execution.


### Tracking how long it takes to do something

If you want to track how long a query takes then a macro similar to `time` will provide what you are looking for.
`track-time` tracks the distribution of the time it takes to execute the body and the rate at which it is processed.

This is useful to measure things such as: the time it takes to query the database, or to process a request, or to send a request to another services. Anytime you what to know about how long it takes to run a part of your code use this tracker.

```clojure
 (track-time "myapp.db.search"
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

### JVM Instrumentation

TRACKit! is able to publish metrics about the running JVM.
This can be controlled via the `:jvm-metrics` which can be
either `:all` or you can provide a list of JVM metrics you are
interested in. (**by default `:all` metrics are instrumented**)

Example:


```clojure
(start-reporting!
   {:type        :console
    ;; publish jvm metrics as well
    :jvm-metrics :all
    })
```

Alternatively you can specify which set of metrics you are
interested in:

```clojure
(start-reporting!
   {:type        :console
    ;; publish jvm metrics groups listed below
    :jvm-metrics [:memory :files :gc :threads :attributes]
    })
```

For more reporting information look at the next section.

If you wish to disable the JVM metrics instrumentation,
then set `:jvm-metrics` to `:none`.

```clojure
(start-reporting!
   {:type        :console
    ;; to disable metrics instrumentation
    :jvm-metrics :none
    })
```

### Start reporting

TRACKit! supports several reporting methods.

Reporting is activated with:

```clojure
(def r (start-reporting! cfg))
```

The configuration will contain a element `:type` which will define
which backend system will be used to report the metrics.
Each different backend has a different set of configuration options.
See here the details.

To stop the reporter:

```clojure
;; stop the reporter
(r)
```

#### Console

```clojure
(import 'java.util.concurrent.TimeUnit)

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

Add the following dependency to your `project.clj`

``` clojure
;; use same version as trackit-core
[samsara/trackit-graphite "0.7.1"]
```

And then start your reporting with:

```clojure
(import 'java.util.concurrent.TimeUnit)

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

Add the following dependency to your `project.clj`

``` clojure
;; use same version as trackit-core
[samsara/trackit-statsd "0.7.1"]
```

And then start your reporting with:

```clojure
(import 'java.util.concurrent.TimeUnit)

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

Add the following dependency to your `project.clj`

``` clojure
;; use same version as trackit-core
[samsara/trackit-riemann "0.7.1"]
```

And then start your reporting with:

```clojure
(import 'java.util.concurrent.TimeUnit)

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
    :prefix                      "trackit"
    ;; local hostname
    :host-name                   "node1"})
```

#### Ganglia

Add the following dependency to your `project.clj`

``` clojure
;; use same version as trackit-core
[samsara/trackit-ganglia "0.7.1"]
```

And then start your reporting with:


```clojure
(import 'java.util.concurrent.TimeUnit)

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

#### InfluxDB

To report to InfluxDB use the `:influxdb` reporter.

Add the following dependency to your `project.clj`

``` clojure
;; use same version as trackit-core
[samsara/trackit-influxdb "0.7.1"]
```

And then start your reporting with:

```clojure
(import 'java.util.concurrent.TimeUnit)

(start-reporting!
   {:type                        :influxdb
    ;; how often the stats will be reported to the server
    :reporting-frequency-seconds 10
    ;; influxdb host and port
    :host                        "localhost"
    :port                        8086
    ;; unit to use to display rates
    :rate-unit                   TimeUnit/SECONDS
    ;; unit to use to display durations
    :duration-unit               TimeUnit/MILLISECONDS
    ;; prefix to add to all metrics
    :prefix                      "trackit"
    ;; influx specific params
    :db-name "metrics"         ;; must already exist
    :auth "username:password"  ;; if required
    ;; additional (optional) tags
    :tags {"host" "node1", "version" "1.2.3"}
    })
```

#### Grafana / InfluxDB via Riemann

To report to Grafana and InfluxDB use Riemann as collector.

Add the following dependency to your `project.clj`

``` clojure
;; use same version as trackit-core
[samsara/trackit-riemann "0.7.1"]
```

And then start your reporting with:

```clojure
(import 'java.util.concurrent.TimeUnit)

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

In your Grafana / InfluxDB server start a Riemann server with
the following configuration to forward your metrics to InfluxDB

``` clojure

(logging/init  {:file "/var/log/riemann.log"})

; Listen on the local interface over TCP (5555), UDP (5555), and websockets
; (5556)
(let  [host "0.0.0.0"]
  (tcp-server  {:host host})
  (udp-server  {:host host})
  (ws-server  {:host host}))

; Expire old events from the index every 5 seconds.
(periodically-expire 5)

(let  [index  (index)
       influx (influxdb {:host "127.0.0.1" :port 8086 :db "dbname"
                         :username "admin" :password "admin"
                         :series #(:service %)
                         :version :0.9
                         })]

  ; Inbound events will be passed to these streams:
  (streams
    ;We are not interested in events from riemann's servers
    ;i.e the tcp-server udp-server and ws-server above
    (where (not  (service #"^riemann .+"))

      (default :ttl 60
        ; Index
        index

        ;for now, log em
        #(info %)

        ;send to influxdb
        influx

        ;Log expired events.
        (expired
          (fn [event] (info "EXPIRED" event)))))))

```

#### NewRelic

Add the following dependency to your `project.clj`

``` clojure
;; use same version as trackit-core
[samsara/trackit-newrelic "0.7.1"]
```

And then start your reporting with:


```clojure
(import 'java.util.concurrent.TimeUnit)

(start-reporting!
   {:type                        :newrelic
    ;; how often the stats will be reported to the server
    :reporting-frequency-seconds 30
    ;; set the reported name
    :reporter-name               "trackit-reporter"
    ;; Whether a specific metric attribute should be published to NewRelic or not.
    ;; It takes a function which takes in input the `name` of the metric and the
    ;; attribute (keyword) and it returns `true`/`false` whether it should be pulished.
    ;; by default it publishes everything.
    :metrics-attribute-filter    (constantly true)
    ;; unit to use to display rates
    :rate-unit                   TimeUnit/SECONDS
    ;; unit to use to display durations
    :duration-unit               TimeUnit/MILLISECONDS
    ;; prefix to add to all metrics (slash separated)
    :prefix                      "trackit/"})
```

**NOTE:** that to use this reporter you need to download and run the NewRelic java agent
as described in the [NewRelic documentaion](https://docs.newrelic.com/docs/agents/java-agent/installation/java-agent-manual-installation).

[NewRelic custom metrics best practices](https://docs.newrelic.com/docs/agents/manage-apm-agents/agent-data/custom-metrics#best_practices) recommends
to keep the number of custom metrics under 2000.  For this purpose you
can use the `:metrics-attribute-filter` option which takes a function
with two arguments: the metrics `name` and the attribute type. The
attribute type is one of the following keywords:

``` clojure
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
```

So if you are interested only in the `999th-percentiles` for timers
and histograms you can write a function which looks like this:

``` clojure
(defn my-filter [name type]
  (#{:timer999th-percentile
     :histogram999th-percentile
     :meter-count
     :meter1-minute-rate
     :counter-count
     :gauge-value} type))
```

This function will return `true` only when the type is one of the listed types.
Finally you have to pass this function as `:metrics-attribute-filter my-filter`.


### Selectively import reporters.

Reporters and their dependencies are distributed into separate JAR files.
Here a breakdown of the different packages.

  * `[samsara/trackit-core     "x.y.z"]` - core api, always required
  * `[samsara/trackit-ganglia  "x.y.z"]` - required only when reporting to Ganglia
  * `[samsara/trackit-graphite "x.y.z"]` - required only when reporting to Graphite
  * `[samsara/trackit-influxdb "x.y.z"]` - required only when reporting to InfluxDB
  * `[samsara/trackit-newrelic "x.y.z"]` - required only when reporting to NewRelic
  * `[samsara/trackit-riemann  "x.y.z"]` - required only when reporting to Riemann
  * `[samsara/trackit-statsd   "x.y.z"]` - required only when reporting to Statsd
  * `[samsara/trackit-all      "x.y.z"]` - use this one if you want bind them all in single dependency.

Latest version: [![Clojars Project](https://img.shields.io/clojars/v/samsara/trackit-all.svg)](https://clojars.org/samsara/trackit-all)


## License

Copyright © 2015-2016 Samsara's authors.

Distributed under the Apache License v 2.0 (http://www.apache.org/licenses/LICENSE-2.0)
