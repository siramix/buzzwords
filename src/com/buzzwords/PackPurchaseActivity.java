package com.buzzwords;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONException;

import com.amazon.inapp.purchasing.PurchasingManager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class PackPurchaseActivity extends Activity {

  private static final String TAG = "PackPurchase";
  
  // To be used for tooltips to help guide users
  private Toast mHelpToast = null;

  List<View> mPackLineList;
  
  // Our pack lists as retrieved from the server
  private LinkedList<Pack> mServerPacks;
  
  private GameManager mGameManager;

  /**
   * This block of maps stores our lists of clients
   */
  HashMap<String, String> mKnownFacebookClients;
  HashMap<String, ActivityInfo> mFoundFacebookClients;

  /**
   * This block of variables is for Amazon In-App Purchases
   */
  // currently logged in user
  private String mCurrentUser;
  // Mapping of our requestIds to unlockable content
  public Map<String, String> requestIds;
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
      Log.d(TAG, "PlayGameListener OnClick()");

      // play confirm sound
      SoundManager sm = SoundManager.getInstance(PackPurchaseActivity.this
          .getBaseContext());
      sm.playSound(SoundManager.Sound.CONFIRM);

      SharedPreferences packSelectionPrefs = getSharedPreferences(Consts.PREFFILE_PACK_SELECTIONS,
          Context.MODE_PRIVATE);

      Map<String, ?> packSelections = new HashMap<String, Boolean>();
      packSelections = packSelectionPrefs.getAll();

      boolean anyPackSelected = false;
      for (String packId : packSelections.keySet()) {
        if (packSelectionPrefs.getBoolean(packId, false) == true) {
          anyPackSelected = true;
        }
      }

      // Only advance to next screen if a pack is selected
      if (anyPackSelected == true) {
        startActivity(new Intent(PackPurchaseActivity.this.getApplication()
            .getString(R.string.IntentGameSetup), getIntent().getData()));
      } else {
        showToast(getString(R.string.toast_wordpurchase_nopackselected));
      }
    }
  };

  /**
   * Create the packages screen from an XML layout and
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate()");

    // Initialize our packs
    mServerPacks = new LinkedList<Pack>();
    
    mGameManager= new GameManager(PackPurchaseActivity.this);

    requestIds = new HashMap<String, String>();

    // Force volume controls to affect Media volume
    setVolumeControlStream(AudioManager.STREAM_MUSIC);

    // Setup the view
    this.setContentView(R.layout.packpurchase);

    // Detect Social Clients
    detectClients();
    
    // set fonts on titles
    Typeface antonFont = Typeface.createFromAsset(getAssets(),
        "fonts/Anton.ttf");
    TextView header = (TextView) this.findViewById(R.id.PackPurchase_Title);
    header.setTypeface(antonFont);
    header = (TextView) this.findViewById(R.id.PackPurchase_UnlockedPackTitle);
    header.setTypeface(antonFont);
    header = (TextView) this.findViewById(R.id.PackPurchase_PaidPackTitle);
    header.setTypeface(antonFont);
    
    // Instantiate all of our lists for programmatic adding of packs to view
    mPackLineList = new LinkedList<View>();
    
    refreshAllPackLayouts();
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
    PurchasingManager.initiateGetUserIdRequest();
    refreshAllPackLayouts();
  }

  protected void refreshAllPackLayouts() {
    Log.d(TAG, "refreshAllPackLayouts");
    // Get our current context

    // Populate and display list of cards
    LinearLayout unlockedPackLayout = (LinearLayout) findViewById(R.id.PackPurchase_UnlockedPackSets);
    LinearLayout paidPackLayout = (LinearLayout) findViewById(R.id.PackPurchase_PaidPackSets);
    
    unlockedPackLayout.removeAllViewsInLayout();
    paidPackLayout.removeAllViewsInLayout();

    PackClient client = PackClient.getInstance();
    LinkedList<Pack> lockedPacks = new LinkedList<Pack>();
    LinkedList<Pack> unlockedPacks = new LinkedList<Pack>();
    unlockedPacks = mGameManager.getInstalledPacks();

    //TODO these http pack requests should be in their own methods (getLockedPacksFromServer...)
    // First try to get the online packs, if no internet, just use local packs
    try {
      mServerPacks = client.getServerPacks();
      lockedPacks = getUnownedPacks(mServerPacks, unlockedPacks);
      populatePackLayout(unlockedPacks, unlockedPackLayout);
      //TODO maybe we want to put this on the purchase button isntead
      populatePackLayout(lockedPacks, paidPackLayout);
    } catch (IOException e1) {
      populatePackLayout(unlockedPacks, unlockedPackLayout);
      showToast(getString(R.string.toast_packpurchase_nointerneterror));
      e1.printStackTrace();
    } catch (URISyntaxException e1) {
      populatePackLayout(unlockedPacks, unlockedPackLayout);
      showToast(getString(R.string.toast_packpurchase_siramixdownerror));
      e1.printStackTrace();
    } catch (JSONException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    
    //TODO Edward this is here for you to clean up
    TextView percentagePlayed = (TextView) this.findViewById(R.id.PackPurchase_TMP_PERCENTAGE);
    TextView numSeen = (TextView) this.findViewById(R.id.PackPurchase_NUMSEEN);
    TextView totalCardCount = (TextView) this.findViewById(R.id.PackPurchase_TOTALCARDS);
    int totalSeen = 0;
    int totalCards = 0;
    for (Pack pack : unlockedPacks) {
      totalSeen += pack.getNumCardsSeen();
      totalCards += pack.getSize();
    }
    numSeen.setText(String.valueOf(totalSeen));
    totalCardCount.setText("/" + String.valueOf(totalCards));
    int percentSeen = (int) Math.round(
        ((float) totalSeen / (float) totalCards) * 100.00);
    percentagePlayed.setText(String.valueOf(percentSeen));
    
    ProgressBar progress = (ProgressBar) this.findViewById(R.id.PackPurchase_Progress);
    progress.setProgress((float) totalSeen / (float) totalCards);
    progress.setTotal(totalCards);

    Button btn = (Button) this.findViewById(R.id.PackPurchase_Button_Next);
    btn.setOnClickListener(mGameSetupListener);
  }

  /**
   * Remove from lockedPacks those packs that are already installed.
   * @param lockedPacks
   * @param localPacks
   * @return
   */
  private LinkedList<Pack> getUnownedPacks(LinkedList<Pack> lockedPacks, LinkedList<Pack> installedPacks) {
    Log.d(TAG, "removeLocalPacks");
    LinkedList<Pack> unownedPackList = new LinkedList<Pack>();
    unownedPackList.addAll(lockedPacks);
    
    for (Pack localPack : installedPacks) {
      for (int unownedIndex=0; unownedIndex<unownedPackList.size(); ++unownedIndex) {
        if (localPack.getId() == unownedPackList.get(unownedIndex).getId()) {
          unownedPackList.remove(unownedIndex);
        }
      }
    }
    
    return unownedPackList;
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
  private void populatePackLayout(List<Pack> packlist,
      LinearLayout insertionPoint) {
    int count = 0;

    // Instantiate all our views for programmatic layout creation
    LinearLayout layout = new LinearLayout(this.getBaseContext());
    layout.setOrientation(LinearLayout.VERTICAL);

    for (Iterator<Pack> it = packlist.iterator(); it.hasNext();) {
      Pack curPack = it.next();

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
      if(count > 0)
      {
        margin.setMargins(0, (int) (-2 * DENSITY), 0, 0);

      }
      row.setLayoutParams(margin);
      mPackLineList.add(row);

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
    boolean syncRequired = getSyncPreferences().getBoolean(Consts.PREFKEY_SYNC_REQUIRED, true);
    boolean updateRequired = mGameManager.packsRequireUpdate(mServerPacks);
    String previousUser = getSyncPreferences().getString(Consts.PREFKEY_LAST_USER, getCurrentUser());
    
    // If user has switched, trigger a re-sync
    if (!previousUser.equals(getCurrentUser())) {
      syncRequired = true;
    }
    
    Log.d(TAG, "   SYNC_REQUIRED: " + syncRequired);
    Log.d(TAG, "   UPDATE REQUIRED: " + updateRequired);
    
    Pack[] packArray = mServerPacks.toArray(new Pack[mServerPacks.size()]);
    try {
      // Don't call syncronize unless SYNCED preference is true or some packs are out of date
      if (syncRequired || updateRequired) {
        new PackSyncronizer().execute(packArray);
      }
    } catch (RuntimeException e) {
      Log.e(TAG, "Encountered an error syncronizing packs.");
      e.printStackTrace();
    }
  }

  /**
   * Opens the Facebook client for promotional packs
   */
  private void openFacebookClient()
  {
    ComponentName targetComponent = getClientComponentName(mFoundFacebookClients);

    if (targetComponent != null) {
      Intent facebookIntent = new Intent(Intent.ACTION_SEND);
      facebookIntent.setComponent(targetComponent);
      String intentType = ("text/plain");
      facebookIntent.setType(intentType);
      facebookIntent.putExtra(Intent.EXTRA_TEXT, BuzzWordsApplication.storeURI_Buzzwords.toString());
      startActivityForResult(facebookIntent, FACEBOOK_REQUEST_CODE);
    } else {
      showToast(getString(R.string.toast_packpurchase_nofacebook));
    }
  };

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
          final SharedPreferences settings = getSharedPreferencesForCurrentUser();
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
      final SharedPreferences.Editor editor = getSharedPreferencesEditor();
      // The requires sync preference must be set globally (across users) so switching users triggers a sync
      final SharedPreferences.Editor syncPrefEditor = getSyncPreferences().edit();
      editor.putBoolean(String.valueOf(PackPurchaseConsts.FACEBOOK_PACK_ID), true);
      syncPrefEditor.putBoolean(Consts.PREFKEY_SYNC_REQUIRED, true);
      syncPrefEditor.putString(Consts.PREFKEY_LAST_USER, getCurrentUser());
      editor.commit();
      syncPrefEditor.commit();
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
  
  public class PackSyncronizer extends AsyncTask <Pack, Void, Integer>
  {
    private ProgressDialog dialog;
    final SharedPreferences userPurchases = getSharedPreferencesForCurrentUser();
    final SharedPreferences.Editor syncPrefEditor = getSyncPreferences().edit();
    final GameManager gm = new GameManager(PackPurchaseActivity.this);
    
    @Override
    protected void onPreExecute() {
      dialog = ProgressDialog.show(
          PackPurchaseActivity.this,
          null,
          getString(R.string.progressDialog_update_text), 
          true);
    }
    
    @Override
    protected Integer doInBackground(Pack... packs) {
      for (int i=0; i<packs.length; ++i) {
        Log.d(TAG, "SYNCING PACK: " + packs[i].getName());
        boolean isPackPurchased = userPurchases.getBoolean(String.valueOf(packs[i].getId()), false);
        if (isPackPurchased) {
          gm.installPack(packs[i]);
        } 
        // Uninstall pack if it is not purchased and is a premium pack
        else if (isPackPurchased == false && 
            packs[i].getPurchaseType() != PackPurchaseConsts.PACKTYPE_FREE) {
          gm.uninstallPack(packs[i].getId());
        }
        // Update free and starter packs
        else if (packs[i].getPurchaseType() == PackPurchaseConsts.PACKTYPE_FREE) {
          gm.installPack(packs[i]);
        }
        else {
          Log.e(TAG, "Failed to update or install packId: " + packs[i].getId() + " name: " + packs[i].getName());
        }
      }
      return 0; 
    }
    
    @Override
    protected void onPostExecute(Integer result)
    {
      dialog.dismiss();
      refreshAllPackLayouts();
      
      syncPrefEditor.putBoolean(Consts.PREFKEY_SYNC_REQUIRED, false);
      syncPrefEditor.putString(Consts.PREFKEY_LAST_USER, getCurrentUser());
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
      setPackSelectedPref(pack, selectionStatus);

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
      // play confirm sound when points are added
      SoundManager sm = SoundManager.getInstance(PackPurchaseActivity.this
          .getBaseContext());
      sm.playSound(SoundManager.Sound.CONFIRM);
     
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
   * http://blogrescue.com/2011/12/android-development-send-tweet-action/
   */
  private void buildKnownClientsList() {
    Log.d(TAG, "buildKnownClientsList()");
    mKnownFacebookClients = new HashMap<String, String>();
    mKnownFacebookClients.put("Facebook",
        "com.facebook.katana.ShareLinkActivity");
    mKnownFacebookClients.put("FriendCaster",
        "uk.co.senab.blueNotifyFree.activity.PostToFeedActivity");
  }

  /**
   * http://blogrescue.com/2011/12/android-development-send-tweet-action/
   * 
   * @return
   */
  public void detectClients() {
    Log.d(TAG, "detectClients()");

    buildKnownClientsList();
    mFoundFacebookClients = new HashMap<String, ActivityInfo>();

    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType("text/plain");
    PackageManager pm = getPackageManager();
    List<ResolveInfo> activityList = pm.queryIntentActivities(intent, 0);

    for (int i = 0; i < activityList.size(); i++) {
      ResolveInfo app = (ResolveInfo) activityList.get(i);
      ActivityInfo activity = app.activityInfo;
      Log.d(TAG, "******* --> " + activity.name);
      if (mKnownFacebookClients.containsValue(activity.name)) {
        mFoundFacebookClients.put(activity.name, activity);
      }
    }
  }

  /**
   * Get the current value of the pack preferences for a given pack name
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
   * Returns the Component name of either Twitter, Google, or Facebook
   * 
   * @param foundClients
   *          A hashmap of clients that have been identified by Detect Clients
   *          as being on the users phone
   * @return
   */
  public ComponentName getClientComponentName(
      HashMap<String, ActivityInfo> foundClients) {
    Log.d(TAG, "getClientComponentName()");

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
   * Get the SharedPreferences file for the current user.
   * @return SharedPreferences file for a user.
   */
  private SharedPreferences getSharedPreferencesForCurrentUser() {
      final SharedPreferences settings = getSharedPreferences(mCurrentUser, Context.MODE_PRIVATE);
      return settings;
  }
  
  /**
   * Generate a SharedPreferences.Editor object. 
   * @return editor for Shared Preferences file.
   */
  private SharedPreferences.Editor getSharedPreferencesEditor(){
      return getSharedPreferencesForCurrentUser().edit();
  }
  
  /**
   * Get the preferences file that all users will share
   * @return SharedPreferences file for all users.
   */
  protected SharedPreferences getSyncPreferences() {
    final SharedPreferences syncPrefs = getSharedPreferences(Consts.PREFFILE_SYNC_REQUIRED, Context.MODE_PRIVATE);
    return syncPrefs;
  }
  
  /**
   * Gets current logged in user
   * @return current user
   */
  protected String getCurrentUser(){
    return mCurrentUser;
  }
  
  /**
   * Sets current logged in user
   * @param currentUser current user to set
   */
  void setCurrentUser(final String currentUser){
      this.mCurrentUser = currentUser;
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
