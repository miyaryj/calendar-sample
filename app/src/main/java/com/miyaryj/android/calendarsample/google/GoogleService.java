package com.miyaryj.android.calendarsample.google;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.ExponentialBackOff;

import java.util.Arrays;

public class GoogleService {
    private static final String[] ACCOUNT_TYPES = new String[]{"com.google"};

    private static final int REQUEST_ACCOUNT_PICKER = 1000;
    private static final int REQUEST_AUTHORIZATION = 1001;
    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;

    private static final String PREF_ACCOUNT_NAME = "google_account_name";

    private final Activity mActivity;

    public GoogleService(Activity activity) {
        if (activity == null) {
            throw new IllegalArgumentException("Args cannot be null.");
        }

        mActivity = activity;
    }

    public void setAccountName(String accountName) {
        if(TextUtils.isEmpty(accountName)) {
            return;
        }

        SharedPreferences settings =
                mActivity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREF_ACCOUNT_NAME, accountName);
        editor.apply();
    }

    public String getAccountName() {
        return mActivity.getPreferences(Context.MODE_PRIVATE)
                .getString(PREF_ACCOUNT_NAME, null);
    }

    public GoogleAccountCredential getCredential(String[] scopes) {
        return GoogleAccountCredential.usingOAuth2(
                mActivity, Arrays.asList(scopes))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(getAccountName());
    }

    public boolean respond(Throwable e) {
        if (e instanceof NoAccountException) {
            Intent intent = AccountPicker
                    .newChooseAccountIntent(null, null, ACCOUNT_TYPES, false, null, null, null, null);
            mActivity.startActivityForResult(intent, REQUEST_ACCOUNT_PICKER);
            return true;
        } else if (e instanceof GooglePlayServicesAvailabilityIOException) {
            int statusCode = ((GooglePlayServicesAvailabilityIOException)e).getConnectionStatusCode();
            showPlayServicesAvailabilityError(statusCode);
            return true;
        } else if (e instanceof UserRecoverableAuthIOException) {
            mActivity.startActivityForResult(
                    ((UserRecoverableAuthIOException) e).getIntent(),
                    REQUEST_AUTHORIZATION);
            return true;
        }
        return false;
    }

    private void showPlayServicesAvailabilityError(int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                mActivity,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == Activity.RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (!TextUtils.isEmpty(accountName)) {
                        setAccountName(accountName);
                        return true;
                    }
                }
                break;
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode == Activity.RESULT_OK) {
                    return true;
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == Activity.RESULT_OK) {
                    return true;
                }
                break;
        }
        return false;
    }

    public CalendarClient calendar() {
        return new CalendarClient(this);
    }

}
