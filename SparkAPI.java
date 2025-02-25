package org.example;

import com.google.gson.Gson;
import spark.Spark;

import java.util.HashMap;
import java.util.Map;

public class SparkAPI {
    private static boolean isFlinkJobRunning = false;
    public static void main(String[] args) {
        Spark.port(8080);

        Spark.post("/start", (req, res) -> {
            if (!isFlinkJobRunning) {
                new Thread(() -> {
                    try {
                        Flink.main(new String[]{});
                        isFlinkJobRunning = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
                return "Flink job started\n";
            } else {
                return "Flink job is already running\n";
            }
        });

        Spark.post("/stop", (req, res) -> {
            if (isFlinkJobRunning) {
                isFlinkJobRunning = false;
                return "Flink job stopped";
            } else {
                return "Flink job is not running";
            }
        });

        Spark.get("/status", (req, res) -> {
            Map<String, String> response = new HashMap<>();
            response.put("status", isFlinkJobRunning ? "running" : "stopped");
            res.type("application/json");
            return new Gson().toJson(response);
        });
    }
}