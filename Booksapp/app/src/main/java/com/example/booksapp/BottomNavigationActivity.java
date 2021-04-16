package com.example.booksapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.booksapp.fragments.*;

public class BottomNavigationActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private BooksReadFragment booksReadFragment;
    private BooksPlannedFragment booksPlannedFragment;
    private BooksRecommendedFragment booksRecommendedFragment;
    private FavouriteBooksFragment favouriteBooksFragment;
    private AccountSettingsFragment accountSettingsFragment;

    private BottomNavigationView navView;

    private Fragment activeFragment;
    final FragmentManager fragmentManager = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation);

        navView = findViewById(R.id.nav_view);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        initializeViews();
        navView.setOnNavigationItemSelectedListener(this);
        loadFragments();
    }

    private void initializeViews() {
        booksReadFragment = new BooksReadFragment();
        booksPlannedFragment = new BooksPlannedFragment();
        booksRecommendedFragment = new BooksRecommendedFragment();
        favouriteBooksFragment = new FavouriteBooksFragment();
        accountSettingsFragment = new AccountSettingsFragment();
        activeFragment = booksReadFragment;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch ((item.getItemId()))
        {
            case R.id.navigation_books:
                fragmentManager.beginTransaction().hide(activeFragment).show(booksReadFragment).detach(booksReadFragment).attach(booksReadFragment).commit();
                activeFragment = booksReadFragment;
                return true;
            case R.id.navigation_planning_books:
                fragmentManager.beginTransaction().hide(activeFragment).show(booksPlannedFragment).commit();
                activeFragment = booksPlannedFragment;
                return true;
            case R.id.navigation_books_recommended:
                fragmentManager.beginTransaction().hide(activeFragment).show(booksRecommendedFragment).commit();
                activeFragment = booksRecommendedFragment;
                return true;
            case R.id.navigation_edit_profile:
                fragmentManager.beginTransaction().hide(activeFragment).show(favouriteBooksFragment).commit();
                activeFragment = favouriteBooksFragment;
                return true;
            case R.id.navigation_more_actions:
                fragmentManager.beginTransaction().hide(activeFragment).show(accountSettingsFragment).commit();
                activeFragment = accountSettingsFragment;
                return true;
        }
        return false;
    }

    public void loadFragments()
    {
        fragmentManager.beginTransaction().add(R.id.nav_host_fragment, booksReadFragment, "1").commit();
        fragmentManager.beginTransaction().add(R.id.nav_host_fragment, booksPlannedFragment, "1").hide(booksPlannedFragment).commit();
        fragmentManager.beginTransaction().add(R.id.nav_host_fragment, booksRecommendedFragment, "1").hide(booksRecommendedFragment).commit();
        fragmentManager.beginTransaction().add(R.id.nav_host_fragment, favouriteBooksFragment, "1").hide(favouriteBooksFragment).commit();
        fragmentManager.beginTransaction().add(R.id.nav_host_fragment, accountSettingsFragment, "1").hide(accountSettingsFragment).commit();
    }
}