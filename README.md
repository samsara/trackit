# TRACKit!

A Clojure library designed to ... well, that part is up to you.

## Usage

### Counting things

The function `make-count-tracker` is used to produce counters.
A counter is a monotonically increasing (or decreasing) number. All counters are updated atomically.
It returns a function which traces the number of times it is called or the number of items seen. 

Usage example:

```clojure
 ;; create the counter
 (def track-orders (make-count-tracker "orders.processed"))

 ;; use the counter
 (defn mark-order-as-processed [& args]
   (track-orders) ;; increment and report the counter
   (comment do something else))

```

When called without arguments it atomically increments the counter by 1. If you call the function with a number will increment the counter by that number. Eample:

```clojure
 ;; create the counter
 (def track-order-items (make-count-tracker "items.processed"))

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


## License

Copyright © 2015 Samsara's authors.

Distributed under the Apache License v 2.0 (http://www.apache.org/licenses/LICENSE-2.0)

