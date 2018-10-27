(ns samsara.trackit.reporter-cloudwatch
  (:import  [java.util.concurrent TimeUnit]
            [com.amazonaws.regions Regions]
            [com.amazonaws.services.cloudwatch AmazonCloudWatchAsyncClientBuilder]
            [io.github.azagniotov.metrics.reporter.cloudwatch CloudWatchReporter]))

(defn start-reporting
  [registry
   {:keys [namespace async-client reporting-frequency-seconds rate-unit duration-unit]
    :or   {async-client                (.build (AmazonCloudWatchAsyncClientBuilder/standard))
           reporting-frequency-seconds 300
           rate-unit                   TimeUnit/SECONDS
           duration-unit               TimeUnit/MILLISECONDS}
    :as   cfg}]
  (let [reporter         (-> (CloudWatchReporter/forRegistry registry async-client namespace)
                             (.convertRatesTo rate-unit)
                             (.convertDurationsTo duration-unit)
                             (.build))]
    (.start reporter reporting-frequency-seconds TimeUnit/SECONDS)
    (fn [] (.stop reporter))))
