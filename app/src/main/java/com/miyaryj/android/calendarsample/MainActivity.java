package com.miyaryj.android.calendarsample;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.WindowCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.miyaryj.android.calendarsample.google.GoogleService;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout mDrawerLayout;

    private NavigationView mNavigationView;

    private GoogleService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar,
                R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerClosed(View view) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
            }
        };
        toolbar.setNavigationIcon(R.drawable.drawer_icn);
        toggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(toggle);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        mService = new GoogleService(this);

        startLocalCalendarFragment();
    }

    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mService.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        switch(menuItem.getItemId()) {
            case R.id.navigation_local_calendar:
                startLocalCalendarFragment();
                break;
            case R.id.navigation_google_calendar:
                startGoogleCalendarFragment();
                break;
        }
        mDrawerLayout.closeDrawers();
        return true;
    }

    GoogleService getGoogleService() {
        return mService;
    }

    private void startLocalCalendarFragment() {
        FragmentManager fm = getFragmentManager();
        Fragment f = fm.findFragmentByTag(LocalCalendarFragment.TAG);
        if (f == null) {
            f = LocalCalendarFragment.newInstance();
            fm.beginTransaction().replace(R.id.fragment_container, f, LocalCalendarFragment.TAG)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        }
    }

    private void startGoogleCalendarFragment() {
        FragmentManager fm = getFragmentManager();
        Fragment f = fm.findFragmentByTag(GoogleCalendarFragment.TAG);
        if (f == null) {
            f = GoogleCalendarFragment.newInstance();
            fm.beginTransaction().replace(R.id.fragment_container, f, GoogleCalendarFragment.TAG)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        }
    }
}
