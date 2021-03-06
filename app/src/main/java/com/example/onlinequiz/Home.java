package com.example.onlinequiz;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.onlinequiz.Common.Common;
import com.example.onlinequiz.Fragment.InternetStatusFragment;
import com.example.onlinequiz.Fragment.VoiceFragment;
import com.example.onlinequiz.Interface.IFragmentCommunicate;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.JsonObject;

public class Home extends Activity {
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        initOnNavigationItemSelected();
        setDefaultFragment();
        initInternetStatusFragment();
    }

    private void initOnNavigationItemSelected() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                switch (item.getItemId()) {
                    case R.id.action_category:
                        selectedFragment = CategoryFragment.newInstance();
                        transaction.replace(R.id.frame_layout, selectedFragment);
                        transaction.commit();
                        break;
                    case R.id.action_ranking:
                        selectedFragment = RankingFragment.newInstance();
                        transaction.replace(R.id.frame_layout, selectedFragment);
                        transaction.commit();
                        break;
                    case R.id.action_myTests:
                        Intent i = new Intent(Home.this, MyTestActivity.class);
                        startActivity(i);
                        break;
                }
                return true;
            }
        });
    }

    private void setDefaultFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, CategoryFragment.newInstance());
        transaction.commit();
    }

    // MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_right_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuLogout:
                showLogoutDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // LOGOUT
    private void showLogoutDialog() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage("Do you want to logout?");

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onLogout();
            }
        });
        alertDialog.show();
    }

    private void onLogout() {
        SharedPreferences sharedPreferences = getSharedParams();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("loggedUsername");
        editor.remove("loggedPassword");
        editor.apply();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Common.setCurrentUser(null);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
        super.onBackPressed();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

}