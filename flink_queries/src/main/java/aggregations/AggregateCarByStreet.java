package aggregations;

import java.time.LocalDateTime;

public class AggregateCarByStreet {

    private String streetId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long latency;
    private long throughput;
    private int parallelism;

    public int getParallelism() { return parallelism; }

    public void setParallelism(int parallelism) { this.parallelism = parallelism; }

    public String getStreetId() {
        return streetId;
    }

    public void setStreetId(String streetId) {
        this.streetId = streetId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public long getLatency() {
        return latency;
    }

    public void setLatency(long latency) {
        this.latency = latency;
    }

    public long getThroughput() {
        return throughput;
    }

    public void setThroughput(long throughput) {
        this.throughput = throughput;
    }

    @Override
    public String toString() {
        return parallelism + "\tcars1\t" + startTime + "\t" + endTime + "\t" + latency;
    }
}
