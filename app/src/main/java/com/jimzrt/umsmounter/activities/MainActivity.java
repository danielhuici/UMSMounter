package com.jimzrt.umsmounter.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jimzrt.umsmounter.BuildConfig;
import com.jimzrt.umsmounter.R;
import com.jimzrt.umsmounter.fragments.CreditsFragment;
import com.jimzrt.umsmounter.fragments.DownloadFragment;
import com.jimzrt.umsmounter.fragments.ImageCreationFragment;
import com.jimzrt.umsmounter.fragments.MainFragment;
import com.jimzrt.umsmounter.model.DownloadItem;
import com.jimzrt.umsmounter.model.ImageItem;
import com.jimzrt.umsmounter.tasks.BaseTask;
import com.jimzrt.umsmounter.tasks.CheckFolderTask;
import com.jimzrt.umsmounter.tasks.CheckMassStorageTask;
import com.jimzrt.umsmounter.tasks.CheckPermissionTask;
import com.jimzrt.umsmounter.tasks.CheckRootTask;
import com.jimzrt.umsmounter.tasks.SetPathsTask;
import com.jimzrt.umsmounter.utils.BackgroundTask;
import com.jimzrt.umsmounter.utils.Helper;

public class MainActivity extends AppCompatActivity implements ImageCreationFragment.OnImageCreationListener, DownloadFragment.OnImageDownloadListener {


    public static final String ROOTDIR = "/UMSMounter";
    public static final String CACHEDIR = "/cache";

    public static String ROOTPATH;
    public static String USERPATH;

    public static final int WRITE_EXTERNAL_STORAGE_PERM = 1337;


    private MainFragment mainFragment;
    private ImageCreationFragment createImageFragment;
    private DownloadFragment downloadFragment;
    private CreditsFragment creditsFragment;

    private Fragment currentFragment;

    private DrawerLayout mDrawerLayout;
    private NavigationView navigationView;



    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        }



        if (findViewById(R.id.fragment_container) != null) {


            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            mainFragment = new MainFragment();
            createImageFragment = new ImageCreationFragment();
            downloadFragment = new DownloadFragment();
            creditsFragment = new CreditsFragment();



            SharedPreferences sharedPref = getSharedPreferences(null, Context.MODE_PRIVATE);
            boolean firstRun = sharedPref.getBoolean("firstRun", true);
            String version = sharedPref.getString("version", "");
            USERPATH = sharedPref.getString("userpath", "");
            ROOTPATH = sharedPref.getString("rootpath", "");

            if (firstRun || !BuildConfig.VERSION_NAME.equals(version)) {
                checkAll();
            } else {
                // Add the fragment to the 'fragment_container' FrameLayout
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, mainFragment).commit();
            }



        }

        currentFragment = mainFragment;

        navigationView = findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(0).setChecked(true);
        navigationView.setNavigationItemSelectedListener(
                menuItem -> {
                    // set item as selected to persist highlight
                    // menuItem.setChecked(true);

                    //   mDrawerLayout.closeDrawers();
                    final int delay = 150;
                    switch (menuItem.getItemId()) {
                        case R.id.nav_home:
                            if (currentFragment != mainFragment) {
                                new Handler().postDelayed(() -> showMain(), delay);
                            }
                            break;
                        case R.id.nav_create_image:
                            if (currentFragment != createImageFragment) {
                                new Handler().postDelayed(() -> showCreateImage(), delay);

                            }
                            break;
                        case R.id.nav_download_image:
                            if (currentFragment != downloadFragment) {
                                new Handler().postDelayed(() -> showDownloadImage(), delay);

                            }
                            break;
                        case R.id.nav_credits:
                            if (currentFragment != createImageFragment) {
                                new Handler().postDelayed(() -> showCredits(), delay);

                            }

                    }
                    // Add code here to update the UI based on the item selected
                    // For example, swap UI fragments here

                    mDrawerLayout.closeDrawer(GravityCompat.START);

                    return true;
                });


        //  }
        getSupportFragmentManager().addOnBackStackChangedListener(
                () -> {
                    if (mainFragment.isVisible()) {
                        navigationView.setCheckedItem(R.id.nav_home);
                        currentFragment = mainFragment;
                    } else if (createImageFragment.isAdded()) {
                        navigationView.setCheckedItem(R.id.nav_create_image);
                        currentFragment = createImageFragment;
                    } else if (downloadFragment.isAdded()) {
                        navigationView.setCheckedItem(R.id.nav_download_image);
                        currentFragment = downloadFragment;
                    } else if (creditsFragment.isAdded()) {
                        navigationView.setCheckedItem(R.id.nav_credits);
                        currentFragment = creditsFragment;
                    }

                });


        //Helper.trustAllHosts();
    }

    private void showDownloadImage() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in,
                android.R.anim.fade_out);

        if (currentFragment == mainFragment) {
            transaction.hide(mainFragment);
        } else {
            transaction.remove(currentFragment);
        }
        transaction.add(R.id.fragment_container, downloadFragment);


        transaction.addToBackStack(null);
        transaction.commit();
        currentFragment = downloadFragment;
        navigationView.setCheckedItem(R.id.nav_download_image);
    }

    private void showCredits() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in,
                android.R.anim.fade_out);

        if (currentFragment == mainFragment) {
            transaction.hide(mainFragment);
        } else {
            transaction.remove(currentFragment);
        }
        transaction.add(R.id.fragment_container, creditsFragment);


        transaction.addToBackStack(null);
        transaction.commit();
        currentFragment = creditsFragment;
        navigationView.setCheckedItem(R.id.nav_credits);
    }

    @Override
    public void onStart() {
        super.onStart();


    }

    private void showCreateImage() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in,
                android.R.anim.fade_out);
        if (currentFragment == mainFragment) {
            transaction.hide(mainFragment);
        } else {
            transaction.remove(currentFragment);
        }
        transaction.add(R.id.fragment_container, createImageFragment);
        transaction.addToBackStack(null);
        transaction.commit();
        currentFragment = createImageFragment;
        navigationView.setCheckedItem(R.id.nav_create_image);
    }

    private void showMain() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in,
                android.R.anim.fade_out);
        transaction.remove(currentFragment);
        transaction.show(mainFragment);
        //transaction.addToBackStack(null);
        transaction.commit();
        currentFragment = mainFragment;
        navigationView.setCheckedItem(R.id.nav_home);
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.action_revert:
                mainFragment.unmount("mtp,adb");
                return true;
            case R.id.action_check_dependencies:
                checkAll();
                return true;
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;

        }


        return super.onOptionsItemSelected(item);
    }


    private void checkAll() {


        SharedPreferences sharedPref = getSharedPreferences(null, Context.MODE_PRIVATE);
        (new BackgroundTask(this).setDelegate((successful, output) -> {
            if (successful) {


                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("firstRun", false);
                editor.putString("version", BuildConfig.VERSION_NAME);
                editor.apply();



            } else {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.clear().apply();
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setMessage(output)
                        .setTitle("Error!");
                builder.setPositiveButton("Ok", null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
            if (!mainFragment.isAdded()) {
                // Add the fragment to the 'fragment_container' FrameLayout
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, mainFragment).commit();
            }

        })).setTasks(new BaseTask[]{new CheckRootTask(), new CheckPermissionTask(), new SetPathsTask(), new CheckFolderTask(), new CheckMassStorageTask()}).execute();
    }

    @Override
    public void OnImageCreation(String imageItemName) {
        showMain();
        ImageItem imageItem = new ImageItem(imageItemName, ROOTPATH + "/" + imageItemName, USERPATH + "/" + imageItemName, Helper.humanReadableByteCount(0));
        mainFragment.createImage(imageItem);

    }

    @Override
    public void OnImageListClick(DownloadItem downloadItem) {

        Gson gson = new GsonBuilder().create();
        String downloadItemString = gson.toJson(downloadItem);

        Intent intent = new Intent(this, LinuxImageActivity.class);


        intent.putExtra("downloadItem", downloadItemString);
        startActivityForResult(intent, 0);


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check which request we're responding to
        if (requestCode == 0) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
//
                String name = data.getStringExtra("name");
                String url = data.getStringExtra("url");
                ImageItem imageItem = new ImageItem(name, ROOTPATH + "/" + name, USERPATH + "/" + name, Helper.humanReadableByteCount(0));
                imageItem.setUrl(url);
                showMain();
                mainFragment.addImage(imageItem);

            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {

        if (requestCode == WRITE_EXTERNAL_STORAGE_PERM) {
            SharedPreferences sharedPref = getSharedPreferences(null, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("hasPermission", true);
            editor.apply();

            Toast.makeText(this, "granteddd!!!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "dont know this shit", Toast.LENGTH_LONG).show();
        }

    }
}
