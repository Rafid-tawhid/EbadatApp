package com.example.ibadatproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationMenu;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    FloatingActionButton floatingActionButton;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;

    LinearLayout linearLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView=findViewById(R.id.bottom_nav);
        floatingActionButton=findViewById(R.id.fab);
        bottomNavigationView.setBackground(null);
        linearLayout=findViewById(R.id.layout_fragment);
        bottomNavigationView.getMenu().getItem(2).setEnabled(false);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        toolbar = findViewById(R.id.toolbar);





        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.loop:
                        SearchFragment searchFragmentFragment=new SearchFragment();
                        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.frame_id,searchFragmentFragment);
                        transaction.commit();


                        return true;
                    case R.id.home:
                        HomeFragment firstFragment=new HomeFragment();
                        FragmentTransaction transaction2=getSupportFragmentManager().beginTransaction();
                        transaction2.replace(R.id.frame_id,firstFragment);
                        transaction2.commit();
                        return true;
                    case R.id.notif:
                        NotifFragment notifFragment=new NotifFragment();
                        FragmentTransaction transaction3=getSupportFragmentManager().beginTransaction();
                        transaction3.replace(R.id.frame_id,notifFragment);
                        transaction3.commit();
                        return true;
                    case R.id.menu:
                        MenuFragment menuFragment=new MenuFragment();
                        FragmentTransaction transaction4=getSupportFragmentManager().beginTransaction();
                        transaction4.replace(R.id.frame_id,menuFragment);
                        transaction4.commit();
                        Log.d("aa", String.valueOf("hello"+transaction4));
                        return true;
                }


                return false;
            }
        });

        ActionBarDrawerToggle toggle = new
                ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
                    /** Called when a drawer has settled in a completely closed state. */
                    public void onDrawerClosed(View view) {
                        super.onDrawerClosed(view);
                   floatingActionButton.show();
                    }

                    /** Called when a drawer has settled in a completely open state. */
                    public void onDrawerOpened(View drawerView) {
                        super.onDrawerOpened(drawerView);
                        floatingActionButton.hide();
                    }
                };


        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();



        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                if (item.getItemId() == R.id.nav_men) {
//                    Toast.makeText(MainActivity.this, "okk", Toast.LENGTH_SHORT).show();
//
//                }
//                if (item.getItemId() == R.id.nav_women) {
//                    Toast.makeText(MainActivity.this, "okk", Toast.LENGTH_SHORT).show();
//
//                }

                return false;
            }
        });
        navigationView.setCheckedItem(R.id.nav_home);
        navigationView.bringToFront();

    }



}