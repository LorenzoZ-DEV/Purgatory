package it.vanixstudios.purgatory.manager.bans;

public class BanInfo {
    private final String reason;
    private final long banTimestamp;
    private final long duration;

    public BanInfo(String reason, long banTimestamp, long duration) {
        this.reason = reason;
        this.banTimestamp = banTimestamp;
        this.duration = duration;
    }

    public String getReason() {
        return reason;
    }

    public long getBanTimestamp() {
        return banTimestamp;
    }

    public long getDuration() {
        return duration;
    }

    public boolean isPermanent() {
        return duration == 0;
    }
}
