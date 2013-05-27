package com.buzzwords;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;

import com.amazon.inapp.purchasing.PurchasingManager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class PackPurchaseActivity extends Activity {

  private static final String TAG = "PackPurchase";
  
  // To be used for tooltips to help guide users
  private Toast mHelpToast = null;

  /**
   * flag used for stopping music OnStop() event.
   */
  private boolean mContinueMusic;
  
  /**
   * Flag to prevent other activities from opening after one is launched
   */
  private boolean mIsActivityClosing;
  
  LinkedList<View> mPackLineList;
  
  // Our local packs.
  private LinkedList<Pack> mUnlockedPacks;
  
  // Our pack lists as retrieved from the server
  private LinkedList<Pack> mServerPacks;
  
  private GameManager mGameManager;

  // Mapping of our requestIds to unlockable content (For Amazon IAP)
  public Map<String, String> requestIds;
  
  // Flag for whether user can connect to our site, set on each refresh.
  private boolean mServerError = false;
  
  /**
   * Request Code constants for social media sharing
   */
    private static final int FACEBOOK_REQUEST_CODE = 12;
    private static final int PACKINFO_REQUEST_CODE = 14;

    
  /**
   * PlayGameListener plays an animation on the view that will result in
   * launching GameSetup
   */
  private OnClickListener mGameSetupListener = new OnClickListener() {
    public void onClick(View v) {
      // Throw out any queued onClicks.
      if(!v.isEnabled()){
        return;
      }

      // Carry music into GameSetup
      mContinueMusic = true;
      
      // play confirm sound
      SoundManager sm = SoundManager.getInstance(PackPurchaseActivity.this
          .getBaseContext());
      sm.playSound(SoundManager.Sound.CONFIRM);

      SharedPreferences packSelectionPrefs = getSharedPreferences(Consts.PREFFILE_PACK_SELECTIONS,
          Context.MODE_PRIVATE);

      Set<String> keySet = packSelectionPrefs.getAll().keySet();

      // Only advance to next screen if a pack is selected
      boolean anyPackSelected = false;
      for (String packId : keySet) {
        if (packSelectionPrefs.getBoolean(packId, true) == true) {
          anyPackSelected = true;
        }
      }

      if (anyPackSelected == true) {
        // Only disable this view when we are definitely advancing
        v.setEnabled(false);
        mIsActivityClosing = true;
        
        startActivity(new Intent(getApplication().getString(R.string.IntentGameSetup),
              getIntent().getData()));
      } else {
        showToast(getString(R.string.toast_packpurchase_nopackselected));
      }
    }
  };

  /**
   * Create the packages screen from an XML layout and
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Initialize our packs
    mServerPacks = new LinkedList<Pack>();

    mGameManager= new GameManager(PackPurchaseActivity.this);

    requestIds = new HashMap<String, String>();
    
    final SharedPreferences.Editor syncPrefEditor = getSyncPreferences().edit();
    syncPrefEditor.putBoolean(Consts.PREFKEY_SYNC_IN_PROGRESS, false);
    syncPrefEditor.commit();
    
    // Force volume controls to affect Media volume
    setVolumeControlStream(AudioManager.STREAM_MUSIC);

    // Setup the view
    this.setContentView(R.layout.packpurchase);
    
    // Set next button listener
    Button btn = (Button) this.findViewById(R.id.PackPurchase_Button_Next);
    btn.setOnClickListener(mGameSetupListener);
  }

  /**
   * Lazy loads the packLineList which is used for populating
   * the pack layout.
   * @return mPackLineList
   */
  private LinkedList<View> getPackLineList() {
    if (mPackLineList == null) {
      mPackLineList = new LinkedList<View>();
    }
    return mPackLineList;
  }

  
  /**
   * Whenever the application regains focus, the observer is registered again.
   */
  @Override
  public void onStart() {
      super.onStart();
      PackPurchaseObserver packPurchaseObserver = new PackPurchaseObserver(this);
      PurchasingManager.registerObserver(packPurchaseObserver);
  }

  /**
   * When the application resumes the application checks which customer is signed in.
   */
  @Override
  public void onResume() {
    super.onResume();
    // Asynchronous call to check our current user. Flags for re-sync if needed. 
    PurchasingManager.initiateGetUserIdRequest();
    refreshAllPackLayouts();
    
    // Re-enable buttons that were disabled to prevent double click.
    Button btn = (Button) this.findViewById(R.id.PackPurchase_Button_Next);
    btn.setEnabled(true);
    
    mIsActivityClosing = false;
    
    // Resume Title Music
    BuzzWordsApplication application = (BuzzWordsApplication) this
        .getApplication();
    MediaPlayer mp = application.getMusicPlayer(application.getBaseContext());
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this
        .getBaseContext());
    if (sp.getBoolean(Consts.PREFKEY_MUSIC, true)) {
      if (!mp.isPlaying()) {
        mp.start();
      }
    }
    // set flag to let onStop handle music
    mContinueMusic = false;
  }

  /**
   * Update the list of packs to include correct purchased and unpurchased lists.
   * First populates purchased then goes online to get list of unpurchased packs.
   */
  protected void refreshAllPackLayouts() {
    mServerError = !isNetworkAvailable();
    // If they don't have internet, dump cached serve packs. 
    if (mServerError) {
      mServerPacks.clear();
    }
    mUnlockedPacks = mGameManager.getInstalledPacks(this.getBaseContext());
    
    displayUnlockedPacks();
    refreshLockedPacksLayout();

    // Update the appropriate card count views
    updateCardCountViews();
  }
  
  /**
   * Helper method that displays just the top list of packs.
   */
  private void displayUnlockedPacks() {
    // Populate and display list of cards
    LinearLayout unlockedPackLayout = (LinearLayout) findViewById(R.id.PackPurchase_UnlockedPackSets);
    unlockedPackLayout.removeAllViews();
    populatePackLayout(mUnlockedPacks, unlockedPackLayout);
  }
  
  /**
   * Helper method that connects to the server and displays any packs the user
   * has not already purchased.  Must be called after displayUnlockedPacks to 
   * show a list that filters out packs user already owns.
   */
  private void refreshLockedPacksLayout() {
    TextView placeHolderText = (TextView) findViewById(R.id.PackPurchase_PaidPackPlaceholderText);
    placeHolderText.setVisibility(View.GONE);
    ProgressBar progressImage = (ProgressBar) findViewById(R.id.PackPurchase_PaidPackPlaceholderImage);
    progressImage.setVisibility(View.VISIBLE);
    
    LinearLayout lockedPackLayout = (LinearLayout) findViewById(R.id.PackPurchase_PaidPackSets);
    lockedPackLayout.removeAllViews();
    
    if (!mServerError && mServerPacks.isEmpty()) { 
      // Don't reload the server packs or refresh the list if packs are already in memory.
      fetchPurchasablePacksOnThread();
    } else {
      // Display the cached server packs
      displayLockedPackServerResults();
    }
  }
  
  /**
   * Retrieve the pack list within a thread.  
   */
  public void fetchPurchasablePacksOnThread() {
    final PackClient client = PackClient.getInstance();
    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
          displayLockedPackServerResults();
        }
    };
  
    Thread thread = new Thread() {
        @Override
        public void run() {
          try {
            mServerPacks = client.getServerPacks();
          } catch (IOException e1) {
            Log.e(TAG, "Error occurred during I/O of serverPacks.");
            mServerError = true;
            Log.e(TAG, e1.toString());
            e1.printStackTrace();
          } catch (URISyntaxException e1) {
            mServerError = true;
            Log.e(TAG, e1.toString());
            e1.printStackTrace();
          } catch (JSONException e1) {
            Log.e(TAG, "Error parsing pack JSON from server.");
            mServerError = true;
            Log.e(TAG, e1.toString());
            e1.printStackTrace();
          }
          Message message = handler.obtainMessage(1, mServerPacks);
          handler.sendMessage(message);
        }
    };
    
    thread.start();
  }

  /**
   * Remove from lockedPacks those packs that are already installed.
   * @param lockedPacks all locked packs
   * @param localPacks packs to exclude from list
   * @return list of unpurchased packs
   */
  private LinkedList<Pack> getUnownedPacks(LinkedList<Pack> lockedPacks, LinkedList<Pack> installedPacks) {
    LinkedList<Pack> unownedPackList = new LinkedList<Pack>();
    unownedPackList.addAll(lockedPacks);

    // Iterate through installed packs removing them from the locked packs
    for (Pack localPack : installedPacks) {
      for (int unownedIndex = 0; unownedIndex < unownedPackList.size(); ++unownedIndex) {
        if (localPack.getId() == unownedPackList.get(unownedIndex).getId()) {
          unownedPackList.remove(unownedIndex);
        }
      }
    }
    
    return unownedPackList;
  }
  
  /**
   * Populate the list of purchasable packs. Handle special cases like removing
   * the loading bar and changing or removing the placeholder text. Assumes layout
   * has been emptied.
   */
  private void displayLockedPackServerResults() {
    // Hide the network progress icon
    ProgressBar progressImage = (ProgressBar) findViewById(R.id.PackPurchase_PaidPackPlaceholderImage);
    progressImage.setVisibility(View.GONE);
    
    LinkedList<Pack> lockedPacks = new LinkedList<Pack>();
    LinearLayout paidPackLayout = (LinearLayout) findViewById(R.id.PackPurchase_PaidPackSets);
    TextView placeHolderText = (TextView) findViewById(R.id.PackPurchase_PaidPackPlaceholderText);
    
    lockedPacks = getUnownedPacks(mServerPacks, mUnlockedPacks);
    if (mServerError) {
      placeHolderText.setVisibility(View.VISIBLE);
      placeHolderText.setText(getString(R.string.packpurchase_nointernet));
    } else if (lockedPacks.size() == 0) {
      placeHolderText.setVisibility(View.VISIBLE);
      placeHolderText.setText(getString(R.string.packpurchase_allpurchased));
    } else {
      populatePackLayout(lockedPacks, paidPackLayout);
    }
  }
  
  /**
   * Create dynamic rows of packs at runtime for pack purchase view. This will
   * set up the XML, bind listeners, and update pack titles, price, and images
   * according to the information passed in. Retrieving the info to populate
   * should be the responsibility of a different method.
   * 
   * @param packlist
   *          A list of packs to iterate through and populate the purchase rows
   *          with
   * @param insertionPoint
   *          The linearlayout at which to insert the rows of packs
   */
  private void populatePackLayout(List<Pack> packlist, LinearLayout insertionPoint) {
    int count = 0;

    // Instantiate all our views for programmatic layout creation
    LinearLayout layout = new LinearLayout(this.getBaseContext());
    layout.setOrientation(LinearLayout.VERTICAL);

    for (Pack curPack : packlist) {

      // Create a new row for this pack
      LinearLayout line = (LinearLayout) LinearLayout.inflate(
          this.getBaseContext(), R.layout.packpurchaserow, layout);
      PackPurchaseRowLayout row = (PackPurchaseRowLayout) line
          .getChildAt(count);
      
      // Assign the pack to the row. This should maybe be done in
      // a constructor
      row.setPack(curPack, getPackSelectedPref(curPack), count % 2 == 0);

      // Add pack rows to the list. Give margin so borders don't double up.
      LinearLayout.LayoutParams margin = (LinearLayout.LayoutParams) row
          .getLayoutParams();
      final float DENSITY = this.getResources().getDisplayMetrics().density;
      if (count > 0) {
        margin.setMargins(0, (int) (-2 * DENSITY), 0, 0);
      }
      row.setLayoutParams(margin);

      // Instantiate all of our lists for programmatic adding of packs to view
      LinkedList<View> lineListView = getPackLineList();
      lineListView.add(row);

      // Set listeners for the row's click events
      row.setOnPackSelectedListener(mSelectPackListener);
      row.setOnPackInfoRequestedListener(mPackInfoListener);
      
      // Add the current pack object to the row so that the listener can get its
      // metadata
      row.setTag(curPack);
      
      count++;
    }
    insertionPoint.addView(layout);
  }

  /** 
   * Compare the pack preferences against installation status for each pack
   * and install or uninstall as necessary.  This is called remotely by PackPurchaseObserver.
   */
  protected void syncronizePacks() {
    Log.d(TAG, "SYNCRONIZING PACKS...");
    boolean unsyncedPurchaseChange = 
                      getSyncPreferences().getBoolean(Consts.PREFKEY_UNSYNCED_PURCHASE_CHANGE, true);
    boolean syncInProgress = getSyncPreferences().getBoolean(Consts.PREFKEY_SYNC_IN_PROGRESS, false);
    boolean updateRequired = mGameManager.packsRequireUpdate(mServerPacks, this.getBaseContext());

    // If outstanding purchases or a pack is out of date
    Boolean syncRequired = (unsyncedPurchaseChange || updateRequired);

    Log.d(TAG, "   SYNC_REQUIRED: " + syncRequired);
    Log.d(TAG, "   UPDATE REQUIRED: " + updateRequired);
    Log.d(TAG, "   SYNC IN PROGRESS: " + syncInProgress);
    
    // Don't call synchronize unless PREFKEY_UNSYNCED_PURCHASE_CHANGE preference is true or
    // some packs are out of date and we have successfully retrieved packs from our server.
    // Also avoid race conditions where synchronizePacks is called by PackPurchaseObserver
    // multiple times by checking for syncing in progress.
    if (syncRequired && !mServerError && !syncInProgress) {
      Pack[] packArray = mServerPacks.toArray(new Pack[mServerPacks.size()]);
      try {
        new PackSyncronizer().execute(packArray);
      }
      catch (RuntimeException e) {
        Log.e(TAG, "Encountered an error syncronizing packs.");
        e.printStackTrace();
      }
    }
  }

  /**
   * Opens the Facebook client for promotional packs
   */
  private void openFacebookClient()
  {
    // Launch Facebook, if not found, launch a browser intent
    String url = Config.buzzwordsFBAppLauncher;
    Intent facebookOrBrowserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
    try {
      startActivityForResult(facebookOrBrowserIntent, FACEBOOK_REQUEST_CODE);
    } catch (ActivityNotFoundException e) {
      Uri uri = Uri.parse(Config.buzzwordsFBPage);
      facebookOrBrowserIntent = new Intent(Intent.ACTION_VIEW);
      facebookOrBrowserIntent.setDataAndType(uri, "text/plain");
      startActivityForResult(facebookOrBrowserIntent, FACEBOOK_REQUEST_CODE);
    }
  }

  /**
   * Listen for the result of a pack info click, purchase click, or social post click.
   */
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    // If the request is coming from our packInfo Activity, handle the request as
    // a purchase, a facebook post, or nothing.
    if(requestCode == PACKINFO_REQUEST_CODE)
    {
      if(resultCode == RESULT_CANCELED)
      {
        // Do nothing
        return;
      }
      
      // Get the pack
      Pack curPack = (Pack) data.getExtras().get(getString(R.string.packBundleKey));
      int resultTypeIndex = curPack.getPurchaseType();
      
      switch(PackPurchaseConsts.PURCHASE_RESULT_CODES[resultTypeIndex])
      {
        case PackPurchaseConsts.RESULT_NOCODE:
          final SharedPreferences settings = getPurchasePrefsForCurrentUser();
          final String sku = String.valueOf(curPack.getId());
          boolean entitled = settings.getBoolean(sku, false);
          
          if (!entitled) {
            String requestId = PurchasingManager.initiatePurchaseRequest(sku);
            storeRequestId(requestId, sku);
          }
          break;
        case PackPurchaseConsts.RESULT_FACEBOOK:
          openFacebookClient();
          break;
      }
    } 

    
    // If the most recent request was to open Facebook and we return to Buzzwords, 
    // then install the Facebook pack
    if (requestCode == FACEBOOK_REQUEST_CODE) {
      setPurchasePrefs(String.valueOf(PackPurchaseConsts.FACEBOOK_PACK_ID), true);
    }

  }

  /**
   * Helper method to associate request ids to shared preference keys
   * 
   * @param requestId
   *            Request ID returned from a Purchasing Manager Request
   * @param key
   *            Key used in shared preferences file
   */
  private void storeRequestId(String requestId, String key) {
      requestIds.put(requestId, key);
  }

  /**
   * In an async task, look through all of our pack prefs for the user and go 
   * through the process of checking purchase status for each pack.  If purchased,
   * check for an update. If it is not purchased, ensure the pack is uninstalled.
   * If it is the starter pack, make sure it is always installed and updated.
   */
  public class PackSyncronizer extends AsyncTask <Pack, Void, Integer>
  {
    private ProgressDialog dialog;
    private long timeStarted = System.currentTimeMillis();
    final SharedPreferences userPurchases = getPurchasePrefsForCurrentUser();
    final SharedPreferences.Editor syncPrefEditor = getSyncPreferences().edit();
    //TODO FIX THIS.  IT CRASHES.
//    final BuzzWordsApplication application = (BuzzWordsApplication) getApplication();
//    final GameManager gm = application.getGameManager();
    final GameManager gm = new GameManager(PackPurchaseActivity.this);
    private boolean installOrUpdateError = false;
    
    @Override
    protected void onPreExecute() {
      dialog = ProgressDialog.show(
          PackPurchaseActivity.this,
          null,
          getString(R.string.progressDialog_update_text), 
          true);
      syncPrefEditor.putBoolean(Consts.PREFKEY_SYNC_IN_PROGRESS, true);
      syncPrefEditor.commit();
    }
    
    @Override
    protected Integer doInBackground(Pack... packs) {
      for (int i=0; i<packs.length; ++i) {
        Log.d(TAG, "SYNCING PACK: " + packs[i].getName());
        boolean isPackPurchased = userPurchases.getBoolean(String.valueOf(packs[i].getId()), false);

        //Install or update the pack if it is purchased.  Select it if it's a new pack.
        if (isPackPurchased) {
          try {
            gm.installLatestPack(packs[i], getBaseContext());
          } catch (RuntimeException e) {
            installOrUpdateError = true;
            Log.e(TAG, "Failed to update or install PURCHASED packId: " + 
                packs[i].getId() + " name: " + packs[i].getName());
            e.printStackTrace();
          }
        } 
        // Uninstall pack if it is not purchased and is not the starter pack
        else if (isPackPurchased == false && 
            packs[i].getPurchaseType() != PackPurchaseConsts.PACKTYPE_STARTER) {
          gm.uninstallPack(packs[i].getId(), getBaseContext());
        }
        // Always check for starter pack update
        else if (packs[i].getPurchaseType() == PackPurchaseConsts.PACKTYPE_STARTER) {
          try {
            gm.installLatestPack(packs[i], getBaseContext());
          } catch (RuntimeException e) {
            installOrUpdateError = true;
            Log.e(TAG, "Failed to update or install STARTER packId: " + 
                packs[i].getId() + " name: " + packs[i].getName());
            e.printStackTrace();
          }
        }
      }
      // Ensure our dialog takes no less than 2.2 seconds
      while(System.currentTimeMillis() - timeStarted < 2200){
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          Log.e(TAG, "thread interrupted", e);
        }
      }
      return 0; 
    }
    
    @Override
    protected void onPostExecute(Integer result)
    {
      refreshAllPackLayouts();
      dialog.dismiss();
      if (installOrUpdateError) {
        showToast(getString(R.string.toast_packpurchase_installfailed));
        syncPrefEditor.putBoolean(Consts.PREFKEY_UNSYNCED_PURCHASE_CHANGE, true);
      } else {
        syncPrefEditor.putBoolean(Consts.PREFKEY_UNSYNCED_PURCHASE_CHANGE, false);
      }
      syncPrefEditor.putBoolean(Consts.PREFKEY_SYNC_IN_PROGRESS, false);
      syncPrefEditor.commit();

      findViewById(R.id.PackPurchase_ScrollView).scrollTo(0, 0);
    }
  }
  
 
  /*
   * Listener for the pack selection, which includes or excludes the pack
   * from the deck.
   */
  private final OnPackSelectedListener mSelectPackListener = new OnPackSelectedListener() {
    @Override
    public void onPackSelected(Pack pack, boolean selectionStatus) {
      // Don't let packs change if the activity is about to close
      if(mIsActivityClosing){
        return;
      }
      
      setPackSelectedPref(pack, selectionStatus);
      updateCardCountViews();

      // play confirm sound when points are added
      SoundManager sm = SoundManager.getInstance(PackPurchaseActivity.this
          .getBaseContext());
      if (selectionStatus) {
        sm.playSound(SoundManager.Sound.CONFIRM);
      } else {
        sm.playSound(SoundManager.Sound.BACK);
      }
    }
  };

  /*
   * Listener that brings up pack info
   */
  private final OnPackInfoRequestedListener mPackInfoListener = new OnPackInfoRequestedListener() {

    @Override
    public void onPackInfoRequested(Pack pack) {
      
      // Don't let this come up if they've already advanced
      if(mIsActivityClosing){
        return;
      }
      
      // play confirm sound when points are added
      SoundManager sm = SoundManager.getInstance(PackPurchaseActivity.this
          .getBaseContext());
      sm.playSound(SoundManager.Sound.CONFIRM);
     
      // Carry music into Pack Info
      mContinueMusic = true;
      
      // Show pack info activity
      showPackInfo(pack); 
    }
  };

  /*
   * Shows the packInfo activity. It takes a row that is to be displayed as
   * a parameter.
   */
  private void showPackInfo(Pack pack)
  {
    boolean selectionStatus = getPackSelectedPref(pack);
    // For now, we don't care if the row background matches
    boolean isPackRowOdd = true; 

    // Show Set Buzzed Team Dialog
    Intent intent = new Intent(getApplication().getString(
        R.string.IntentPackInfo), getIntent().getData());
    // Pass in that the choice is not required
    intent.putExtra(getApplication().getString(R.string.packBundleKey),
        pack);
    intent.putExtra(
        getApplication().getString(R.string.packInfoIsPackSelectedBundleKey),
        selectionStatus);
    intent.putExtra(
        getApplication().getString(R.string.packInfoIsPackRowOddBundleKey),
        isPackRowOdd);
    intent.putExtra(getApplication().getString(R.string.packInfoPurchaseTypeKey),
        pack.getPurchaseType());
    
    startActivityForResult(intent, PACKINFO_REQUEST_CODE);
  }

  
  /**
   * Return the SharedPreferences for selected packs.
   * @return
   */
  public SharedPreferences getPackSelectionPrefs() {
    final SharedPreferences packSelectionPrefs = getSharedPreferences(Consts.PREFFILE_PACK_SELECTIONS,
        Context.MODE_PRIVATE);

    return packSelectionPrefs;
  }

  /**
   * Get the current value of the pack preferences for a given pack name.
   * 
   * @param packName
   * @return
   */
  public boolean getPackSelectedPref(Pack pack) {
    SharedPreferences packSelectionPrefs = getSharedPreferences(Consts.PREFFILE_PACK_SELECTIONS,
        Context.MODE_PRIVATE);

    return packSelectionPrefs.getBoolean(String.valueOf(pack.getId()), false);
  }

  /**
   * Change the pack preference for the passed in pack to either on or off.
   * 
   * @param curPack
   *          the pack whose preference will be changed
   */
  public void setPackSelectedPref(Pack pack, boolean onoff) {
    // Store the pack's boolean in the preferences file for pack preferences
    SharedPreferences.Editor packPrefsEdit = getSharedPreferences(Consts.PREFFILE_PACK_SELECTIONS,
        Context.MODE_PRIVATE).edit();
    packPrefsEdit.putBoolean(String.valueOf(pack.getId()), onoff);
    packPrefsEdit.commit();
  }
  
  /**
   * Updates the card counts and the views that display them.
   */
  private void updateCardCountViews()
  {
    int totalSeen = 0;
    int totalCards = 0;

    for (Pack pack : mUnlockedPacks) {
      // Could show progress on only selected packs, but we are showing all
      totalSeen += pack.getNumCardsSeen();
      totalCards += pack.getSize();
    }
    
    ProgressBarView progress = (ProgressBarView) this.findViewById(R.id.PackPurchase_Progress);
    progress.setProgress(totalSeen, totalCards);

  }
  
  /**
   * Perform a basic internet connection check.
   * @return
   */
  private boolean isNetworkAvailable() {
    ConnectivityManager connectivityManager 
          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
    return activeNetworkInfo != null;
  }

  /**
   * Returns the Component name of either Twitter, Google, or Facebook
   * 
   * @param foundClients
   *          A hashmap of clients that have been identified by Detect Clients
   *          as being on the users phone
   * @return
   */
  public ComponentName getClientComponentName(
      HashMap<String, ActivityInfo> foundClients) {

    ComponentName result = null;

    if (foundClients.size() > 0) {
      ActivityInfo socialActivity = null;
      for (Map.Entry<String, ActivityInfo> entry : foundClients.entrySet()) {
        socialActivity = entry.getValue();
        break;
      }

      result = new ComponentName(socialActivity.applicationInfo.packageName,
          socialActivity.name);
    }

    return result;
  }

  /**
   * Get the Purchase SharedPreferences file for the current user.
   * @return SharedPreferences file for a user's purchases.
   */
  private SharedPreferences getPurchasePrefsForCurrentUser() {
    final SharedPreferences settings = getSharedPreferences(getCurrentUser(), Context.MODE_PRIVATE);
    return settings;
  }
  
  /**
   * Generate a SharedPreferences.Editor object. 
   * @return editor for Shared Preferences file.
   */
  private SharedPreferences.Editor getPurchasePrefsEditor(){
    return getPurchasePrefsForCurrentUser().edit();
  }
  
  /**
   * Get the preferences file that all users will share
   * @return SharedPreferences file for all users.
   */
  protected SharedPreferences getSyncPreferences() {
    final SharedPreferences syncPrefs = getSharedPreferences(Consts.PREFFILE_SYNC_PREFS, Context.MODE_PRIVATE);
    return syncPrefs;
  }
  
  /**
   * Gets current logged in user
   * @return current user
   */
  protected String getCurrentUser(){
    return getSyncPreferences().getString(Consts.PREFKEY_CURRENT_USER, "unset");
  }
  
  /**
   * Get the user who logged in previously to the current one
   * @return
   */
  protected String getPreviousUser() {
    return getSyncPreferences().getString(Consts.PREFKEY_LAST_USER, "unset");
  }
  
  /**
   * Sets current logged in user
   * @param currentUser current user to set
   */
  protected void setCurrentUser(final String currentUser){
    final SharedPreferences.Editor syncPrefEditor = getSyncPreferences().edit();
    syncPrefEditor.putString(Consts.PREFKEY_CURRENT_USER, currentUser);
    syncPrefEditor.commit();
  }
  
  /**
   * Set the user who logged in previously to the current one
   * @param previousUser
   */
  protected void setPreviousUser(final String previousUser) {
    final SharedPreferences.Editor syncPrefEditor = getSyncPreferences().edit();
    syncPrefEditor.putString(Consts.PREFKEY_LAST_USER, previousUser);
    syncPrefEditor.commit();
  }

  /**
   * Ensure all pertinent preferences are updated when a purchase occurs. This should
   * be the ONE AND ONLY WAY that purchase changes are made!
   * @param SKU of pack being purchased or unpurchased
   * @param purchased true for purchased, false otherwise
   */
  protected void setPurchasePrefs(final String SKU, boolean purchased) {
    final SharedPreferences.Editor userPurchases = getPurchasePrefsEditor();
    // The requires sync preference must be set globally (across users) so switching users triggers a sync
    final SharedPreferences.Editor syncPrefEditor = getSyncPreferences().edit();
    userPurchases.putBoolean(SKU, purchased);
    syncPrefEditor.putBoolean(Consts.PREFKEY_UNSYNCED_PURCHASE_CHANGE, true);
    userPurchases.commit();
    syncPrefEditor.commit();
  }

  
  /**
   * Handle showing a toast or refreshing an existing toast
   */
  private void showToast(String text) {
    if (mHelpToast == null) {
      mHelpToast = Toast.makeText(getApplicationContext(), text,
          Toast.LENGTH_LONG);
    } else {
      mHelpToast.setText(text);
      // TODO Can we get these toasts to display in a different spot?
      mHelpToast.setDuration(Toast.LENGTH_LONG);
    }
    mHelpToast.show();
  }
  
  /**
   * Helper method for purchaser to communicate failures to user.
   */
  protected void showPurchaseFailureToast() {
    showToast(getString(R.string.toast_packpurchase_purhcasefailed));
  }

  /**
   * Override back button to carry music on back to the Title activity
   */
  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && event.isTracking()
        && !event.isCanceled()) {
      mContinueMusic = true; // Flag to keep music playing
    }

    return super.onKeyUp(keyCode, event);
  }

  /**
   * Override onPause for music continuation
   */
  @Override
  public void onPause() {
    super.onPause();
    
    if (!mContinueMusic) {
      BuzzWordsApplication application = (BuzzWordsApplication) this
          .getApplication();
      MediaPlayer mp = application.getMusicPlayer(application.getBaseContext());
      SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this
          .getBaseContext());
      if (mp.isPlaying() && sp.getBoolean(Consts.PREFKEY_MUSIC, true)) {
        application.cleanUpMusicPlayer();
      }
    }
  }

  /**
   * Called when this activity is no longer visible.
   */
  @Override
  protected void onStop() {
      super.onStop();
//      ResponseHandler.unregister(mPurchaseObserver);
//      mBillingService.unbind();
  }

  @Override
  protected void onDestroy() {
      super.onDestroy();
//      ResponseHandler.unregister(mPurchaseObserver);
//      mBillingService.unbind();
  }
}
