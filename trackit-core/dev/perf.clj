(ns perf
  (:require [samsara.trackit :refer :all]
            [criterium.core :refer [bench quick-bench]]))


(comment

  ;; Clojure 1.9.0
  ;; Java 1.8.0_161


  (quick-bench
   (namer "foo.bar.baz")) ;; 154.651771 ns

  (quick-bench
   (namer "foo.bar.baz.foo.far.faz.boo.bar.baz")) ;; 293.999674 ns


  (quick-bench
   (re-find #"^([^.]+)\.([^.]+)\.(.*)$" "foo.bar.baz")) ;; 358.249296 ns

  (quick-bench
   (re-find #"^([^.]+)\.([^.]+)\.(.*)$" "foo.bar.baz.foo.far.faz.boo.bar.baz")) ;; 631.019174 ns

  (bench
   (track-count "foo.bar.baz.foo.far.faz.boo.bar.baz.count" 1)) ;; 230.410884 ns

  (bench
   (track-time "foo.bar.baz.foo.far.faz.boo.bar.baz.time" 1))   ;; 660.773663 ns

  (bench
   (track-rate "foo.bar.baz.foo.far.faz.boo.bar.baz.rate" 1))   ;; 290.237882 ns

  (bench
   (track-distribution "foo.bar.baz.foo.far.faz.boo.bar.baz.hist" 1)) ;; 392.112105 ns


  (let [tracker (count-tracker "foo.bar.baz.foo.far.faz.boo.bar.baz.count")]
    (bench
     (tracker 1))) ;; 10.059072 ns

  )
