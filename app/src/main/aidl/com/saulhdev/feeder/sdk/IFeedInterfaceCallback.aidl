package com.saulhdev.feeder.sdk;

/**
* Main feed service [callback]
*/
import com.saulhdev.feeder.sdk.FeedItem;
import com.saulhdev.feeder.sdk.FeedCategory;

interface IFeedInterfaceCallback {
    // When service receives feed data
    void onFeedReceive(in List<FeedItem> feed);

    void onCategoriesReceive(in List<FeedCategory> categories);
}