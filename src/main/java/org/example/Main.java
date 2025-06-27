package org.example;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.formats.json.JsonDeserializationSchema;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import java.time.Duration;

public class Main {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Create Kafka source for orders topic
        KafkaSource<Order> source = KafkaSource.<Order>builder()
                .setBootstrapServers("kafka:9093")
                .setTopics("orders")
                .setGroupId("flink-group")
                .setDeserializer(KafkaRecordDeserializationSchema.valueOnly(
                        new JsonDeserializationSchema<>(Order.class)))
                .setProperty("request.timeout.ms", "60000")
                .setProperty("session.timeout.ms", "30000")
                .setProperty("retries", "3")
                .build();

        DataStreamSource<Order> stream = env.fromSource(
                source,
                WatermarkStrategy.noWatermarks(),
                "Kafka JSON Source"
        );

        // Print all incoming orders for debugging
        stream.print("Incoming Orders");

        // Convert Order to CountryTotal and aggregate
        var aggregatedStream = stream
                .map(order -> new CountryTotal(order.country, order.amount))
                .keyBy(countryTotal -> countryTotal.country)
                .window(TumblingProcessingTimeWindows.of(Duration.ofMinutes(1)))
                .reduce(new ReduceFunction<CountryTotal>() {
                    @Override
                    public CountryTotal reduce(CountryTotal ct1, CountryTotal ct2) {
                        return new CountryTotal(ct1.country, ct1.totalAmount + ct2.totalAmount);
                    }
                });

        aggregatedStream.print("Country Totals (1-min windows)");


        // Create Kafka sink for processed-orders topic (for String messages)
        KafkaSink<String> sink = KafkaSink.<String>builder()
                .setBootstrapServers("kafka:9093")
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic("processed-orders")
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .build())
                .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                .build();

        // Convert CountryTotal to JSON string and send to Kafka
        aggregatedStream
                .map(countryTotal -> String.format(
                        "{\"country\":\"%s\",\"total_amount\":%.2f,\"window_end\":\"%s\"}",
                        countryTotal.country,
                        countryTotal.totalAmount,
                        java.time.Instant.now().toString()
                ))
                .sinkTo(sink);

        env.execute("Flink Kafka Country Aggregation with Sink");
    }
}