# Troubleshooting.

## JDK9+ module problem.

``` text
19-05-28 16:22:23 ERROR [com.codahale.metrics.ScheduledReporter:176] - Exception thrown from ConsoleReporter#report. Exception was suppressed.
                                                    java.lang.Thread.run                       Thread.java:  835
                      java.util.concurrent.ThreadPoolExecutor$Worker.run           ThreadPoolExecutor.java:  628
                       java.util.concurrent.ThreadPoolExecutor.runWorker           ThreadPoolExecutor.java: 1128
java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run  ScheduledThreadPoolExecutor.java:  305
                             java.util.concurrent.FutureTask.runAndReset                   FutureTask.java:  305

     java.util.concurrent.Executors$RunnableAdapter.call                    Executors.java:  515
                            com.codahale.metrics.ScheduledReporter$1.run            ScheduledReporter.java:  174
                           com.codahale.metrics.ScheduledReporter.report            ScheduledReporter.java:  251
                             com.codahale.metrics.ConsoleReporter.report              ConsoleReporter.java:  232
                         com.codahale.metrics.ConsoleReporter.printGauge              ConsoleReporter.java:  290
                                com.codahale.metrics.RatioGauge.getValue                   RatioGauge.java:   11
                                com.codahale.metrics.RatioGauge.getValue                   RatioGauge.java:   64
              com.codahale.metrics.jvm.FileDescriptorRatioGauge.getRatio     FileDescriptorRatioGauge.java:   35

        com.codahale.metrics.jvm.FileDescriptorRatioGauge.invoke     FileDescriptorRatioGauge.java:   48
                                                                     ...
java.lang.reflect.InaccessibleObjectException: Unable to make public long com.sun.management.internal.OperatingSystemImpl.getOpenFileDescriptorCount() accessible: module jdk.management does not "opens com.sun.management.internal" to unnamed module @1f1b120f
```

This is caused by the JVM instrumentation thread which tries to read
the number of open file descriptors. This doesn't work in JDK9+
because of the module separation.

To work around this issue please just remove the sampling of open
files from your reported configuration:


```clojure
(start-reporting!
   {:type        :console
    ;; publish jvm metrics groups listed below (NOTE: doesn't contains :files)
    :jvm-metrics [:memory :gc :threads :attributes]
    })
```
