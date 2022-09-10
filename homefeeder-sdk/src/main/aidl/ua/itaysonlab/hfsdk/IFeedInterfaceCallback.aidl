package ua.itaysonlab.hfsdk;

/**
* Main feed service [callback]
*/
import ua.itaysonlab.hfsdk.FeedItem;
import ua.itaysonlab.hfsdk.FeedCategory;

interface IFeedInterfaceCallback {
    // When service receives feed data
    void onFeedReceive(in List<FeedItem> feed);

    void onCategoriesReceive(in List<FeedCategory> categories);
}