package com.android.acios.blocly.api;

import android.database.Cursor;
import android.os.Handler;

import com.android.acios.blocly.BloclyApplication;
import com.android.acios.blocly.BuildConfig;
import com.android.acios.blocly.api.model.RssFeed;
import com.android.acios.blocly.api.model.RssItem;
import com.android.acios.blocly.api.model.database.DatabaseOpenHelper;
import com.android.acios.blocly.api.model.database.table.RssFeedTable;
import com.android.acios.blocly.api.model.database.table.RssItemTable;
import com.android.acios.blocly.api.model.database.table.Table;
import com.android.acios.blocly.api.network.GetFeedsNetworkRequest;
import com.android.acios.blocly.api.network.NetworkRequest;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataSource {

    public static interface Callback<Result> {
        public void onSuccess(Result result);
        public void onError(String errorMessage);
    }

    private DatabaseOpenHelper databaseOpenHelper;
    private RssFeedTable rssFeedTable;
    private RssItemTable rssItemTable;
    private ExecutorService executorService;

    public DataSource() {

        rssFeedTable = new RssFeedTable();
        rssItemTable = new RssItemTable();
        executorService = Executors.newSingleThreadExecutor();
        databaseOpenHelper = new DatabaseOpenHelper(BloclyApplication.getSharedInstance(), rssFeedTable, rssItemTable);

        if (BuildConfig.DEBUG && true) {
            BloclyApplication.getSharedInstance().deleteDatabase("blocly_db");
        }
    }

    public void fetchNewFeed(final String feedURL, final Callback<RssFeed> callback){
        final Handler callbackThreadHandler = new Handler();
        submitTask(new Runnable() {
            @Override
            public void run() {

                Cursor existingFeedCursor = RssFeedTable.fetchFeedWithURL(databaseOpenHelper.getReadableDatabase(), feedURL);
                if (existingFeedCursor.moveToFirst()) {
                    final RssFeed fetchedFeed = feedFromCursor(existingFeedCursor);
                    existingFeedCursor.close();

                    callbackThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(fetchedFeed);
                        }
                    });
                    return;
                }

                GetFeedsNetworkRequest getFeedsNetworkRequest = new GetFeedsNetworkRequest(feedURL);
                List<GetFeedsNetworkRequest.FeedResponse> feedResponses = getFeedsNetworkRequest.performRequest();

                if (getFeedsNetworkRequest.getErrorCode() != 0) {
                    final String errorMessage;
                    if (getFeedsNetworkRequest.getErrorCode() == NetworkRequest.ERROR_IO) {
                        errorMessage = "Network Error";
                    } else if (getFeedsNetworkRequest.getErrorCode() == NetworkRequest.ERROR_MALFORMED_URL) {
                        errorMessage = "Malformed URL error";
                    } else if (getFeedsNetworkRequest.getErrorCode() == GetFeedsNetworkRequest.ERROR_PARSING) {
                        errorMessage = "Error parsing feed";
                    } else {
                        errorMessage = "Unknown error";
                    }

                    callbackThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(errorMessage);
                        }
                    });
                    return;
                }
                GetFeedsNetworkRequest.FeedResponse newFeedResponse = feedResponses.get(0);
                long newFeedId = new RssFeedTable.Builder()
                        .setFeedURL(newFeedResponse.channelFeedURL)
                        .setSiteURL(newFeedResponse.channelURL)
                        .setTitle(newFeedResponse.channelTitle)
                        .setDescription(newFeedResponse.channelDescription)
                        .insert(databaseOpenHelper.getWritableDatabase());



                for (GetFeedsNetworkRequest.ItemResponse itemResponse : newFeedResponse.channelItems) {
                    long itemPubDate = System.currentTimeMillis();
                    DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyy kk:mm:ss z", Locale.ENGLISH);
                    try {
                        itemPubDate = dateFormat.parse(itemResponse.itemPubDate).getTime();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    new RssItemTable.Builder()
                            .setTitle(itemResponse.itemTitle)
                            .setDescription(itemResponse.itemDescription)
                            .setEnclosure(itemResponse.itemEnclosureURL)
                            .setMIMEType(itemResponse.itemEnclosureMIMEType)
                            .setLink(itemResponse.itemURL)
                            .setGUID(itemResponse.itemGUID)
                            .setPubDate(itemPubDate)
                            .setRSSFeed(newFeedId)
                            .insert(databaseOpenHelper.getWritableDatabase());

                    Cursor newFeedCursor = rssFeedTable.fetchRow(databaseOpenHelper.getReadableDatabase(), newFeedId);
                    newFeedCursor.moveToFirst();
                    final RssFeed fetchedFeed = feedFromCursor(newFeedCursor);
                    newFeedCursor.close();
                    callbackThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(fetchedFeed);
                        }
                    });
                }
            }
        });
    }

    public void fetchItemsForFeed(final RssFeed rssFeed, final Callback<List<RssItem>> callback) {
        final Handler callbackThreadHandler = new Handler();
        submitTask(new Runnable() {
            @Override
            public void run() {
                final List<RssItem> resultList = new ArrayList<>();
                Cursor cursor = RssItemTable.fetchItemsForFeed(
                        databaseOpenHelper.getReadableDatabase(),
                        rssFeed.getRowId());
                if (cursor.moveToFirst()) {
                    do {
                        resultList.add(itemFromCursor(cursor));
                    } while (cursor.moveToNext());
                    cursor.close();
                }
                callbackThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess(resultList);
                    }
                });
            }
        });
    }


    static RssFeed feedFromCursor(Cursor cursor) {
        return new RssFeed(Table.getRowId(cursor), RssFeedTable.getTitle(cursor), RssFeedTable.getDescription(cursor),
                RssFeedTable.getSiteURL(cursor), RssFeedTable.getFeedURL(cursor));
    }

    static RssItem itemFromCursor(Cursor cursor) {
        return new RssItem(Table.getRowId(cursor), RssItemTable.getGUID(cursor), RssItemTable.getTitle(cursor),
                RssItemTable.getDescription(cursor), RssItemTable.getLink(cursor),
                RssItemTable.getEnclosure(cursor), RssItemTable.getRssFeedId(cursor),
                RssItemTable.getPubDate(cursor), false, RssItemTable.getFavorite(cursor),
                RssItemTable.getArchived(cursor));
    }

    void submitTask(Runnable task) {
        if (executorService.isShutdown() || executorService.isTerminated()) {
            executorService = Executors.newSingleThreadExecutor();
        }
        executorService.submit(task);
    }


}
