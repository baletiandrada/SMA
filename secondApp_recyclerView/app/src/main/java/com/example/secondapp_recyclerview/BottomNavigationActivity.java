package com.example.secondapp_recyclerview;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.secondapp_recyclerview.fragments.DashboardFragment;
import com.example.secondapp_recyclerview.fragments.HomeFragment;
import com.example.secondapp_recyclerview.fragments.NotificationsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class BottomNavigationActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemReselectedListener{

    private HomeFragment homeFragment;
    private DashboardFragment dashboardFragment;
    private NotificationsFragment notificationsFragment;

    private Fragment activeFragment;
    final FragmentManager fragmentManager = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation);
        BottomNavigationView navView = findViewById(R.id.nav_view);

        initializeViews();
        navView.setOnNavigationItemReselectedListener(this);
        loadFragments();
    }

    private void initializeViews() {
        homeFragment = new HomeFragment();
        dashboardFragment = new DashboardFragment();
        notificationsFragment = new NotificationsFragment();
        activeFragment = homeFragment;
    }

    @Override
    public void onNavigationItemReselected(@NonNull MenuItem item) {
        switch ((item.getItemId()))
        {
            case R.id.navigation_home:
                fragmentManager.beginTransaction().hide(activeFragment).show(homeFragment).commit();
                activeFragment = homeFragment;
                break;
            case R.id.navigation_dashboard:
                fragmentManager.beginTransaction().hide(activeFragment).show(dashboardFragment).commit();
                activeFragment = dashboardFragment;
                break;
            case R.id.navigation_notifications:
                fragmentManager.beginTransaction().hide(activeFragment).show(notificationsFragment).commit();
                activeFragment = notificationsFragment;
                break;
        }
    }

    public void loadFragments()
    {
        fragmentManager.beginTransaction().add(R.id.nav_host_fragment, homeFragment, "1").commit();
        fragmentManager.beginTransaction().add(R.id.nav_host_fragment, dashboardFragment, "1").hide(dashboardFragment).commit();
        fragmentManager.beginTransaction().add(R.id.nav_host_fragment, notificationsFragment, "1").hide(notificationsFragment).commit();
    }
}