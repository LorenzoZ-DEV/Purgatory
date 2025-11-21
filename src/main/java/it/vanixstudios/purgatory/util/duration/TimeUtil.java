package it.vanixstudios.purgatory.util.duration;

import java.util.concurrent.TimeUnit;

public class TimeUtil {

    public static long parseTime(String input) {
        if (input == null || input.length() < 2) throw new IllegalArgumentException("Invalid duration");
        long time;
        try {
            time = Long.parseLong(input.substring(0, input.length() - 1));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid number", ex);
        }
        char unit = input.charAt(input.length() - 1);
        switch (unit) {
            case 's': return TimeUnit.SECONDS.toMillis(time);
            case 'm': return TimeUnit.MINUTES.toMillis(time);
            case 'h': return TimeUnit.HOURS.toMillis(time);
            case 'd': return TimeUnit.DAYS.toMillis(time);
            default: throw new IllegalArgumentException("Unknown unit: " + unit);
        }
    }

    public static String formatDuration(long millis) {
        long seconds = millis / 1000;
        if (seconds < 60) return seconds + "s";
        long minutes = seconds / 60;
        if (minutes < 60) return minutes + "m";
        long hours = minutes / 60;
        if (hours < 24) return hours + "h";
        return (hours / 24) + "d";
    }
}
