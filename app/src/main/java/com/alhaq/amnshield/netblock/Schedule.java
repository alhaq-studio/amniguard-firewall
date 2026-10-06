package com.alhaq.amnshield.netblock;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class Schedule implements Serializable {
    public long id;
    public String name;
    public int startHour;
    public int startMinute;
    public int endHour;
    public int endMinute;
    public Set<Integer> days; // 1=Sunday, 2=Monday, ... 7=Saturday
    public Set<Integer> apps; // UIDs of apps to block
    public boolean enabled;

    public Schedule() {
        this.id = System.currentTimeMillis();
        this.days = new HashSet<>();
        this.apps = new HashSet<>();
        this.enabled = true;
    }

    public boolean isActiveNow() {
        if (!enabled) return false;

        java.util.Calendar cal = java.util.Calendar.getInstance();
        int currentDay = cal.get(java.util.Calendar.DAY_OF_WEEK);
        int currentHour = cal.get(java.util.Calendar.HOUR_OF_DAY);
        int currentMinute = cal.get(java.util.Calendar.MINUTE);

        if (!days.contains(currentDay)) return false;

        int currentTime = currentHour * 60 + currentMinute;
        int startTime = startHour * 60 + startMinute;
        int endTime = endHour * 60 + endMinute;

        if (startTime <= endTime) {
            return currentTime >= startTime && currentTime <= endTime;
        } else {
            // Crosses midnight
            return currentTime >= startTime || currentTime <= endTime;
        }
    }

    public String getTimeString() {
        return String.format("%02d:%02d - %02d:%02d", startHour, startMinute, endHour, endMinute);
    }

    public String getDaysString() {
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 7; i++) {
            if (days.contains(i)) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(dayNames[i - 1]);
            }
        }
        return sb.toString();
    }
}
