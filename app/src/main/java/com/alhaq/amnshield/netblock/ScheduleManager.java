package com.alhaq.amnshield.netblock;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ScheduleManager {
    private static final String TAG = "ScheduleManager";
    private static final String PREF_SCHEDULES = "schedules";

    public static List<Schedule> getSchedules(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String json = prefs.getString(PREF_SCHEDULES, null);
        
        if (json == null) {
            return new ArrayList<>();
        }

        try {
            JSONArray array = new JSONArray(json);
            List<Schedule> schedules = new ArrayList<>();
            
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Schedule schedule = new Schedule();
                schedule.id = obj.getLong("id");
                schedule.name = obj.getString("name");
                schedule.startHour = obj.getInt("startHour");
                schedule.startMinute = obj.getInt("startMinute");
                schedule.endHour = obj.getInt("endHour");
                schedule.endMinute = obj.getInt("endMinute");
                schedule.enabled = obj.getBoolean("enabled");
                
                JSONArray daysArray = obj.getJSONArray("days");
                schedule.days = new HashSet<>();
                for (int j = 0; j < daysArray.length(); j++) {
                    schedule.days.add(daysArray.getInt(j));
                }
                
                JSONArray appsArray = obj.getJSONArray("apps");
                schedule.apps = new HashSet<>();
                for (int j = 0; j < appsArray.length(); j++) {
                    schedule.apps.add(appsArray.getInt(j));
                }
                
                schedules.add(schedule);
            }
            
            return schedules;
        } catch (JSONException e) {
            Log.e(TAG, "Error loading schedules", e);
            return new ArrayList<>();
        }
    }

    public static void saveSchedules(Context context, List<Schedule> schedules) {
        try {
            JSONArray array = new JSONArray();
            
            for (Schedule schedule : schedules) {
                JSONObject obj = new JSONObject();
                obj.put("id", schedule.id);
                obj.put("name", schedule.name);
                obj.put("startHour", schedule.startHour);
                obj.put("startMinute", schedule.startMinute);
                obj.put("endHour", schedule.endHour);
                obj.put("endMinute", schedule.endMinute);
                obj.put("enabled", schedule.enabled);
                
                JSONArray daysArray = new JSONArray();
                for (Integer day : schedule.days) {
                    daysArray.put(day);
                }
                obj.put("days", daysArray);
                
                JSONArray appsArray = new JSONArray();
                for (Integer uid : schedule.apps) {
                    appsArray.put(uid);
                }
                obj.put("apps", appsArray);
                
                array.put(obj);
            }
            
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            prefs.edit().putString(PREF_SCHEDULES, array.toString()).apply();
        } catch (JSONException e) {
            Log.e(TAG, "Error saving schedules", e);
        }
    }

    public static void addSchedule(Context context, Schedule schedule) {
        List<Schedule> schedules = getSchedules(context);
        schedules.add(schedule);
        saveSchedules(context, schedules);
    }

    public static void updateSchedule(Context context, Schedule schedule) {
        List<Schedule> schedules = getSchedules(context);
        for (int i = 0; i < schedules.size(); i++) {
            if (schedules.get(i).id == schedule.id) {
                schedules.set(i, schedule);
                break;
            }
        }
        saveSchedules(context, schedules);
    }

    public static void deleteSchedule(Context context, long scheduleId) {
        List<Schedule> schedules = getSchedules(context);
        for (int i = 0; i < schedules.size(); i++) {
            if (schedules.get(i).id == scheduleId) {
                schedules.remove(i);
                break;
            }
        }
        saveSchedules(context, schedules);
    }

    public static List<Schedule> getActiveSchedules(Context context) {
        List<Schedule> all = getSchedules(context);
        List<Schedule> active = new ArrayList<>();
        for (Schedule schedule : all) {
            if (schedule.isActiveNow()) {
                active.add(schedule);
            }
        }
        return active;
    }

    public static boolean isAppBlocked(Context context, int uid) {
        List<Schedule> active = getActiveSchedules(context);
        for (Schedule schedule : active) {
            if (schedule.apps.contains(uid)) {
                return true;
            }
        }
        return false;
    }
}
