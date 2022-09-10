package ua.itaysonlab.hfsdk;

/**
* Main feed service
*/
import ua.itaysonlab.hfsdk.FeedItem;
import ua.itaysonlab.hfsdk.IFeedInterfaceCallback;

interface IFeedInterface {
    // Get main feed by page with specific parameters (can be null)
    // Page contains variable item count
    // default category - "default", this can be unified feed or other...
    void getFeed(in IFeedInterfaceCallback callback, in int page, in String category_id, in Bundle parameters);

    // Get feed categories
    void getCategories(in IFeedInterfaceCallback callback);
}