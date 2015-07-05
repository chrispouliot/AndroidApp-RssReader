package com.android.acios.blocly.ui.activity;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.acios.blocly.R;
import com.android.acios.blocly.api.model.RssFeed;
import com.android.acios.blocly.ui.adapter.ItemAdapter;
import com.android.acios.blocly.ui.adapter.NavigationDrawerAdapter;

import java.util.ArrayList;

public class ActivityBlocly extends AppCompatActivity implements NavigationDrawerAdapter.NavigationDrawerAdapterDelegate, ItemAdapter.ItemAdapterDelegate{

    private ItemAdapter itemAdapter;
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private NavigationDrawerAdapter navigationDrawerAdapter;
    private Menu menu;
    private View overFlowButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocly);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_activity_blocly);
        setSupportActionBar(toolbar);

        itemAdapter = new ItemAdapter();
        itemAdapter.setDelegate(this);

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_activity_blocly);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {


            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                Log.d("onSwiped", " User swiped");
                recyclerView.getAdapter().notifyDataSetChanged();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(itemAdapter);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        drawerLayout = (DrawerLayout) findViewById(R.id.dl_activity_blocly);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, 0, 0) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (overFlowButton != null) {
                    overFlowButton.setEnabled(true);
                }
                if (menu == null) {
                    return;
                }
                for (int i = 0; i< menu.size(); i ++) {
                    MenuItem item = menu.getItem(i);
                    item.setEnabled(true);
                    Drawable icon = item.getIcon();
                    if (icon != null) {
                        icon.setAlpha(255);
                    }
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (overFlowButton != null) {
                    overFlowButton.setEnabled(false);
                }
                if (menu == null) {
                    return;
                }
                for (int i = 0; i< menu.size(); i ++) {
                    menu.getItem(i).setEnabled(false);
                }
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                if (overFlowButton == null) {
                    ArrayList<View> foundViews = new ArrayList<View>();
                    getWindow().getDecorView().findViewsWithText(foundViews,
                            getString(R.string.abc_action_menu_overflow_description),
                            View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
                    if (foundViews.size() > 0) {
                        overFlowButton = foundViews.get(0);
                    }
                }

                if (overFlowButton != null) {
                    overFlowButton.setAlpha(1f - slideOffset);
                }
                if (menu == null) {
                    return;
                }

                for (int i = 0; i< menu.size(); i++) {
                    MenuItem item = menu.getItem(i);
                    Drawable icon = item.getIcon();
                    if (icon != null) {
                        icon.setAlpha((int) ((1f - slideOffset * 255)));
                    }
                }
            }
        };

        // BAM I FIGURED OUT HOW TO MAKE THE DRAWER BE OPEN ON FIRST OPEN OF APP WOOT
        if (!hasDrawerDemod()) {
            drawerLayout.openDrawer(Gravity.LEFT);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("hasDemod", true);
            editor.commit();
        }

        drawerLayout.setDrawerListener(drawerToggle);

        navigationDrawerAdapter = new NavigationDrawerAdapter();
        navigationDrawerAdapter.setDelegate(this);
        RecyclerView navigationRecyclerView = (RecyclerView) findViewById(R.id.rv_nav_activity_blocly);
        navigationRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        navigationRecyclerView.setItemAnimator(new DefaultItemAnimator());
        navigationRecyclerView.setAdapter(navigationDrawerAdapter);
    }

    public boolean hasDrawerDemod() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean hasDrawerDemod = prefs.getBoolean("hasDemod", false);
        return hasDrawerDemod;
    }

    @Override
    protected void onPostCreate(Bundle savedInstance) {
        super.onPostCreate(savedInstance);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch ((String)item.getTitle()) {
            case ("Search"):
                Toast.makeText(this, "SEARCH!", Toast.LENGTH_SHORT).show();
                break;
            case ("Share"):
                Toast.makeText(this, "SHARE!", Toast.LENGTH_SHORT).show();
                break;
            case ("Refresh"):
                Toast.makeText(this, "REFRESH!", Toast.LENGTH_SHORT).show();
                break;
            case ("Mark all as read"):
                Toast.makeText(this, "MARK ALL AS READ!", Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(this, "This item was not in the switch case: " + item.getTitle(), Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.blocly, menu);
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    /*
     * NavigationDrawerAdapterDelegate
     */

    @Override
    public void didSelectNavigationOption(NavigationDrawerAdapter adapter, NavigationDrawerAdapter.NavigationOption navigationOption) {
        drawerLayout.closeDrawers();
        Toast.makeText(this, "Show the " + navigationOption.name(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void didSelectFeed(NavigationDrawerAdapter adapter, RssFeed rssFeed) {
        drawerLayout.closeDrawers();
        Toast.makeText(this, "Show RSS items from " + rssFeed.getTitle(), Toast.LENGTH_SHORT).show();
    }

    /*
     * ItemAdapterDelegate
     */

    @Override
    public void didExpandItem(View itemView) {
        Toast.makeText(this, "VIEW EXPANDED", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void didContractItem(View itemView) {
        Toast.makeText(this, "VIEW CONTRACTED", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void didClickVisitSite(String site) {
        Toast.makeText(this, "Visit " + site, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void didFavorite(View view, boolean isChecked) {
        Toast.makeText(this, isChecked ? "Favorited" : "Unfavorited", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void didArchive(View view, boolean isChecked) {
        Toast.makeText(this, isChecked ? "Archived" : "Unarchived", Toast.LENGTH_SHORT).show();
    }

}
