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

    private BooksMainViewFragment booksMainViewFragment;
    private PlanningBooksFragment planningBooksFragment;
    private BooksRecommendedFragment booksRecommendedFragment;
    private EditUserDataFragment editUserDataFragment;
    private MoreActionsFragment moreActionsFragment;

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
        booksMainViewFragment = new BooksMainViewFragment();
        planningBooksFragment = new PlanningBooksFragment();
        booksRecommendedFragment = new BooksRecommendedFragment();
        editUserDataFragment = new EditUserDataFragment();
        moreActionsFragment = new MoreActionsFragment();
        activeFragment = booksMainViewFragment;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch ((item.getItemId()))
        {
            case R.id.navigation_books:
                fragmentManager.beginTransaction().hide(activeFragment).show(booksMainViewFragment).detach(booksMainViewFragment).attach(booksMainViewFragment).commit();
                activeFragment = booksMainViewFragment;
                return true;
            case R.id.navigation_planning_books:
                fragmentManager.beginTransaction().hide(activeFragment).show(planningBooksFragment).commit();
                activeFragment = planningBooksFragment;
                return true;
            case R.id.navigation_books_recommended:
                fragmentManager.beginTransaction().hide(activeFragment).show(booksRecommendedFragment).commit();
                activeFragment = booksRecommendedFragment;
                return true;
            case R.id.navigation_edit_profile:
                fragmentManager.beginTransaction().hide(activeFragment).show(editUserDataFragment).commit();
                activeFragment = editUserDataFragment;
                return true;
            case R.id.navigation_more_actions:
                fragmentManager.beginTransaction().hide(activeFragment).show(moreActionsFragment).commit();
                activeFragment = moreActionsFragment;
                return true;
        }
        return false;
    }

    public void loadFragments()
    {
        fragmentManager.beginTransaction().add(R.id.nav_host_fragment, booksMainViewFragment, "1").commit();
        fragmentManager.beginTransaction().add(R.id.nav_host_fragment, planningBooksFragment, "1").hide(planningBooksFragment).commit();
        fragmentManager.beginTransaction().add(R.id.nav_host_fragment, booksRecommendedFragment, "1").hide(booksRecommendedFragment).commit();
        fragmentManager.beginTransaction().add(R.id.nav_host_fragment, editUserDataFragment, "1").hide(editUserDataFragment).commit();
        fragmentManager.beginTransaction().add(R.id.nav_host_fragment, moreActionsFragment, "1").hide(moreActionsFragment).commit();
    }
}