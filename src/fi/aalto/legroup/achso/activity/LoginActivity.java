/**
 * Copyright 2013 Aalto university, see AUTHORS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fi.aalto.legroup.achso.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import fi.aalto.legroup.achso.R;
import fi.aalto.legroup.achso.state.i5LoginState;
import fi.aalto.legroup.achso.util.App;

public class LoginActivity extends ActionbarActivity {

    private final Context ctx = this;
    private EditText mUserName;
    private EditText mPassword;
    private BroadcastReceiver mReceiver = null;
    private IntentFilter mFilter = null;
    private
    View.OnClickListener mLoginButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            CharSequence username_cs = mUserName.getText();
            CharSequence password_cs = mPassword.getText();
            String username = "";
            String password = "";
            if (username_cs != null) {
                username = username_cs.toString();
            }
            if (password_cs != null) {
                password = password_cs.toString();
            }

            App.login_state.login(username, password);
        }
    };

    private View.OnClickListener mAutoLoginCheckBoxListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            boolean checked = ((CheckBox) v).isChecked();
            SharedPreferences prefs = ctx.getSharedPreferences("AchSoPrefs", 0);
            Editor edit = prefs.edit();
            edit.putBoolean("autologin", checked);
            edit.apply();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i("LoginActivity", "Inflating options menu - LoginActivity");
        mMenu = menu;
        App.login_state.setHostActivity(this);
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.main_menubar, menu);
        // Remove search as it does not make sense here
        menu.removeItem(R.id.action_search);
        updateLoginMenuItem();
        return true;
    }

    private void close_this(String intentdata) {
        Toast.makeText(ctx, "received" + intentdata, Toast.LENGTH_LONG).show();
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        SharedPreferences prefs = ctx.getSharedPreferences("AchSoPrefs", 0);
        boolean autologin = prefs.getBoolean("autologin", false);

        if (mFilter == null && mReceiver == null) {
            mFilter = new IntentFilter();
            mFilter.addAction(i5LoginState.LOGIN_SUCCESS);
            mFilter.addAction(i5LoginState.LOGIN_FAILED);
            mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            mReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    updateLoginMenuItem();
                    if (intent.getAction() != null && intent.getAction().equals(i5LoginState.LOGIN_SUCCESS)) {
                        close_this(intent.getAction());
                    }
                }
            };
        }

        mUserName = (EditText) findViewById(R.id.username_field);
        mPassword = (EditText) findViewById(R.id.password_field);

        if (autologin) {
            mUserName.setText(prefs.getString("login", ""));
            mPassword.setText(prefs.getString("pwd", ""));
        }
        Button loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(mLoginButtonClickListener);

        CheckBox autoLoginCheckBox = (CheckBox) findViewById(R.id.autologin_checkbox);
        autoLoginCheckBox.setOnClickListener(mAutoLoginCheckBoxListener);
        autoLoginCheckBox.setChecked(autologin);

    }

    @Override
    protected void onResume() {
        super.onResume();
        this.registerReceiver(mReceiver, mFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(mReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
