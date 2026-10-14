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

    Copyright 2015-2025 by Marcel Bokhorst (M66B)
*/

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;

public class PendingIntentCompat {
    private PendingIntentCompat() {
    }

    private static int applyImmutableFlag(int flags) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if ((flags & (PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_IMMUTABLE)) == 0) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }
        }
        return flags;
    }

    public static PendingIntent getActivity(Context context, int requestCode, Intent intent, int flags) {
        return PendingIntent.getActivity(context, requestCode, intent, applyImmutableFlag(flags));
    }

    public static PendingIntent getService(Context context, int requestCode, @NonNull Intent intent, int flags) {
        return PendingIntent.getService(context, requestCode, intent, applyImmutableFlag(flags));
    }

    public static PendingIntent getForegroundService(Context context, int requestCode, @NonNull Intent intent, int flags) {
        int finalFlags = applyImmutableFlag(flags);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return PendingIntent.getForegroundService(context, requestCode, intent, finalFlags);
        } else {
            return PendingIntent.getService(context, requestCode, intent, finalFlags);
        }
    }

    public static PendingIntent getBroadcast(Context context, int requestCode, Intent intent, int flags) {
        return PendingIntent.getBroadcast(context, requestCode, intent, applyImmutableFlag(flags));
    }
}
