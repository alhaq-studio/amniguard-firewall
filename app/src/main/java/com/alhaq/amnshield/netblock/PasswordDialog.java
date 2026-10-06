package com.alhaq.amnshield.netblock;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class PasswordDialog {

    public interface PasswordCallback {
        void onPasswordVerified();
        void onPasswordCancelled();
    }

    public static void showVerifyDialog(final Context context, final PasswordCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.title_password_enter);

        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setHint(R.string.hint_password);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        lp.setMargins(50, 0, 50, 0);
        input.setLayoutParams(lp);

        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String password = input.getText().toString();
                if (PasswordHelper.verifyPassword(context, password)) {
                    if (callback != null) callback.onPasswordVerified();
                } else {
                    Toast.makeText(context, R.string.msg_password_incorrect, Toast.LENGTH_SHORT).show();
                    if (callback != null) callback.onPasswordCancelled();
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                if (callback != null) callback.onPasswordCancelled();
            }
        });

        builder.show();
    }

    public static void showSetPasswordDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.title_password_set);

        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(50, 20, 50, 20);

        final EditText inputPassword = new EditText(context);
        inputPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        inputPassword.setHint(R.string.hint_password);
        container.addView(inputPassword);

        final EditText inputConfirm = new EditText(context);
        inputConfirm.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        inputConfirm.setHint(R.string.hint_password_confirm);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 20, 0, 0);
        inputConfirm.setLayoutParams(lp);
        container.addView(inputConfirm);

        builder.setView(container);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String password = inputPassword.getText().toString();
                String confirm = inputConfirm.getText().toString();

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(context, R.string.msg_password_empty, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(confirm)) {
                    Toast.makeText(context, R.string.msg_password_mismatch, Toast.LENGTH_SHORT).show();
                    return;
                }

                PasswordHelper.setPassword(context, password);
                Toast.makeText(context, R.string.msg_password_set, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        if (PasswordHelper.isPasswordSet(context)) {
            builder.setNeutralButton(R.string.menu_clear, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    PasswordHelper.removePassword(context);
                    Toast.makeText(context, R.string.msg_password_removed, Toast.LENGTH_SHORT).show();
                }
            });
        }

        builder.show();
    }
}
