(ns samsara.trackit.reporter-cloudwatch
  (:import  [java.util.concurrent TimeUnit]
            [com.amazonaws.regions Regions]
            [com.amazonaws.services.cloudwatch AmazonCloudWatchAsyncClientBuilder]
            [io.github.azagniotov.metrics.reporter.cloudwatch CloudWatchReporter]))

(defn apply-dimensions [builder dimensions]
  (let [dimension-strs (mapv #(str (first %) "=" (second %)) dimensions)]
    (.withGlobalDimensions builder (into-array dimension-strs))
    builder))

(defn start-reporting
  [registry
   {:keys [namespace async-client reporting-frequency-seconds rate-unit duration-unit
           global-dimensions]
    :or   {async-client                (.build (AmazonCloudWatchAsyncClientBuilder/standard))
           reporting-frequency-seconds 300
           rate-unit                   TimeUnit/SECONDS
           duration-unit               TimeUnit/MILLISECONDS}
    :as   cfg}]
  (let [reporter         (-> (CloudWatchReporter/forRegistry registry async-client namespace)
                             (.convertRatesTo rate-unit)
                             (.convertDurationsTo duration-unit)
                             (#(if global-dimensions (apply-dimensions % global-dimensions) %))
                             (.build))]
    (.start reporter reporting-frequency-seconds TimeUnit/SECONDS)
    (fn [] (.stop reporter))))
