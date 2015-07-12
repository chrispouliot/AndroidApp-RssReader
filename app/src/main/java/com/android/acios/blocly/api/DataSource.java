package com.android.acios.blocly.api;

import com.android.acios.blocly.api.model.RssFeed;
import com.android.acios.blocly.api.model.RssItem;
import com.android.acios.blocly.api.network.GetFeedsNetworkRequest;

import java.util.ArrayList;
import java.util.List;

public class DataSource {

    private List<RssFeed> feeds;
    private List<RssItem> items;

    public DataSource() {
        feeds = new ArrayList<>();
        items = new ArrayList<>();
        // I assume I do List<FeedResponse> = new GetFeedsNetworkRequest(...)
        // then just set feeds / items to the private ones
        new Thread(new Runnable() {
            @Override
            public void run() {
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
