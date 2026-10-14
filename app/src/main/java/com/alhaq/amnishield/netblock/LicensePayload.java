package com.alhaq.amnishield.netblock;

/*
    This file is part of AmniGuard Firewall.

    AmniGuard Firewall is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    AmniGuard Firewall is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with AmniGuard Firewall.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2015-2026 by Alhaq Initiative
*/

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class LicensePayload implements Serializable {
    private final String email;
    private final String type;
    private final long expires;
    private final String userId;
    private final int version;

    public LicensePayload(String email, String type, long expires, String userId, int version) {
        this.email = email != null ? email : "";
        this.type = type != null ? type : "pro";
        this.expires = expires;
        this.userId = userId;
        this.version = version;
    }

    public static LicensePayload fromJson(String jsonStr) {
        try {
            JSONObject obj = new JSONObject(jsonStr);
            String email = obj.optString("email", "");
            String type = obj.optString("type", "pro");
            long expires = obj.optLong("expires", 0L);
            String userId = obj.isNull("user_id") ? null : obj.optString("user_id", null);
            int version = obj.optInt("version", 1);
            return new LicensePayload(email, type, expires, userId, version);
        } catch (JSONException e) {
            return null;
        }
    }

    public String toJson() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("email", email);
            obj.put("type", type);
            obj.put("expires", expires);
            if (userId != null) {
                obj.put("user_id", userId);
            }
            obj.put("version", version);
            return obj.toString();
        } catch (JSONException e) {
            return "{}";
        }
    }

    public String getEmail() {
        return email;
    }

    public String getType() {
        return type;
    }

    public long getExpires() {
        return expires;
    }

    public String getUserId() {
        return userId;
    }

    public int getVersion() {
        return version;
    }

    public boolean isExpired() {
        return expires > 0 && expires < System.currentTimeMillis();
    }

    public boolean isLifetime() {
        return expires >= 4000000000000L || "lifetime".equalsIgnoreCase(type);
    }
}
