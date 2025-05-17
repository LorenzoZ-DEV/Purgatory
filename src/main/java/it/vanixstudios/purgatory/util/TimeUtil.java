package it.vanixstudios.purgatory.util;

import java.util.concurrent.TimeUnit;

public class TimeUtil {

    public static long parseTime(String input) {
        try {
            long time = Long.parseLong(input.substring(0, input.length() - 1));
            char unit = input.charAt(input.length() - 1);

            switch (unit) {
                case 's': return TimeUnit.SECONDS.toMillis(time);
                case 'm': return TimeUnit.MINUTES.toMillis(time);
                case 'h': return TimeUnit.HOURS.toMillis(time);
                case 'd': return TimeUnit.DAYS.toMillis(time);
                default: return -1;
            }
        } catch (Exception e) {
            return -1;
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
