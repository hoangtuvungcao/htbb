package javax.microedition.media;

public interface PlayerListener {
    String STARTED = "started";
    String STOPPED = "stopped";
    String END_OF_MEDIA = "endOfMedia";
    String CLOSED = "closed";
    String ERROR = "error";
    String VOLUME_CHANGED = "volumeChanged";
    String DURATION_UPDATED = "durationUpdated";
    String DEVICE_UNAVAILABLE = "deviceUnavailable";
    String DEVICE_AVAILABLE = "deviceAvailable";
    String STOPPED_AT_TIME = "stoppedAtTime";

    void playerUpdate(Player player, String event, Object eventData);
}
