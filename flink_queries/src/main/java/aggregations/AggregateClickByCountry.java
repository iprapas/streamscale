package aggregations;

import java.time.LocalDateTime;

public class AggregateClickByCountry {

    private Integer count;
    private String country;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long latency;
    private long throughput;
    private int parallelism;

    public int getParallelism() {
        return parallelism;
    }

    public void setParallelism(int parallelism) {
        this.parallelism = parallelism;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getCountry() {
        return country;
    }

    public long getLatency() {
        return latency;
    }

    public void setLatency(long latency) {
        this.latency = latency;
    }

    public void setCountry(String country) {
        this.country = country;
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

    public long getThroughput() {
        return throughput;
    }

    public void setThroughput(long throughput) {
        this.throughput = throughput;
    }

    @Override
    public String toString() {
        return parallelism + "\tclicks2\t" + startTime + "\t" + endTime + "\t" + latency;
    }
}
