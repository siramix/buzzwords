/*
 * Button Clicker
 * Sample Implementation of the In-App Purchasing APIs
 * © 2012, Amazon.com, Inc. or its affiliates.
 * All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 * http://aws.amazon.com/apache2.0/
 * or in the "license" file accompanying this file.
 * This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package com.buzzwords;

import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.amazon.inapp.purchasing.BasePurchasingObserver;
import com.amazon.inapp.purchasing.GetUserIdResponse;
import com.amazon.inapp.purchasing.GetUserIdResponse.GetUserIdRequestStatus;
import com.amazon.inapp.purchasing.Item;
import com.amazon.inapp.purchasing.ItemDataResponse;
import com.amazon.inapp.purchasing.Offset;
import com.amazon.inapp.purchasing.PurchaseResponse;
import com.amazon.inapp.purchasing.PurchaseUpdatesResponse;
import com.amazon.inapp.purchasing.PurchasingManager;
import com.amazon.inapp.purchasing.Receipt;

/**
 * Purchasing Observer will be called on by the Purchasing Manager asynchronously.
 * Since the methods on the UI thread of the application, all fulfillment logic is done via an AsyncTask. This way, any
 * intensive processes will not hang the UI thread and cause the application to become
 * unresponsive.
 */
public class PackPurchaseObserver extends BasePurchasingObserver {
    
    private static final String OFFSET = "offset";
    private static final String TAG = "Amazon-IAP";
    private final PackPurchaseActivity baseActivity;

    /**
     * Creates new instance of the PackPurchaseActivity class.
     * 
     * @param PackPurchaseActivity Activity context
     */
    public PackPurchaseObserver(final PackPurchaseActivity packPurchaseActivity) {
        super(packPurchaseActivity);
        this.baseActivity = packPurchaseActivity;
    }

    /**
     * Invoked once the observer is registered with the Puchasing Manager If the boolean is false, the application is
     * receiving responses from the SDK Tester. If the boolean is true, the application is live in production.
     * 
     * @param isSandboxMode
     *            Boolean value that shows if the app is live or not.
     */
    @Override
    public void onSdkAvailable(final boolean isSandboxMode) {
        SafeLog.d(TAG, "onSdkAvailable recieved: Response -" + isSandboxMode);
        PurchasingManager.initiateGetUserIdRequest();
    }

    /**
     * Invoked once the call from initiateGetUserIdRequest is completed.
     * On a successful response, a response object is passed which contains the request id, request status, and the
     * userid generated for your application.
     * 
     * @param getUserIdResponse
     *            Response object containing the UserID
     */
    @Override
    public void onGetUserIdResponse(final GetUserIdResponse getUserIdResponse) {
        SafeLog.d(TAG, "onGetUserIdResponse recieved: Response -" + getUserIdResponse);
        SafeLog.d(TAG, "RequestId:" + getUserIdResponse.getRequestId());
        SafeLog.d(TAG, "IdRequestStatus:" + getUserIdResponse.getUserIdRequestStatus());
        new GetUserIdAsyncTask().execute(getUserIdResponse);
    }

    /**
     * Invoked once the call from initiateItemDataRequest is completed.
     * On a successful response, a response object is passed which contains the request id, request status, and a set of
     * item data for the requested skus. Items that have been suppressed or are unavailable will be returned in a
     * set of unavailable skus.
     * 
     * @param itemDataResponse
     *            Response object containing a set of purchasable/non-purchasable items
     */
    @Override
    public void onItemDataResponse(final ItemDataResponse itemDataResponse) {
        SafeLog.d(TAG, "onItemDataResponse recieved");
        SafeLog.d(TAG, "ItemDataRequestStatus" + itemDataResponse.getItemDataRequestStatus());
        SafeLog.d(TAG, "ItemDataRequestId" + itemDataResponse.getRequestId());
        new ItemDataAsyncTask().execute(itemDataResponse);
    }

    /**
     * Is invoked once the call from initiatePurchaseRequest is completed.
     * On a successful response, a response object is passed which contains the request id, request status, and the
     * receipt of the purchase.
     * 
     * @param purchaseResponse
     *            Response object containing a receipt of a purchase
     */
    @Override
    public void onPurchaseResponse(final PurchaseResponse purchaseResponse) {
        SafeLog.d(TAG, "onPurchaseResponse recieved");
        SafeLog.d(TAG, "PurchaseRequestStatus:" + purchaseResponse.getPurchaseRequestStatus());
        new PurchaseAsyncTask().execute(purchaseResponse);
    }

    /**
     * Is invoked once the call from initiatePurchaseUpdatesRequest is completed.
     * On a successful response, a response object is passed which contains the request id, request status, a set of
     * previously purchased receipts, a set of revoked skus, and the next offset if applicable. If a user downloads your
     * application to another device, this call is used to sync up this device with all the user's purchases.
     * 
     * @param purchaseUpdatesResponse
     *            Response object containing the user's recent purchases.
     */
    @Override
    public void onPurchaseUpdatesResponse(final PurchaseUpdatesResponse purchaseUpdatesResponse) {
        SafeLog.d(TAG, "onPurchaseUpdatesRecived recieved: Response -" + purchaseUpdatesResponse);
        SafeLog.d(TAG, "PurchaseUpdatesRequestStatus:" + purchaseUpdatesResponse.getPurchaseUpdatesRequestStatus());
        SafeLog.d(TAG, "RequestID:" + purchaseUpdatesResponse.getRequestId());
        new PurchaseUpdatesAsyncTask().execute(purchaseUpdatesResponse);
    }

    /*
     * Helper method to print out relevant receipt information to the SafeLog.d
     */
    private void printReceipt(final Receipt receipt) {
        SafeLog.d(
            TAG,
            String.format("Receipt: ItemType: %s Sku: %s SubscriptionPeriod: %s", receipt.getItemType(),
                receipt.getSku(), receipt.getSubscriptionPeriod()));
    }

    /*
     * Helper method to retrieve the correct key to use with our shared preferences
     */
    private String getKey(final String sku) {
        // To avoid hard-coding our list of entitlements like the example Amazon app,
        // we will just use the sku as our key
        return sku;
    }

    private SharedPreferences getSharedPreferencesForCurrentUser() {
        final SharedPreferences settings = baseActivity.getSharedPreferences(baseActivity.getCurrentUser(), Context.MODE_PRIVATE);
        return settings;
    }
    
    private SharedPreferences getSyncPreferences() {
        final SharedPreferences syncPrefs = baseActivity.getSyncPreferences();
        return syncPrefs;
    }
    
    private SharedPreferences.Editor getSharedPreferencesEditor(){
        return getSharedPreferencesForCurrentUser().edit();
    }

    /*
     * Started when the Observer receives a GetUserIdResponse. The Shared Preferences file for the returned user id is
     * accessed.
     */
    private class GetUserIdAsyncTask extends AsyncTask<GetUserIdResponse, Void, Boolean> {

        @Override
        protected Boolean doInBackground(final GetUserIdResponse... params) {
            GetUserIdResponse getUserIdResponse = params[0];

            if (getUserIdResponse.getUserIdRequestStatus() == GetUserIdRequestStatus.SUCCESSFUL) {
                final String userId = getUserIdResponse.getUserId();

                // Each UserID has their own shared preferences file, and we'll load that file when a new user logs in.
                baseActivity.setCurrentUser(userId);
                return true;
            } else {
                SafeLog.d(TAG, "onGetUserIdResponse: Unable to get user ID.");
                return false;
            }
        }

        /*
         * Call initiatePurchaseUpdatesRequest for the returned user to sync purchases that are not yet fulfilled.
         */
        @Override
        protected void onPostExecute(final Boolean result) {
            super.onPostExecute(result);
            if (result) {
                PurchasingManager.initiatePurchaseUpdatesRequest(Offset.fromString(baseActivity.getApplicationContext()
                    .getSharedPreferences(baseActivity.getCurrentUser(), Context.MODE_PRIVATE)
                    .getString(OFFSET, Offset.BEGINNING.toString())));
            }
        }
    }

    /*
     * Started when the observer receives an Item Data Response.
     * Takes the items and display them in the logs. You can use this information to display an in game
     * storefront for your IAP items.
     */
    private class ItemDataAsyncTask extends AsyncTask<ItemDataResponse, Void, Void> {
        @Override
        protected Void doInBackground(final ItemDataResponse... params) {
            final ItemDataResponse itemDataResponse = params[0];

            switch (itemDataResponse.getItemDataRequestStatus()) {
            case SUCCESSFUL_WITH_UNAVAILABLE_SKUS:
                // Skus that you can not purchase will be here.
                for (final String s : itemDataResponse.getUnavailableSkus()) {
                    SafeLog.d(TAG, "Unavailable SKU:" + s);
                }
            case SUCCESSFUL:
                // Information you'll want to display about your IAP items is here
                // In this example we'll simply log them.
                final Map<String, Item> items = itemDataResponse.getItemData();
                for (final String key : items.keySet()) {
                    Item i = items.get(key);
                    SafeLog.d(TAG, String.format("Item: %s\n Type: %s\n SKU: %s\n Price: %s\n Description: %s\n", 
                        i.getTitle(), i.getItemType(), i.getSku(), i.getPrice(), i.getDescription()));
                }
                break;
            case FAILED:
                // On failed responses will fail gracefully.
                break;
            }
            
            return null;
        }
    }

    /*
     * Started when the observer receives a Purchase Response
     * Once the AsyncTask returns successfully, the UI is updated.
     */
    private class PurchaseAsyncTask extends AsyncTask<PurchaseResponse, Void, Boolean> {

        @Override
        protected Boolean doInBackground(final PurchaseResponse... params) {
            final PurchaseResponse purchaseResponse = params[0];            
            final String userId = baseActivity.getCurrentUser();
            
            if (!purchaseResponse.getUserId().equals(userId)) {
                // currently logged in user is different than what we have so update the state
                baseActivity.setCurrentUser(purchaseResponse.getUserId());                
                PurchasingManager.initiatePurchaseUpdatesRequest(Offset.fromString(baseActivity.getSharedPreferences(baseActivity.getCurrentUser(), Context.MODE_PRIVATE)
                                                                                   .getString(OFFSET, Offset.BEGINNING.toString())));                
            }
            final SharedPreferences.Editor userPrefEditor = getSharedPreferencesEditor();
            // The requires sync preference must be set globally (across users) so switching users triggers a sync
            final SharedPreferences.Editor syncPrefEditor = getSyncPreferences().edit();
            switch (purchaseResponse.getPurchaseRequestStatus()) {
            case SUCCESSFUL:
                /*
                 * You can verify the receipt and fulfill the purchase on successful responses.
                 */
                final Receipt receipt = purchaseResponse.getReceipt();
                String key = "";
                switch (receipt.getItemType()) {
                case CONSUMABLE:
                    //Nothing to do since Buzzwords doesn't use consumables
                    break;
                case ENTITLED:
                    key = getKey(receipt.getSku());
                    userPrefEditor.putBoolean(key, true);
                    syncPrefEditor.putBoolean(Consts.PREFKEY_SYNC_REQUIRED, true);
                    break;
                case SUBSCRIPTION:
                    //Nothing to do since Buzzwords doesn't use subscriptions
                    break;
                }
                userPrefEditor.commit();
                syncPrefEditor.commit();
                
                printReceipt(purchaseResponse.getReceipt());
                return true;
            case ALREADY_ENTITLED:
                /*
                 * If the customer has already been entitled to the item, a receipt is not returned.
                 * Fulfillment is done unconditionally, we determine which item should be fulfilled by matching the
                 * request id returned from the initial request with the request id stored in the response.
                 */
                final String requestId = purchaseResponse.getRequestId();
                userPrefEditor.putBoolean(baseActivity.requestIds.get(requestId), true);
                syncPrefEditor.putBoolean(Consts.PREFKEY_SYNC_REQUIRED, true);
                userPrefEditor.commit();
                syncPrefEditor.commit();
                return true;
            case FAILED:
                /*
                 * If the purchase failed for some reason, (The customer canceled the order, or some other
                 * extraneous circumstance happens) the application ignores the request and logs the failure.
                 */
                SafeLog.d(TAG, "Failed purchase for request" + baseActivity.requestIds.get(purchaseResponse.getRequestId()));
                return false;
            case INVALID_SKU:
                /*
                 * If the sku that was purchased was invalid, the application ignores the request and logs the failure.
                 * This can happen when there is a sku mismatch between what is sent from the application and what
                 * currently exists on the dev portal.
                 */
                SafeLog.d(TAG, "Invalid Sku for request " + baseActivity.requestIds.get(purchaseResponse.getRequestId()));
                return false;
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            super.onPostExecute(success);
            if (success) {
                baseActivity.refreshAllPackLayouts();
            }
        }
    }

    /*
     * Started when the observer receives a Purchase Updates Response Once the AsyncTask returns successfully, we'll
     * update the UI.
     */
    private class PurchaseUpdatesAsyncTask extends AsyncTask<PurchaseUpdatesResponse, Void, Boolean> {

        @Override
        protected Boolean doInBackground(final PurchaseUpdatesResponse... params) {
            final PurchaseUpdatesResponse purchaseUpdatesResponse = params[0];
            final SharedPreferences.Editor userPrefEditor = getSharedPreferencesEditor();
            // The requires sync preference must be set globally (across users) so switching users triggers a sync
            final SharedPreferences.Editor syncPrefEditor = getSyncPreferences().edit();
            final String userId = baseActivity.getCurrentUser();
            if (!purchaseUpdatesResponse.getUserId().equals(userId)) {
                return false;
            }
            /*
             * If the customer for some reason had items revoked, the skus for these items will be contained in the
             * revoked skus set.
             */
            for (final String sku : purchaseUpdatesResponse.getRevokedSkus()) {
                SafeLog.d(TAG, "Revoked Sku:" + sku);
                final String key = getKey(sku);
                userPrefEditor.putBoolean(key, false);
                syncPrefEditor.putBoolean(Consts.PREFKEY_SYNC_REQUIRED, true);
                userPrefEditor.commit();
                syncPrefEditor.commit();
            }

            
            switch (purchaseUpdatesResponse.getPurchaseUpdatesRequestStatus()) {
            case SUCCESSFUL:
                for (final Receipt receipt : purchaseUpdatesResponse.getReceipts()) {
                    final String sku = receipt.getSku();
                    final String key = getKey(sku);
                    switch (receipt.getItemType()) {
                    case ENTITLED:
                        /*
                         * If the receipt is for an entitlement, the customer is re-entitled.
                         */
                        userPrefEditor.putBoolean(key, true);
                        syncPrefEditor.putBoolean(Consts.PREFKEY_SYNC_REQUIRED, true);
                        userPrefEditor.commit();
                        syncPrefEditor.commit();
                        break;
                    default:
                      // Buzzwords does not use Subscription, or Consumable items.
                      break;
                    }
                    printReceipt(receipt);
                }
                /*
                 * Don't bother checking the latest subscription periods once all receipts have been read since
                 * we don't implement subscriptions.

                /*
                 * Store the offset into shared preferences. If there has been more purchases since the
                 * last time our application updated, another initiatePurchaseUpdatesRequest is called with the new
                 * offset.
                 */
                final Offset newOffset = purchaseUpdatesResponse.getOffset();
                userPrefEditor.putString(OFFSET, newOffset.toString());
                userPrefEditor.commit();
                if (purchaseUpdatesResponse.isMore()) {
                    SafeLog.d(TAG, "Initiating Another Purchase Updates with offset: " + newOffset.toString());
                    PurchasingManager.initiatePurchaseUpdatesRequest(newOffset);
                }
                return true;
            case FAILED:
                /*
                 * On failed responses the application will ignore the request.
                 */
                return false;
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            super.onPostExecute(success);
            if (success) {
              baseActivity.syncronizePacks();
            }
        }
    }
}
