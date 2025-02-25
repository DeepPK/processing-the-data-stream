package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.apache.kafka.common.serialization.StringDeserializer;

public class Flink {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers("localhost:9092")
                .setTopics("TraficData")
                .setGroupId("FlinkProcessing")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setDeserializer(KafkaRecordDeserializationSchema.valueOnly(StringDeserializer.class))
                .build();

        DataStream<String> dataSource = env.fromSource(
                kafkaSource,
                WatermarkStrategy.noWatermarks(),
                "TraficData"
        );
        DataStream<Trafic> Data = dataSource.map(json -> {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, Trafic.class);
        });

        DataStream<Trafic> ProcessedData = Data
                .keyBy(Trafic::getsensor_id)
                .window(TumblingProcessingTimeWindows.of(Time.seconds(10)))
                .process(new ProcessWindowFunction<Trafic, Trafic, Integer, TimeWindow>() {
                    @Override
                    public void process(
                            Integer sensor_id,
                            Context context,
                            Iterable<Trafic> trafics,
                            Collector<Trafic> out
                    ) {
                        double sum1 = 0.0;
                        int count1 = 0;
                        double sum2 = 0.0;
                        int count2 = 0;
                        for (Trafic t : trafics) {
                            sum1 += t.getcount1();
                            count1++;
                            sum2 += t.getcount2();
                            count2++;
                        }
                        double avg1 = sum1 / count1;
                        double avg2 = sum2 / count2;
                        for (Trafic t : trafics) {
                            System.out.print(t.getcount1() + " " + avg1 + "\n");
                            System.out.print(t.getcount2() + " " + avg2 + "\n");
                            t.setnew_green1((int) Math.ceil(avg1*5));
                            t.setnew_green2((int) Math.ceil(avg2*5));
                            out.collect(t);
                        }
                    }
                });

        KafkaSink<String> kafkaSink = KafkaSink.<String>builder()
                .setBootstrapServers("localhost:9092")
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic("ProcessedData")
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .build()
                )
                .setDeliverGuarantee(DeliveryGuarantee.NONE)
                .build();

        DataStream<String> outputStream = ProcessedData.map(Trafic -> {
                    ObjectMapper objectMapper = new ObjectMapper();
                    return objectMapper.writeValueAsString(Trafic);
                });
        outputStream.sinkTo(kafkaSink);
        env.execute("Kafka Source Flink Example");
    }
}
