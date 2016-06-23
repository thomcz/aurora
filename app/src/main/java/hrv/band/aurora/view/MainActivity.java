package hrv.band.aurora.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Date;

import hrv.band.aurora.Control.Calculation;
import hrv.band.aurora.Control.HRVParameters;
import hrv.band.aurora.Fourier.FastFourierTransform;
import hrv.band.aurora.Interpolation.CubicSplineInterpolation;
import hrv.band.aurora.R;
import hrv.band.aurora.RRInterval.IRRInterval;
import hrv.band.aurora.RRInterval.Interval;
import hrv.band.aurora.RRInterval.msband.MSBandRRInterval;
import hrv.band.aurora.storage.IStorage;
import hrv.band.aurora.storage.SharedPreferencesController;
import hrv.band.aurora.view.fragment.MeasuringFragment;
import hrv.band.aurora.view.fragment.OverviewFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;
    private IRRInterval rrInterval;
    private MeasuringFragment measureFragment;
    private OverviewFragment overviewFragment;

    public static final String HRV_PARAMETER_ID = "HRV_PARAMETER";

    private Interval ival;
    private TextView rrStatus;
    private TextView txtStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MeasureActivity.class);
                startActivity(intent);
            }
        });

        //NavigationDrawer

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Fragment

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        //measureFragment = new MeasuringFragment();

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);


        rrStatus = (TextView) findViewById(R.id.rrStatus);
        txtStatus = (TextView) findViewById(R.id.measure_status);
        rrInterval = new MSBandRRInterval(this, txtStatus, rrStatus);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            IStorage storage = new SharedPreferencesController();
            HRVParameters parameters = storage.loadData(getApplicationContext(), null).get(0);
            double[] rr = parameters.getRRIntervals();
            Calculation calc = new Calculation(new FastFourierTransform(4096), new CubicSplineInterpolation());
            calc.Calculate(new Interval(parameters.getTime(), rr));
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {


        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if (position == 0) {
                return measureFragment = new MeasuringFragment();
            }
            return overviewFragment = new OverviewFragment();
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "MEASURING";
                case 1:
                    return "OVERVIEW";
            }
            return null;
        }
    }

    public void startMeasuring(View view) {
        measureFragment.startAnimation(new Interval(new Date()), rrInterval, (ProgressBar) findViewById(R.id.progressBar));
    }

    public void getDevicePermission(View view) {
        rrInterval.getDevicePermission();
    }





    @Override
    protected void onResume() {
        super.onResume();
        //reset to default
    }

    @Override
    protected void onPause() {
        super.onPause();
        rrInterval.pauseMeasuring();
    }

    @Override
    protected void onDestroy() {
        rrInterval.destroy();
        super.onDestroy();
    }

    /**
     * write data to UI-thread
     * @param string the text to write
     */
    /*public void appendToUI(final String string, final TextView txt) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txt.setText(string);
            }
        });
    }*/

}
