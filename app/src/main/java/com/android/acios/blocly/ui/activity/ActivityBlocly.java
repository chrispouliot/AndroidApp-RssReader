package com.android.acios.blocly.ui.activity;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;

import com.android.acios.blocly.BloclyApplication;
import com.android.acios.blocly.R;
import com.android.acios.blocly.api.DataSource;
import com.android.acios.blocly.api.model.RssFeed;
import com.android.acios.blocly.api.model.RssItem;
import com.android.acios.blocly.ui.adapter.NavigationDrawerAdapter;
import com.android.acios.blocly.ui.fragment.RssItemDetailFragment;
import com.android.acios.blocly.ui.fragment.RssItemListFragment;

import java.util.ArrayList;
import java.util.List;

public class ActivityBlocly extends AppCompatActivity implements NavigationDrawerAdapter.NavigationDrawerAdapterDelegate,
         NavigationDrawerAdapter.NavigationDrawerAdapterDataSource, RssItemListFragment.Delegate{


    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private NavigationDrawerAdapter navigationDrawerAdapter;
    private Menu menu;
    private View overFlowButton;
    private List<RssFeed> allFeeds = new ArrayList<>();
    private RssItem expandedItem = null;
    private boolean onTablet;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocly);

        onTablet = findViewById(R.id.fl_activity_blocly_right_pane) != null;

        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_activity_blocly);
        setSupportActionBar(toolbar);


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

                    if (item.getItemId() == R.id.action_share && expandedItem == null) {
                        continue;
                    }

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

                    if (item.getItemId() == R.id.action_share && expandedItem == null) {
                        continue;
                    }

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
        Log.v("In onCreate", "bla2");

        drawerLayout.setDrawerListener(drawerToggle);

        navigationDrawerAdapter = new NavigationDrawerAdapter();
        navigationDrawerAdapter.setDelegate(this);
        navigationDrawerAdapter.setDataSource(this);
        RecyclerView navigationRecyclerView = (RecyclerView) findViewById(R.id.rv_nav_activity_blocly);
        navigationRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        navigationRecyclerView.setItemAnimator(new DefaultItemAnimator());
        navigationRecyclerView.setAdapter(navigationDrawerAdapter);
        Log.v("In onCreate", "bla3");

        BloclyApplication.getSharedDataSource().fetchAllFeeds(new DataSource.Callback<List<RssFeed>>() {
            @Override
            public void onSuccess(List<RssFeed> rssFeeds) {
                Log.v("rssFeeds", rssFeeds.toString());
                allFeeds.addAll(rssFeeds);
                navigationDrawerAdapter.notifyDataSetChanged();
                getFragmentManager()
                        .beginTransaction()
                        .add(R.id.fl_activity_blocly, RssItemListFragment.fragmentForRssFeed(rssFeeds.get(0)))
                        .commit();
            }

            @Override
            public void onError(String errorMessage) {
                Log.v("Error", errorMessage);
            }
        });
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


        if (item.getItemId() == R.id.action_share) {
            RssItem itemToShare = expandedItem;
            if (itemToShare == null) {
                return false;
            }

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    String.format("%s (%s)", itemToShare.getTitle(), itemToShare.getUrl()));
            shareIntent.setType("text/plain");

            Intent chooser = Intent.createChooser(shareIntent, getString(R.string.share_chooser_title));
            startActivity(chooser);
        }

        switch ((String)item.getTitle()) {
            case ("Search"):
                Toast.makeText(this, "SEARCH!", Toast.LENGTH_SHORT).show();
                break;
            case ("Refresh"):
                Toast.makeText(this, "REFRESH!", Toast.LENGTH_SHORT).show();
                break;
            case ("Mark all as read"):
                Toast.makeText(this, "MARK ALL AS READ!", Toast.LENGTH_SHORT).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.blocly, menu);
        this.menu = menu;
        animateShareItem(expandedItem != null);
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

    private void animateShareItem(final boolean enabled) {
        MenuItem shareItem = menu.findItem(R.id.action_share);
        if (shareItem.isEnabled() == enabled) {
            return;
        }

        shareItem.setEnabled(enabled);
        final Drawable shareIcon = shareItem.getIcon();
        ValueAnimator valueAnimator = ValueAnimator.ofInt(enabled ? new int[]{0, 225} : new int[]{225, 0});
        valueAnimator.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                shareIcon.setAlpha((Integer) animation.getAnimatedValue());
            }
        });
        valueAnimator.start();

    }


    /*
     * ItemAdapterDelegate
     */

    public void onItemContracted(RssItemListFragment rssItemListFragment, RssItem rssItem) {
        if (expandedItem == rssItem) {
            expandedItem = null;
        }
        animateShareItem(expandedItem != null);
    }

    @Override
    public void onItemVisitClicked(RssItemListFragment rssItemListFragment, RssItem rssItem) {
        Intent visitIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(rssItem.getUrl()));
        startActivity(visitIntent);
    }


    /*
     * NavigationDrawerAdapterDataSource
     */

    @Override
    public List<RssFeed> getFeeds(NavigationDrawerAdapter adapter) {
        return allFeeds;
    }

    /*
     * RssListFragment.Delegate
     */

    @Override
    public void onItemExpanded(RssItemListFragment rssItemListFragment, RssItem rssItem) {
        expandedItem = rssItem;
        if (onTablet) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fl_activity_blocly_right_pane, RssItemDetailFragment.detailFragmentForRssItem(rssItem))
                    .commit();

            return;
        }
        animateShareItem(expandedItem != null);
    }



}
