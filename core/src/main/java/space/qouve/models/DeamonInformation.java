package space.qouve.models;

public class DeamonInformation {
    public DeamonStatus status;

    // Only if UnHealthy
    public String error;

    public enum DeamonStatus {
        Healthy,
        UnHealthy
    }
}