package com.saulhdev.feeder.data.entity;

/**
* Main feed service [callback]
*/
import com.saulhdev.feeder.data.entity.FeedItem;
import com.saulhdev.feeder.data.entity.FeedCategory;

interface IFeedInterfaceCallback {
    // When service receives feed data
    void onFeedReceive(in List<FeedItem> feed);

    void onCategoriesReceive(in List<FeedCategory> categories);
}