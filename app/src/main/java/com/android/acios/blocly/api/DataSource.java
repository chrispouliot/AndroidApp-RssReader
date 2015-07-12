package com.android.acios.blocly.api;

import android.database.sqlite.SQLiteDatabase;

import com.android.acios.blocly.BloclyApplication;
import com.android.acios.blocly.BuildConfig;
import com.android.acios.blocly.api.model.RssFeed;
import com.android.acios.blocly.api.model.RssItem;
import com.android.acios.blocly.api.model.database.DatabaseOpenHelper;
import com.android.acios.blocly.api.model.database.table.RssFeedTable;
import com.android.acios.blocly.api.model.database.table.RssItemTable;
import com.android.acios.blocly.api.network.GetFeedsNetworkRequest;

import java.util.ArrayList;
import java.util.List;

public class DataSource {

    private DatabaseOpenHelper databaseOpenHelper;
    private RssFeedTable rssFeedTable;
    private RssItemTable rssItemTable;
    private List<RssFeed> feeds;
    private List<RssItem> items;

    public DataSource() {

        rssFeedTable = new RssFeedTable();
        rssItemTable = new RssItemTable();
        databaseOpenHelper = new DatabaseOpenHelper(BloclyApplication.getSharedInstance(), rssFeedTable, rssItemTable);

        feeds = new ArrayList<>();
        items = new ArrayList<>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (BuildConfig.DEBUG && false) {
                    BloclyApplication.getSharedInstance().deleteDatabase("blocly_db");
                }
                SQLiteDatabase writableDatabase = databaseOpenHelper.getWritableDatabase();
                new GetFeedsNetworkRequest("http://feeds.feedburner.com/androidcentral?format=xml").performRequest();
            }
        }).start();
    }

    public List<RssFeed> getFeeds() {
        return feeds;
    }

    public List<RssItem> getItems() {
        return items;
    }


}
