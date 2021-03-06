package com.vismaad.naad.player;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.vismaad.naad.AddPlayList.AddPlayList;
import com.vismaad.naad.Constants;
import com.vismaad.naad.R;
import com.vismaad.naad.navigation.NavigationActivity;
import com.vismaad.naad.navigation.home.raagi_detail.RaagiDetailActivity;
import com.vismaad.naad.navigation.playlist.PlayListFrag;
import com.vismaad.naad.player.presenter.ShabadPlayerPresenterImpl;
import com.vismaad.naad.player.service.App;
import com.vismaad.naad.player.service.MediaPlayerState;
import com.vismaad.naad.player.service.ShabadPlayerForegroundService;
import com.vismaad.naad.player.view.ShabadPlayerView;
import com.vismaad.naad.rest.model.raagi.Shabad;
import com.vismaad.naad.sharedprefrences.JBSehajBaniPreferences;
import com.vismaad.naad.sharedprefrences.SehajBaniPreferences;
import com.vismaad.naad.utils.Utils;
import com.vismaad.naad.welcome.WelcomeActivity;
import com.vismaad.naad.welcome.signup.SignupActivity;

import android.support.v7.app.AlertDialog;

import java.util.ArrayList;
import java.util.Random;

import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;

import static com.vismaad.naad.Constants.BIG_FONT_SINGLE_BREAK;
import static com.vismaad.naad.Constants.DOUBLE_BREAK;
import static com.vismaad.naad.Constants.ENGLISH_FONT;
import static com.vismaad.naad.Constants.GURBANI_FONT;
import static com.vismaad.naad.Constants.PLAY_SONG;
import static com.vismaad.naad.Constants.PUNJABI_FONT;
import static com.vismaad.naad.Constants.SINGLE_BREAK;
import static com.vismaad.naad.Constants.TEEKA_ARTH_FONT;
import static com.vismaad.naad.Constants.TEEKA_PAD_ARTH_FONT;
import static com.vismaad.naad.player.service.MediaPlayerState.SHABAD_DURATION;
import static com.vismaad.naad.player.service.MediaPlayerState.shabad_list;
import static com.vismaad.naad.player.service.ShabadPlayerForegroundService.PAUSED;
import static com.vismaad.naad.player.service.ShabadPlayerForegroundService.PLAYING;
import static com.vismaad.naad.player.service.ShabadPlayerForegroundService.STOPPED;

public class ShabadPlayerActivity extends AppCompatActivity implements ShabadPlayerView {

    private ShabadPlayerPresenterImpl shabadPlayerPresenterImpl;
    private ActionBar shabad_player_AB;
    private TextView gurbani_TV, raagi_name_TV, shabad_title_TV;
    private Typeface gurbani_lipi_face;
    private Shabad current_shabad;
    private ScrollView gurbani_SV;
    private SimpleExoPlayerView simpleExoPlayerView;
    private SimpleExoPlayer player;
    private boolean isBound;
    private ShabadPlayerForegroundService shabadPlayerForegroundService;
    private String[] shabadLinks, shabadTitles;
    private int originalShabadIndex = 0;
    public ShowShabadReceiver showShabadReceiver;
    private ShabadDialog shabadDialog;
    private boolean mServiceConnected = false, playSong = false, isToPlay = false;
    private long shabadDuration;
    float hitPercent = 0.3f; //30% of the time it will show ad
    final Random generator = new Random();
    AdView adView_mini;
    private SharedPreferences mSharedPreferences;
    Random mRandom;
    int randomNumber;
    ACProgressFlower dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shabad_player);
        // MobileAds.initialize(this, "ca-app-pub-3940256099942544/5224354917");

        // Use an activity context to get the rewarded video instance.

        shabadPlayerForegroundService = App.getService();


        shabadPlayerPresenterImpl = new ShabadPlayerPresenterImpl(this, ShabadPlayerActivity.this);
        shabadPlayerPresenterImpl.init();
        LocalBroadcastManager.getInstance(this).registerReceiver(showShabadReceiver, new IntentFilter(MediaPlayerState.SHOW_SHABAD));
        mSharedPreferences = getSharedPreferences(
                SehajBaniPreferences.Atree_PREFERENCES, Context.MODE_PRIVATE);
        //TODO - When back button is pressed from this page and a new shabad is clicked, it doesn't play the shabad.
        //TODO - App crashes when on raagiDetail page and notification is on top and next/prev button is pressed.
        //TODO - Set Volume to Mid High

    }

    private void loadRewardedVideoAd() {

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_shabad_player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.action_font_settings:
                shabadDialog.create_dialog_box();
                break;

            case R.id.add_to_playlist:
                if (!JBSehajBaniPreferences.getLoginId(mSharedPreferences).equalsIgnoreCase("")) {
                    Intent mIntent = new Intent(ShabadPlayerActivity.this, AddPlayList.class);
                    mIntent.putExtra("RAGGI_NAME", current_shabad.getRaagiName());
                    mIntent.putExtra("SHABAD_ID", current_shabad.getShabadId());
                    mIntent.putExtra("SHABAD_NAME", current_shabad.getShabadEnglishTitle());
                    startActivity(mIntent);
                } else {
                    createDialog();
                }
                break;

        }
        return true;
    }

    public void createDialog() {
        // dialog.show();
        final Dialog dialog = new Dialog(ShabadPlayerActivity.this);
        dialog.setContentView(R.layout.dialog_login);
        dialog.setTitle("Alert!");

        dialog.show();

        Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);
        Button btnSubmit = (Button) dialog.findViewById(R.id.btnSubmit);
        final EditText editName = (EditText) dialog.findViewById(R.id.editName);


        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close dialog
                dialog.dismiss();
                //Intent mIntent = new Intent(ShabadPlayerActivity.this, NavigationActivity.class);
                //mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                // startActivity(mIntent);
                // finish();
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent(ShabadPlayerActivity.this, SignupActivity.class);
                startActivity(mIntent);

                dialog.dismiss();
            }
        });

    }


    @Override
    protected void onStop() {
        super.onStop();
        doUnbindService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(showShabadReceiver);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
    }

    @Override
    public void changeShabadColor(int color) {
        gurbani_SV.setBackgroundResource(color);

    }

    @Override
    public void initUI() {
        //MobileAds.initialize(this, "ca-app-pub-3940256099942544/5224354917");

        // Use an activity context to get the rewarded video instance.

        shabad_player_AB = getSupportActionBar();
        gurbani_TV = findViewById(R.id.gurbani_TV);
        raagi_name_TV = findViewById(R.id.raagi_name_TV);
        shabad_title_TV = findViewById(R.id.shabad_title_TV);
        gurbani_lipi_face = Typeface.createFromAsset(getAssets(), "fonts/gurblipi_.ttf");
        gurbani_TV.setTypeface(gurbani_lipi_face);
        shabadLinks = new String[NavigationActivity.shabadsList.size()];
        shabadTitles = new String[NavigationActivity.shabadsList.size()];
        showShabadReceiver = new ShowShabadReceiver();
        gurbani_SV = findViewById(R.id.gurbani_SV);
        shabadDialog = new ShabadDialog(this, shabadPlayerPresenterImpl);
        shabadPlayerPresenterImpl.changeShabadView(shabadDialog.getSharedPreferences().getInt("background_color_position", 0));


        MobileAds.initialize(ShabadPlayerActivity.this,
                getResources().getString(R.string.YOUR_ADMOB_APP_ID));

        adView_mini = findViewById(R.id.adView_mini);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView_mini.loadAd(adRequest);

        dialog = new ACProgressFlower.Builder(ShabadPlayerActivity.this)
                .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                .themeColor(Color.WHITE)
                .bgColor(Color.TRANSPARENT)
                .bgAlpha(0)
                .bgCornerRadius(0)
                .fadeColor(Color.DKGRAY).build();
        dialog.setCanceledOnTouchOutside(true);
        //loadRewardedVideoAd();
        dialog.show();
        mRandom = new Random();
    }

    @Override
    public void showCustomAppbar() {
        shabad_player_AB.setDisplayShowTitleEnabled(false);
        shabad_player_AB.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void generateShabadsData() {

        for (int i = 0; i < NavigationActivity.shabadsList.size(); i++) {
            Log.i("URL", "" + NavigationActivity.shabadsList.get(i).getShabadUrl());
            shabadLinks[i] = NavigationActivity.shabadsList.get(i).getShabadUrl().replace(" ", "+");
            if (NavigationActivity.shabadsList.get(i).getShabadUrl().equals(current_shabad.getShabadUrl())) {
                originalShabadIndex = i;
            }
            shabadTitles[i] = NavigationActivity.shabadsList.get(i).getShabadEnglishTitle();
        }

        showCurrentShabad(originalShabadIndex);

//        int j = 0;
//        shabadLinks[j] = current_shabad.getShabadUrl().replace(" ", "+");
//        for(int i = 0; i < nextShabads().size(); i++){
//            shabadLinks[j++] = nextShabads().get(i).getShabadUrl().replace(" ", "+");
//        }
//        for(int i = 0; i < previousShabads().size(); i++){
//            if(j == shabadLinks.length){
//                break;
//            }else{
//                shabadLinks[j++] = previousShabads().get(i).getShabadUrl().replace(" ", "+");
//            }
//
//        }
    }

    @Override
    public void initPlayer() {

        if (!isServiceRunning()) {
            simpleExoPlayerView = findViewById(R.id.player);
            dialog.dismiss();
            player = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector());
            simpleExoPlayerView.setPlayer(player);
            player.seekTo(shabadDuration);
            Intent intent = new Intent(this, ShabadPlayerForegroundService.class);
            intent.putExtra(MediaPlayerState.RAAGI_NAME, current_shabad.getRaagiName());
            intent.putExtra(MediaPlayerState.SHABAD_TITLES, shabadTitles);
            intent.putExtra(MediaPlayerState.SHABAD_LINKS, shabadLinks);
            intent.putExtra(MediaPlayerState.ORIGINAL_SHABAD, originalShabadIndex);
            intent.putExtra(MediaPlayerState.SHABAD, current_shabad);
            intent.putExtra(MediaPlayerState.shabad_list, NavigationActivity.shabadsList);
            intent.setAction(Constants.STARTFOREGROUND_ACTION);
            intent.addCategory(ShabadPlayerForegroundService.TAG);
            if (playSong) {
                intent.putExtra(MediaPlayerState.Action_Play, true);
                startService(intent);
            } else {
                intent.putExtra(MediaPlayerState.Action_Play, false);
                intent.putExtra(SHABAD_DURATION, shabadDuration);
                if (shabadPlayerForegroundService.getStatus() != PLAYING) {
                    startService(intent);
                }
            }
            doBindService();
        } else {
            if (!mServiceConnected) {
                doBindService();
                dialog.dismiss();
                simpleExoPlayerView = findViewById(R.id.player);
                player = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector());
                simpleExoPlayerView.setPlayer(player);
                player.seekTo(shabadDuration);
            }
            Intent intent = new Intent(this, ShabadPlayerForegroundService.class);
            intent.putExtra(MediaPlayerState.RAAGI_NAME, current_shabad.getRaagiName());
            intent.putExtra(MediaPlayerState.SHABAD_TITLES, shabadTitles);
            intent.putExtra(MediaPlayerState.SHABAD_LINKS, shabadLinks);
            intent.putExtra(MediaPlayerState.ORIGINAL_SHABAD, originalShabadIndex);
            intent.putExtra(MediaPlayerState.SHABAD, current_shabad);
            intent.putExtra(MediaPlayerState.shabad_list, NavigationActivity.shabadsList);
            intent.addCategory(ShabadPlayerForegroundService.TAG);
            intent.setAction(Constants.STARTFOREGROUND_ACTION);
            if (playSong) {
                intent.putExtra(MediaPlayerState.Action_Play, true);
                startService(intent);
            } else {
                intent.putExtra(MediaPlayerState.Action_Play, false);
                intent.putExtra(SHABAD_DURATION, shabadDuration);
                if (shabadPlayerForegroundService.getStatus() != PLAYING) {
                    startService(intent);
                }
            }
        }
    }


    @Override
    public void setFetchedShabadValues(Shabad fetched_shabad) {
        current_shabad.setShabadSize(fetched_shabad.getShabadSize());
        current_shabad.setGurmukhiList(fetched_shabad.getGurmukhiList());
        current_shabad.setPunjabiList(fetched_shabad.getPunjabiList());
        current_shabad.setTeekaPadArthList(fetched_shabad.getTeekaPadArthList());
        current_shabad.setTeekaArthList(fetched_shabad.getTeekaArthList());
        current_shabad.setEnglishList(fetched_shabad.getEnglishList());
    }

    @Override
    public void getIntentValues() {
        if (getIntent() != null && getIntent().hasExtra("shabads")) {
            NavigationActivity.shabadsList = getIntent().getExtras().getParcelableArrayList("shabads");
            current_shabad = getIntent().getExtras().getParcelable("current_shabad");
        }
        playSong = getIntent().getBooleanExtra(PLAY_SONG, false);
        shabadDuration = getIntent().getLongExtra(SHABAD_DURATION, 0);
    }

    @Override
    public void showGurmukhi() {
        StringBuilder shabad_text = new StringBuilder();
        for (int i = 0; i < current_shabad.getShabadSize(); i++) {
            shabad_text.append(GURBANI_FONT + current_shabad.getGurmukhiList().get(i) + BIG_FONT_SINGLE_BREAK);
            shabad_text.append("<br>");
        }
        gurbani_TV.setText(Html.fromHtml(shabad_text.toString().replace("<>", "&lt&gt") + "<br><br>"));
    }

    @Override
    protected void onStart() {
        super.onStart();
        //loadRewardedVideoAd();
    }

    @Override
    public void showGurmukhiTeeka() {
        StringBuilder shabad_text = new StringBuilder();
        for (int i = 0; i < current_shabad.getShabadSize(); i++) {
            shabad_text.append(GURBANI_FONT + current_shabad.getGurmukhiList().get(i) + BIG_FONT_SINGLE_BREAK);
            shabad_text.append(teeka_null_case(current_shabad.getTeekaPadArthList().get(i), current_shabad.getTeekaArthList().get(i)));
        }
        gurbani_TV.setText(Html.fromHtml(shabad_text.toString().replace("<>", "&lt&gt") + "<br><br>"));
    }

    @Override
    public void showGurmukhiPunjabi() {
        StringBuilder shabad_text = new StringBuilder();
        for (int i = 0; i < current_shabad.getShabadSize(); i++) {
            shabad_text.append(GURBANI_FONT + current_shabad.getGurmukhiList().get(i) + BIG_FONT_SINGLE_BREAK);
            shabad_text.append(PUNJABI_FONT + current_shabad.getPunjabiList().get(i) + DOUBLE_BREAK);
        }
        gurbani_TV.setText(Html.fromHtml(shabad_text.toString().replace("<>", "&lt&gt") + "<br><br>"));
    }

    @Override
    public void showGurmukhiEnglish() {
        StringBuilder shabad_text = new StringBuilder();
        for (int i = 0; i < current_shabad.getShabadSize(); i++) {
            shabad_text.append(GURBANI_FONT + current_shabad.getGurmukhiList().get(i) + BIG_FONT_SINGLE_BREAK);
            shabad_text.append(ENGLISH_FONT + current_shabad.getEnglishList().get(i) + DOUBLE_BREAK);
        }
        gurbani_TV.setText(Html.fromHtml(shabad_text.toString().replace("<>", "&lt&gt") + "<br><br>"));
    }

    @Override
    public void showGurmukhiTeekaPunjabi() {
        StringBuilder shabad_text = new StringBuilder();
        for (int i = 0; i < current_shabad.getShabadSize(); i++) {
            shabad_text.append(GURBANI_FONT + current_shabad.getGurmukhiList().get(i) + BIG_FONT_SINGLE_BREAK);
            shabad_text.append(PUNJABI_FONT + current_shabad.getPunjabiList().get(i) + SINGLE_BREAK);
            shabad_text.append(teeka_null_case(current_shabad.getTeekaPadArthList().get(i), current_shabad.getTeekaArthList().get(i)));
        }
        gurbani_TV.setText(Html.fromHtml(shabad_text.toString().replace("<>", "&lt&gt") + "<br><br>"));
    }

    @Override
    public void showGurmukhiTeekaEnglish() {
        StringBuilder shabad_text = new StringBuilder();
        for (int i = 0; i < current_shabad.getShabadSize(); i++) {
            shabad_text.append(GURBANI_FONT + current_shabad.getGurmukhiList().get(i) + BIG_FONT_SINGLE_BREAK);
            shabad_text.append(ENGLISH_FONT + current_shabad.getEnglishList().get(i) + SINGLE_BREAK);
            shabad_text.append(teeka_null_case(current_shabad.getTeekaPadArthList().get(i), current_shabad.getTeekaArthList().get(i)));
        }
        gurbani_TV.setText(Html.fromHtml(shabad_text.toString().replace("<>", "&lt&gt") + "<br><br>"));
    }

    @Override
    public void showGurmukhiPunjabiEnglish() {
        StringBuilder shabad_text = new StringBuilder();
        for (int i = 0; i < current_shabad.getShabadSize(); i++) {
            shabad_text.append(GURBANI_FONT + current_shabad.getGurmukhiList().get(i) + BIG_FONT_SINGLE_BREAK);
            shabad_text.append(PUNJABI_FONT + current_shabad.getPunjabiList().get(i) + SINGLE_BREAK);
            shabad_text.append(ENGLISH_FONT + current_shabad.getEnglishList().get(i) + DOUBLE_BREAK);
        }
        gurbani_TV.setText(Html.fromHtml(shabad_text.toString().replace("<>", "&lt&gt") + "<br><br>"));
    }

    @Override
    public void showGurmukhiTeekaPunjabiEnglish() {
        StringBuilder shabad_text = new StringBuilder();
        for (int i = 0; i < current_shabad.getShabadSize(); i++) {
            shabad_text.append(GURBANI_FONT + current_shabad.getGurmukhiList().get(i) + BIG_FONT_SINGLE_BREAK);
            shabad_text.append(PUNJABI_FONT + current_shabad.getPunjabiList().get(i) + SINGLE_BREAK);
            shabad_text.append(ENGLISH_FONT + current_shabad.getEnglishList().get(i) + SINGLE_BREAK);
            shabad_text.append(teeka_null_case(current_shabad.getTeekaPadArthList().get(i), current_shabad.getTeekaArthList().get(i)));
        }
        gurbani_TV.setText(Html.fromHtml(shabad_text.toString().replace("<>", "&lt&gt") + "<br><br>"));
    }

    @Override
    public void changeTranlationSize(int size) {
        gurbani_TV.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
    }

    private void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        bindService(new Intent(ShabadPlayerActivity.this,
                ShabadPlayerForegroundService.class), mConnection, Context.BIND_AUTO_CREATE);
        isBound = true;
    }

    private void doUnbindService() {
        if (isBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            isBound = false;
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            shabadPlayerForegroundService = ((ShabadPlayerForegroundService.LocalBinder) service).getService();
            player = shabadPlayerForegroundService.getPlayer();
            simpleExoPlayerView.setPlayer(player);
            mServiceConnected = true;
            App.setService(shabadPlayerForegroundService);
        }

        public void onServiceDisconnected(ComponentName className) {
            shabadPlayerForegroundService = null;
            mServiceConnected = false;
        }
    };

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        assert manager != null;
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.sehaj.bani.player.service.ShabadPlayerForegroundService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void showCurrentShabad(int showShabadIndex) {
        Log.i("index-number-class", "" + showShabadIndex);
        current_shabad = NavigationActivity.shabadsList.get(showShabadIndex);
        shabad_player_AB.setTitle(current_shabad.getShabadEnglishTitle());
        raagi_name_TV.setText(current_shabad.getRaagiName());
        shabad_title_TV.setText(current_shabad.getShabadEnglishTitle());
        shabadPlayerPresenterImpl.prepareShabad(current_shabad.getStartingId(), current_shabad.getEndingId());
        // todo prepare lyrics from shared preference otherwise load default
        shabadPlayerPresenterImpl.prepareTranslation(App.getPrefranceDataBoolean(Constants.TEEKA_CB),
                App.getPrefranceDataBoolean(Constants.PUNJABI_CB), App.getPrefranceDataBoolean(Constants.ENGLISH_CB));
        shabadPlayerPresenterImpl.setTranslationSize(App.getPreferanceInt(Constants.FONT_SIZE));
        saveLastShabadToPlay();
    }

    public void onTranslationSelected(View view) {
        boolean checked = ((CheckBox) view).isChecked();

        switch (view.getId()) {
            case R.id.teeka_CB:
                shabadDialog.setTeeka(checked);
                App.setPreferencesBoolean(Constants.TEEKA_CB, checked);
                break;

            case R.id.punjabi_CB:
                shabadDialog.setPunjabi(checked);
                App.setPreferencesBoolean(Constants.PUNJABI_CB, checked);
                break;

            case R.id.english_CB:
                shabadDialog.setEnglish(checked);
                App.setPreferencesBoolean(Constants.ENGLISH_CB, checked);
                break;
        }
        shabadPlayerPresenterImpl.prepareTranslation(App.getPrefranceDataBoolean(Constants.TEEKA_CB),
                App.getPrefranceDataBoolean(Constants.PUNJABI_CB), App.getPrefranceDataBoolean(Constants.ENGLISH_CB));
    }

    private String teeka_null_case(String pad_arth, String arth) {
        String teeka = "";
        if (pad_arth.equals("") || arth.equals("")) {
            if (pad_arth.equals("") && !arth.equals(""))
                teeka = TEEKA_ARTH_FONT + arth + DOUBLE_BREAK;

            if (!pad_arth.equals("") && arth.equals(""))
                teeka = TEEKA_PAD_ARTH_FONT + pad_arth + DOUBLE_BREAK;

            if (pad_arth.equals("") && arth.equals(""))
                teeka = SINGLE_BREAK;

        } else {
            teeka = TEEKA_PAD_ARTH_FONT + pad_arth + SINGLE_BREAK + TEEKA_ARTH_FONT + arth + DOUBLE_BREAK;
        }

        return teeka;
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    private void saveLastShabadToPlay() {
        // store shabad list in shared pref using set in android or in list of json
        String json = App.getGson().toJson(NavigationActivity.shabadsList);
        if (App.getPrefranceData(MediaPlayerState.shabad_list) != null && App.getPrefranceData(MediaPlayerState.shabad_list).length() > 0) {
            App.setPreferences(MediaPlayerState.shabad_list, "");
        }
        App.setPreferences(MediaPlayerState.shabad_list, json);

        String jsonShabad = App.getGson().toJson(current_shabad);
        if (App.getPrefranceData(MediaPlayerState.SHABAD) != null && App.getPrefranceData(MediaPlayerState.SHABAD).length() > 0) {
            App.setPreferences(MediaPlayerState.SHABAD, "");
        }
        App.setPreferences(MediaPlayerState.SHABAD, jsonShabad);
        if (player != null)
            App.setPreferencesLong(SHABAD_DURATION, player.getCurrentPosition());
        else
            App.setPreferencesLong(SHABAD_DURATION, shabadDuration);
    }

    public class ShowShabadReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                int showShabadIndex = intent.getIntExtra(MediaPlayerState.SHOW_SHABAD, 0);
                showCurrentShabad(showShabadIndex);
            }
        }
    }
}
