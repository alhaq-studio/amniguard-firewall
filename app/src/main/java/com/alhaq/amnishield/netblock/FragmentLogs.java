package com.alhaq.amnishield.netblock;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.DialogInterface;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.chip.Chip;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FragmentLogs extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "AmniGuard.FragmentLogs";

    private ListView lvLog;
    private AdapterLog adapter;
    private SwitchCompat swLive;
    private TextView tvDisabled;
    private View vwPopupAnchor;

    private Chip chipAll;
    private Chip chipBlocked;
    private Chip chipAllowed;
    private Chip chipUdp;
    private Chip chipTcp;
    private Chip chipOther;

    private boolean resolve;
    private boolean organization;
    private InetAddress vpn4 = null;
    private InetAddress vpn6 = null;

    private DatabaseHelper.LogChangedListener listener = new DatabaseHelper.LogChangedListener() {
        @Override
        public void onChanged() {
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateAdapter();
                    }
                });
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_logs, container, false);

        lvLog = view.findViewById(R.id.lvLog);
        swLive = view.findViewById(R.id.swLive);
        tvDisabled = view.findViewById(R.id.tvDisabled);
        vwPopupAnchor = view.findViewById(R.id.vwPopupAnchor);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        resolve = prefs.getBoolean("resolve", false);
        organization = prefs.getBoolean("organization", false);
        boolean log = prefs.getBoolean("log", true);

        tvDisabled.setVisibility(log ? View.GONE : View.VISIBLE);
        swLive.setChecked(log);

        swLive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("log", isChecked).apply();
                tvDisabled.setVisibility(isChecked ? View.GONE : View.VISIBLE);
            }
        });

        try {
            vpn4 = InetAddress.getByName(prefs.getString("vpn4", "10.1.10.1"));
            vpn6 = InetAddress.getByName(prefs.getString("vpn6", "fd00:1:fd00:1:fd00:1:fd00:1"));
        } catch (UnknownHostException ex) {
            Log.e(TAG, ex.toString());
        }

        ImageButton btnLogFilter = view.findViewById(R.id.btnLogFilter);
        ImageButton btnExportLogs = view.findViewById(R.id.btnExportLogs);
        ImageButton btnRetention = view.findViewById(R.id.btnRetention);
        ImageButton btnClearLogs = view.findViewById(R.id.btnClearLogs);

        btnLogFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogFilterDialog();
            }
        });

        btnExportLogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showExportDialog();
            }
        });

        btnRetention.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRetentionDialog();
            }
        });

        btnClearLogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showClearLogsDialog();
            }
        });

        // Initialize Filter Chips
        chipAll = view.findViewById(R.id.chipAll);
        chipBlocked = view.findViewById(R.id.chipBlocked);
        chipAllowed = view.findViewById(R.id.chipAllowed);
        chipUdp = view.findViewById(R.id.chipUdp);
        chipTcp = view.findViewById(R.id.chipTcp);
        chipOther = view.findViewById(R.id.chipOther);

        chipAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefs.edit()
                        .putBoolean("proto_udp", true)
                        .putBoolean("proto_tcp", true)
                        .putBoolean("proto_other", true)
                        .putBoolean("traffic_allowed", true)
                        .putBoolean("traffic_blocked", true)
                        .apply();
                updateFilterChips();
                updateAdapter();
            }
        });

        chipBlocked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = chipBlocked.isChecked();
                prefs.edit()
                        .putBoolean("traffic_blocked", true)
                        .putBoolean("traffic_allowed", !checked)
                        .apply();
                updateFilterChips();
                updateAdapter();
            }
        });

        chipAllowed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = chipAllowed.isChecked();
                prefs.edit()
                        .putBoolean("traffic_allowed", true)
                        .putBoolean("traffic_blocked", !checked)
                        .apply();
                updateFilterChips();
                updateAdapter();
            }
        });

        chipUdp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = chipUdp.isChecked();
                prefs.edit()
                        .putBoolean("proto_udp", true)
                        .putBoolean("proto_tcp", !checked)
                        .putBoolean("proto_other", !checked)
                        .apply();
                updateFilterChips();
                updateAdapter();
            }
        });

        chipTcp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = chipTcp.isChecked();
                prefs.edit()
                        .putBoolean("proto_tcp", true)
                        .putBoolean("proto_udp", !checked)
                        .putBoolean("proto_other", !checked)
                        .apply();
                updateFilterChips();
                updateAdapter();
            }
        });

        chipOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = chipOther.isChecked();
                prefs.edit()
                        .putBoolean("proto_other", true)
                        .putBoolean("proto_udp", !checked)
                        .putBoolean("proto_tcp", !checked)
                        .apply();
                updateFilterChips();
                updateAdapter();
            }
        });

        updateFilterChips();
        updateAdapter();

        lvLog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (adapter == null) return;
                Cursor cursor = (Cursor) adapter.getItem(position);
                if (cursor == null) return;
                showLogDetailsDialog(cursor);
            }
        });

        return view;
    }

    private void updateFilterChips() {
        if (getContext() == null || chipAll == null) return;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        boolean udp = prefs.getBoolean("proto_udp", true);
        boolean tcp = prefs.getBoolean("proto_tcp", true);
        boolean other = prefs.getBoolean("proto_other", true);
        boolean allowed = prefs.getBoolean("traffic_allowed", true);
        boolean blocked = prefs.getBoolean("traffic_blocked", true);

        boolean isAll = udp && tcp && other && allowed && blocked;
        chipAll.setChecked(isAll);
        chipBlocked.setChecked(blocked && !allowed);
        chipAllowed.setChecked(allowed && !blocked);
        chipUdp.setChecked(udp && !tcp && !other);
        chipTcp.setChecked(tcp && !udp && !other);
        chipOther.setChecked(other && !udp && !tcp);
    }

    private void showLogFilterDialog() {
        if (getContext() == null) return;
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_log_filter, null);

        final com.google.android.material.checkbox.MaterialCheckBox cbTrafficAllowed = dialogView.findViewById(R.id.cbTrafficAllowed);
        final com.google.android.material.checkbox.MaterialCheckBox cbTrafficBlocked = dialogView.findViewById(R.id.cbTrafficBlocked);
        final com.google.android.material.checkbox.MaterialCheckBox cbProtoUdp = dialogView.findViewById(R.id.cbProtoUdp);
        final com.google.android.material.checkbox.MaterialCheckBox cbProtoTcp = dialogView.findViewById(R.id.cbProtoTcp);
        final com.google.android.material.checkbox.MaterialCheckBox cbProtoOther = dialogView.findViewById(R.id.cbProtoOther);
        final com.google.android.material.checkbox.MaterialCheckBox cbResolve = dialogView.findViewById(R.id.cbResolve);
        final com.google.android.material.checkbox.MaterialCheckBox cbOrganization = dialogView.findViewById(R.id.cbOrganization);
        final com.google.android.material.checkbox.MaterialCheckBox cbPcap = dialogView.findViewById(R.id.cbPcap);

        cbTrafficAllowed.setChecked(prefs.getBoolean("traffic_allowed", true));
        cbTrafficBlocked.setChecked(prefs.getBoolean("traffic_blocked", true));
        cbProtoUdp.setChecked(prefs.getBoolean("proto_udp", true));
        cbProtoTcp.setChecked(prefs.getBoolean("proto_tcp", true));
        cbProtoOther.setChecked(prefs.getBoolean("proto_other", true));
        cbResolve.setChecked(prefs.getBoolean("resolve", false));
        cbOrganization.setChecked(prefs.getBoolean("organization", false));
        cbPcap.setChecked(prefs.getBoolean("pcap", false));

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setPositiveButton(R.string.btn_apply_filters, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean allowed = cbTrafficAllowed.isChecked();
                        boolean blocked = cbTrafficBlocked.isChecked();
                        if (!allowed && !blocked) {
                            allowed = true;
                            blocked = true;
                        }
                        boolean udp = cbProtoUdp.isChecked();
                        boolean tcp = cbProtoTcp.isChecked();
                        boolean other = cbProtoOther.isChecked();
                        if (!udp && !tcp && !other) {
                            udp = true;
                            tcp = true;
                            other = true;
                        }

                        prefs.edit()
                                .putBoolean("traffic_allowed", allowed)
                                .putBoolean("traffic_blocked", blocked)
                                .putBoolean("proto_udp", udp)
                                .putBoolean("proto_tcp", tcp)
                                .putBoolean("proto_other", other)
                                .putBoolean("resolve", cbResolve.isChecked())
                                .putBoolean("organization", cbOrganization.isChecked())
                                .putBoolean("pcap", cbPcap.isChecked())
                                .apply();

                        updateFilterChips();
                        updateAdapter();
                    }
                })
                .setNeutralButton(R.string.btn_reset_filters, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        prefs.edit()
                                .putBoolean("traffic_allowed", true)
                                .putBoolean("traffic_blocked", true)
                                .putBoolean("proto_udp", true)
                                .putBoolean("proto_tcp", true)
                                .putBoolean("proto_other", true)
                                .putBoolean("resolve", false)
                                .putBoolean("organization", false)
                                .putBoolean("pcap", false)
                                .apply();

                        updateFilterChips();
                        updateAdapter();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showClearLogsDialog() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.menu_clear_logs)
                .setMessage(R.string.msg_confirm_clear_logs)
                .setIcon(R.drawable.ic_delete_24dp)
                .setPositiveButton(R.string.title_delete, new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... voids) {
                                DatabaseHelper.getInstance(requireContext()).clearLog(-1);
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                if (getContext() != null) {
                                    Toast.makeText(requireContext(), R.string.msg_logs_cleared, Toast.LENGTH_SHORT).show();
                                    updateAdapter();
                                }
                            }
                        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showExportDialog() {
        if (getContext() == null) return;
        final String[] options = new String[]{
                getString(R.string.option_copy_clipboard),
                getString(R.string.option_share_logs),
                getString(R.string.option_export_csv)
        };

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.title_export_dialog)
                .setIcon(R.drawable.ic_share_24dp)
                .setItems(options, new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        exportLogs(which);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void exportLogs(final int mode) {
        if (getContext() == null) return;
        final Context context = requireContext();

        new AsyncTask<Void, Void, String[]>() {
            private int count = 0;

            @Override
            protected String[] doInBackground(Void... voids) {
                Cursor cursor = DatabaseHelper.getInstance(context).getLog(true, true, true, true, true);
                if (cursor == null) return null;

                try {
                    count = cursor.getCount();
                    if (count == 0) return null;

                    StringBuilder sb = new StringBuilder();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US);

                    if (mode == 2) {
                        // CSV header
                        sb.append("Timestamp,Status,Protocol,App,UID,Destination,Source,Host,Flags,Payload\n");
                    } else {
                        sb.append("=== AmniGuard Traffic Logs (").append(count).append(" entries) ===\n\n");
                    }

                    int colVersion = cursor.getColumnIndex("version");
                    int colProtocol = cursor.getColumnIndex("protocol");
                    int colSAddr = cursor.getColumnIndex("saddr");
                    int colSPort = cursor.getColumnIndex("sport");
                    int colDAddr = cursor.getColumnIndex("daddr");
                    int colDPort = cursor.getColumnIndex("dport");
                    int colUid = cursor.getColumnIndex("uid");
                    int colFlags = cursor.getColumnIndex("flags");
                    int colDName = cursor.getColumnIndex("dname");
                    int colData = cursor.getColumnIndex("data");
                    int colTime = cursor.getColumnIndex("time");
                    int colAllowed = cursor.getColumnIndex("allowed");

                    while (cursor.moveToNext()) {
                        long time = cursor.getLong(colTime);
                        int allowed = cursor.isNull(colAllowed) ? -1 : cursor.getInt(colAllowed);
                        int protocol = cursor.getInt(colProtocol);
                        int version = cursor.getInt(colVersion);
                        int uid = cursor.isNull(colUid) ? -1 : cursor.getInt(colUid);
                        String saddr = cursor.getString(colSAddr);
                        int sport = cursor.isNull(colSPort) ? -1 : cursor.getInt(colSPort);
                        String daddr = cursor.getString(colDAddr);
                        int dport = cursor.isNull(colDPort) ? -1 : cursor.getInt(colDPort);
                        String dname = cursor.getString(colDName);
                        String flags = cursor.getString(colFlags);
                        String data = cursor.getString(colData);

                        String appName = (uid >= 0 ? TextUtils.join(", ", Util.getApplicationNames(uid, context)) : "System / Kernel");
                        String statusStr = (allowed == 1 ? "ALLOWED" : allowed == 0 ? "BLOCKED" : "UNKNOWN");
                        String protoStr = Util.getProtocolName(protocol, version, true);
                        String hostStr = !TextUtils.isEmpty(dname) ? dname : daddr;
                        String destStr = daddr + (dport > 0 ? ":" + dport : "");
                        String srcStr = saddr + (sport > 0 ? ":" + sport : "");
                        String timeStr = sdf.format(new Date(time));

                        if (mode == 2) {
                            // CSV format
                            sb.append("\"").append(timeStr).append("\",");
                            sb.append("\"").append(statusStr).append("\",");
                            sb.append("\"").append(protoStr).append("\",");
                            sb.append("\"").append(appName.replace("\"", "\"\"")).append("\",");
                            sb.append(uid).append(",");
                            sb.append("\"").append(destStr).append("\",");
                            sb.append("\"").append(srcStr).append("\",");
                            sb.append("\"").append(hostStr.replace("\"", "\"\"")).append("\",");
                            sb.append("\"").append(flags != null ? flags : "").append("\",");
                            sb.append("\"").append(data != null ? data.replace("\"", "\"\"").replace("\n", " ") : "").append("\"\n");
                        } else {
                            // Text format
                            sb.append("[").append(timeStr).append("] [").append(statusStr).append("] ")
                              .append(protoStr).append(" | App: ").append(appName).append(" (UID ").append(uid).append(")")
                              .append(" | Dest: ").append(destStr)
                              .append(" | Host: ").append(hostStr);
                            if (!TextUtils.isEmpty(flags)) sb.append(" | Flags: ").append(flags);
                            if (!TextUtils.isEmpty(data)) sb.append(" | Payload: ").append(data);
                            sb.append("\n");
                        }
                    }

                    return new String[]{sb.toString(), String.valueOf(count)};
                } finally {
                    cursor.close();
                }
            }

            @Override
            protected void onPostExecute(String[] result) {
                if (getContext() == null) return;
                if (result == null || TextUtils.isEmpty(result[0])) {
                    Toast.makeText(context, R.string.msg_no_logs_to_export, Toast.LENGTH_SHORT).show();
                    return;
                }

                String text = result[0];
                int total = Integer.parseInt(result[1]);

                if (mode == 0) {
                    // Copy to clipboard
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("AmniGuard Logs", text);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(context, getString(R.string.msg_logs_copied_count, total), Toast.LENGTH_LONG).show();
                } else {
                    // Share via Intent
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType(mode == 2 ? "text/csv" : "text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "AmniGuard Traffic Logs (" + total + " entries)");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, text);
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.menu_export_logs)));
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void showRetentionDialog() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        int currentDays = prefs.getInt("log_retention_days", 3);
        final int[] dayValues = new int[]{1, 3, 7, 30, 0};
        String[] options = new String[]{
                getString(R.string.retention_1_day),
                getString(R.string.retention_3_days),
                getString(R.string.retention_7_days),
                getString(R.string.retention_30_days),
                getString(R.string.retention_forever)
        };

        int selectedIndex = 1; // Default 3 days
        for (int i = 0; i < dayValues.length; i++) {
            if (dayValues[i] == currentDays) {
                selectedIndex = i;
                break;
            }
        }

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.setting_log_retention)
                .setIcon(R.drawable.ic_hourglass_empty_white_24dp)
                .setSingleChoiceItems(options, selectedIndex, new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        final int chosenDays = dayValues[which];
                        prefs.edit().putInt("log_retention_days", chosenDays).apply();
                        dialog.dismiss();

                        if (chosenDays > 0) {
                            new AsyncTask<Void, Void, Void>() {
                                @Override
                                protected Void doInBackground(Void... voids) {
                                    long cutoff = System.currentTimeMillis() - (chosenDays * 24L * 3600L * 1000L);
                                    DatabaseHelper.getInstance(requireContext()).cleanupLog(cutoff);
                                    return null;
                                }

                                @Override
                                protected void onPostExecute(Void aVoid) {
                                    if (getContext() != null) {
                                        updateAdapter();
                                        Toast.makeText(requireContext(), R.string.msg_completed, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showLogDetailsDialog(Cursor cursor) {
        if (getContext() == null) return;
        PackageManager pm = requireContext().getPackageManager();

        int version = cursor.getInt(cursor.getColumnIndex("version"));
        int protocol = cursor.getInt(cursor.getColumnIndex("protocol"));
        final String saddr = cursor.getString(cursor.getColumnIndex("saddr"));
        final int sport = (cursor.isNull(cursor.getColumnIndex("sport")) ? -1 : cursor.getInt(cursor.getColumnIndex("sport")));
        final String daddr = cursor.getString(cursor.getColumnIndex("daddr"));
        final int dport = (cursor.isNull(cursor.getColumnIndex("dport")) ? -1 : cursor.getInt(cursor.getColumnIndex("dport")));
        final int uid = (cursor.isNull(cursor.getColumnIndex("uid")) ? -1 : cursor.getInt(cursor.getColumnIndex("uid")));
        final String flags = cursor.getString(cursor.getColumnIndex("flags"));
        final String dname = cursor.getString(cursor.getColumnIndex("dname"));
        final String data = cursor.getString(cursor.getColumnIndex("data"));
        final long time = cursor.getLong(cursor.getColumnIndex("time"));
        final int interactive = (cursor.isNull(cursor.getColumnIndex("interactive")) ? -1 : cursor.getInt(cursor.getColumnIndex("interactive")));
        final int allowed = (cursor.isNull(cursor.getColumnIndex("allowed")) ? -1 : cursor.getInt(cursor.getColumnIndex("allowed")));

        InetAddress addr = null;
        try {
            addr = InetAddress.getByName(daddr);
        } catch (UnknownHostException ex) {
            Log.e(TAG, ex.toString());
        }

        String ip;
        int port;
        if (addr != null && (addr.equals(vpn4) || addr.equals(vpn6))) {
            ip = saddr;
            port = sport;
        } else {
            ip = daddr;
            port = dport;
        }

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_log_details, null, false);
        androidx.appcompat.app.AlertDialog dialog = new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok, null)
                .create();

        // App info
        android.widget.ImageView ivDetailIcon = dialogView.findViewById(R.id.ivDetailIcon);
        TextView tvDetailAppName = dialogView.findViewById(R.id.tvDetailAppName);
        TextView tvDetailAppPkg = dialogView.findViewById(R.id.tvDetailAppPkg);
        TextView tvDetailStatusBadge = dialogView.findViewById(R.id.tvDetailStatusBadge);

        String appName = (uid >= 0 ? TextUtils.join(", ", Util.getApplicationNames(uid, requireContext())) : "System / Network Kernel");
        tvDetailAppName.setText(appName);
        tvDetailAppPkg.setText(uid >= 0 ? "UID " + uid : "UID -1 (Root/Kernel)");

        try {
            if (uid >= 0) {
                String[] packages = pm.getPackagesForUid(uid);
                if (packages != null && packages.length > 0) {
                    ivDetailIcon.setImageDrawable(pm.getApplicationIcon(packages[0]));
                    tvDetailAppPkg.setText("UID " + uid + " • " + packages[0]);
                } else {
                    ivDetailIcon.setImageResource(R.drawable.ic_security_white_24dp);
                }
            } else {
                ivDetailIcon.setImageResource(R.drawable.ic_security_white_24dp);
            }
        } catch (Throwable ex) {
            ivDetailIcon.setImageResource(R.drawable.ic_security_white_24dp);
        }

        // Status badge
        if (allowed == 1) {
            tvDetailStatusBadge.setText("ALLOWED");
            tvDetailStatusBadge.setTextColor(0xFFA6EE9A);
        } else if (allowed == 0) {
            tvDetailStatusBadge.setText("BLOCKED");
            tvDetailStatusBadge.setTextColor(0xFFFF8A80);
        } else {
            tvDetailStatusBadge.setVisibility(View.GONE);
        }

        // Connection telemetry
        TextView tvDetailHost = dialogView.findViewById(R.id.tvDetailHost);
        TextView tvDetailDestination = dialogView.findViewById(R.id.tvDetailDestination);
        TextView tvDetailSource = dialogView.findViewById(R.id.tvDetailSource);
        TextView tvDetailProtocol = dialogView.findViewById(R.id.tvDetailProtocol);
        TextView tvDetailFlags = dialogView.findViewById(R.id.tvDetailFlags);
        TextView tvDetailPayload = dialogView.findViewById(R.id.tvDetailPayload);
        TextView tvDetailTime = dialogView.findViewById(R.id.tvDetailTime);
        TextView tvDetailScreenState = dialogView.findViewById(R.id.tvDetailScreenState);
        View layoutExtraData = dialogView.findViewById(R.id.layoutExtraData);

        String host = !TextUtils.isEmpty(dname) ? dname : ip;
        tvDetailHost.setText(host);
        tvDetailDestination.setText(daddr + (dport > 0 ? " : " + dport : ""));
        tvDetailSource.setText(saddr + (sport > 0 ? " : " + sport : ""));
        tvDetailProtocol.setText(Util.getProtocolName(protocol, version, true));
        tvDetailFlags.setText(!TextUtils.isEmpty(flags) ? flags : "None");

        if (!TextUtils.isEmpty(data)) {
            layoutExtraData.setVisibility(View.VISIBLE);
            tvDetailPayload.setText(data);
        } else {
            layoutExtraData.setVisibility(View.GONE);
        }

        tvDetailTime.setText(SimpleDateFormat.getDateTimeInstance().format(new Date(time)));
        tvDetailScreenState.setText(interactive == 1 ? "Screen On (Interactive)" : interactive == 0 ? "Screen Off (Background)" : "Unknown");

        com.google.android.material.button.MaterialButton btnCopyIp = dialogView.findViewById(R.id.btnCopyIp);
        com.google.android.material.button.MaterialButton btnCopyLog = dialogView.findViewById(R.id.btnCopyLog);
        com.google.android.material.button.MaterialButton btnWhois = dialogView.findViewById(R.id.btnWhois);
        com.google.android.material.button.MaterialButton btnPortLookup = dialogView.findViewById(R.id.btnPortLookup);
        com.google.android.material.button.MaterialButton btnClearAppLogs = dialogView.findViewById(R.id.btnClearAppLogs);

        final String finalIp = ip;
        final int finalPort = port;
        final String finalAppName = appName;
        final String finalHost = host;
        final String finalDAddr = daddr;
        final int finalDPort = dport;
        final String finalSAddr = saddr;
        final int finalSPort = sport;
        final int finalProtocol = protocol;
        final int finalVersion = version;
        final String finalFlags = flags;
        final String finalData = data;
        final long finalTime = time;
        final int finalInteractive = interactive;
        final int finalAllowed = allowed;
        final int finalUid = uid;

        btnCopyIp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("ip", finalIp);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(requireContext(), R.string.msg_copied, Toast.LENGTH_SHORT).show();
            }
        });

        btnCopyLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder sb = new StringBuilder();
                sb.append("AmniGuard Packet Telemetry:\n");
                sb.append("• App: ").append(finalAppName).append(" (UID: ").append(finalUid).append(")\n");
                sb.append("• Status: ").append(finalAllowed == 1 ? "ALLOWED" : finalAllowed == 0 ? "BLOCKED" : "UNKNOWN").append("\n");
                sb.append("• Host/Domain: ").append(finalHost).append("\n");
                sb.append("• Destination: ").append(finalDAddr).append(finalDPort > 0 ? ":" + finalDPort : "").append("\n");
                sb.append("• Source: ").append(finalSAddr).append(finalSPort > 0 ? ":" + finalSPort : "").append("\n");
                sb.append("• Protocol: ").append(Util.getProtocolName(finalProtocol, finalVersion, true)).append("\n");
                sb.append("• Flags: ").append(!TextUtils.isEmpty(finalFlags) ? finalFlags : "None").append("\n");
                if (!TextUtils.isEmpty(finalData)) {
                    sb.append("• Payload: ").append(finalData).append("\n");
                }
                sb.append("• Time: ").append(SimpleDateFormat.getDateTimeInstance().format(new Date(finalTime))).append("\n");
                sb.append("• Screen: ").append(finalInteractive == 1 ? "Interactive (Screen On)" : finalInteractive == 0 ? "Background (Screen Off)" : "Unknown");

                ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("packet_telemetry", sb.toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(requireContext(), R.string.msg_detail_log_copied, Toast.LENGTH_SHORT).show();
            }
        });

        final Intent lookupIP = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.dnslytics.com/whois-lookup/" + finalIp));
        btnWhois.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pm.resolveActivity(lookupIP, 0) != null) {
                    startActivity(lookupIP);
                } else {
                    Toast.makeText(requireContext(), "No browser available", Toast.LENGTH_SHORT).show();
                }
            }
        });

        final Intent lookupPort = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.speedguide.net/port.php?port=" + finalPort));
        btnPortLookup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (finalPort > 0 && pm.resolveActivity(lookupPort, 0) != null) {
                    startActivity(lookupPort);
                } else {
                    Toast.makeText(requireContext(), "Invalid port", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (uid < 0) {
            btnClearAppLogs.setVisibility(View.GONE);
        } else {
            btnClearAppLogs.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                            .setTitle(R.string.title_delete)
                            .setMessage(getString(R.string.msg_confirm_clear_app_logs, finalAppName))
                            .setPositiveButton(R.string.title_delete, new android.content.DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(android.content.DialogInterface d, int which) {
                                    new AsyncTask<Void, Void, Void>() {
                                        @Override
                                        protected Void doInBackground(Void... voids) {
                                            DatabaseHelper.getInstance(requireContext()).clearLog(uid);
                                            return null;
                                        }

                                        @Override
                                        protected void onPostExecute(Void aVoid) {
                                            if (getContext() != null) {
                                                Toast.makeText(requireContext(), R.string.msg_logs_cleared, Toast.LENGTH_SHORT).show();
                                                dialog.dismiss();
                                                updateAdapter();
                                            }
                                        }
                                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .show();
                }
            });
        }

        dialog.show();
    }

    public void updateAdapter() {
        if (getContext() == null || lvLog == null) return;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        boolean udp = prefs.getBoolean("proto_udp", true);
        boolean tcp = prefs.getBoolean("proto_tcp", true);
        boolean other = prefs.getBoolean("proto_other", true);
        boolean allowed = prefs.getBoolean("traffic_allowed", true);
        boolean blocked = prefs.getBoolean("traffic_blocked", true);

        Cursor cursor = DatabaseHelper.getInstance(requireContext()).getLog(udp, tcp, other, allowed, blocked);
        if (adapter == null) {
            adapter = new AdapterLog(requireContext(), cursor, resolve, organization);
            adapter.setFilterQueryProvider(new FilterQueryProvider() {
                public Cursor runQuery(CharSequence constraint) {
                    if (getContext() == null) return null;
                    return DatabaseHelper.getInstance(requireContext()).searchLog(constraint.toString());
                }
            });
            lvLog.setAdapter(adapter);
        } else {
            adapter.changeCursor(cursor);
            if (lvLog.getAdapter() == null) {
                lvLog.setAdapter(adapter);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        DatabaseHelper.getInstance(requireContext()).addLogChangedListener(listener);
        updateFilterChips();
        updateAdapter();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getContext() != null) {
            DatabaseHelper.getInstance(requireContext()).removeLogChangedListener(listener);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (adapter != null) {
            adapter.changeCursor(null);
            adapter = null;
        }
        if (getContext() != null) {
            PreferenceManager.getDefaultSharedPreferences(requireContext()).unregisterOnSharedPreferenceChangeListener(this);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String name) {
        if ("resolve".equals(name) || "organization".equals(name) ||
                "proto_udp".equals(name) || "proto_tcp".equals(name) || "proto_other".equals(name) ||
                "traffic_allowed".equals(name) || "traffic_blocked".equals(name) || "log".equals(name) ||
                "log_app".equals(name) || "filter".equals(name) || "pcap".equals(name)) {
            resolve = prefs.getBoolean("resolve", false);
            organization = prefs.getBoolean("organization", false);
            if (adapter != null) {
                adapter.setResolve(resolve);
                adapter.setOrganization(organization);
            }
            updateFilterChips();
            updateAdapter();
        }
    }

    public AdapterLog getAdapter() {
        return adapter;
    }
}
