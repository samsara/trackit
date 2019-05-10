package samsara;


import io.prometheus.client.exporter.PushGateway;
import io.prometheus.client.Collector;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.SortedMap;
import java.io.IOException;

public final class PrometheusReporter extends ScheduledReporter {
    private static final Logger logger = LoggerFactory.getLogger(PrometheusReporter.class);

    private PushGateway pushGateway = null;
    private Collector collector = null;
    private String jobName = null;
    private Map<String,String> groupingKeys = null;

    public PrometheusReporter(MetricRegistry registry,
                               String name,
                               MetricFilter filter,
                               TimeUnit rateUnit,
                               TimeUnit durationUnit,
                               PushGateway pushGateway,
                               Collector collector,
                               Map<String,String> groupingKeys) {
        super(registry, name, filter, rateUnit, durationUnit);
        this.pushGateway = pushGateway;
        this.collector = collector;
        this.jobName = name;
        this.groupingKeys = groupingKeys;

        logger.info("Initialized PrometheusReporter for registry with name '{}', filter of type '{}', rate unit '{}' , duration unit '{}'",
                    name, filter.getClass().getCanonicalName(), rateUnit.toString(), durationUnit.toString());
    }


    @Override
    public void report(SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms, SortedMap<String, Meter> meters, SortedMap<String, Timer> timers) {
        logger.debug("Received report of {} gauges, {} counters, {} histograms, {} meters and {} timers",
                     gauges.size(), counters.size(), histograms.size(), meters.size(), timers.size());
        try {
            pushGateway.push(collector, jobName, groupingKeys);
        } catch(IOException e) {
            logger.error("Failed to push metrics to prometheus", e);
        }
    }
}
