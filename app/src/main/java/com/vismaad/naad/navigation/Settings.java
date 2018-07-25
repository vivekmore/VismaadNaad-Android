package com.vismaad.naad.navigation;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.vismaad.naad.Constants;
import com.vismaad.naad.R;
import com.vismaad.naad.databinding.SettingsBinding;
import com.vismaad.naad.player.service.App;
import com.vismaad.naad.player.service.ShabadPlayerForegroundService;
import com.vismaad.naad.sharedprefrences.JBSehajBaniPreferences;
import com.vismaad.naad.sharedprefrences.SehajBaniPreferences;
import com.vismaad.naad.utils.Utils;
import com.vismaad.naad.welcome.WelcomeActivity;
import com.vismaad.naad.welcome.login.LoginActivity;

/**
 * Created by satnamsingh on 25/06/18.
 */

public class Settings extends Fragment implements View.OnClickListener {
    static public final int STOPPED = -1, PAUSED = 0, PLAYING = 1;

    SettingsBinding binding;
    View view;
    private SharedPreferences mSharedPreferences;
    private AdView mAdView;
    Intent i;
    Uri uri;
    Intent intent;
    private static SimpleExoPlayer player;
    private static int status = STOPPED;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater,
                R.layout.settings, container, false);
        view = binding.getRoot();

        initial();
        return view;
    }

    private void initial() {
        MobileAds.initialize(getActivity(),
                getResources().getString(R.string.YOUR_ADMOB_APP_ID));
        mAdView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mSharedPreferences = getActivity().getSharedPreferences(
                SehajBaniPreferences.Atree_PREFERENCES, Context.MODE_PRIVATE);
       // binding.rl1.setOnClickListener(this);
        binding.rl2.setOnClickListener(this);
        binding.btnSubmit.setOnClickListener(this);
        binding.shabadThumbnailIV.setOnClickListener(this);
       // binding.txtLogout.setOnClickListener(this);
        binding.rlFbLike.setOnClickListener(this);
        binding.txtLikeFb.setOnClickListener(this);
        binding.imageLike.setOnClickListener(this);

        // binding.txtLog.setText("You are logged is as " + JBSehajBaniPreferences.getLoginId(mSharedPreferences));

    }

    @Override
    public void onResume() {
        super.onResume();
        /* ((AppCompatActivity) getActivity()).getSupportActionBar().hide();*/

    }

    @Override
    public void onStop() {
        super.onStop();
        /* ((AppCompatActivity) getActivity()).getSupportActionBar().show();*/
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.rl_fb_like:
                uri = Uri.parse("https://www.facebook.com/Vismaad-Apps-1413125452234300/"); // missing 'http://' will cause crashed
                intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                break;

            case R.id.imageLike:
                uri = Uri.parse("https://www.facebook.com/Vismaad-Apps-1413125452234300/"); // missing 'http://' will cause crashed
                intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                break;


            case R.id.txtLikeFb:
                uri = Uri.parse("https://www.facebook.com/Vismaad-Apps-1413125452234300/"); // missing 'http://' will cause crashed
                intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                break;


            case R.id.shabad_thumbnail_IV:
               // player.setPlayWhenReady(false);
                App.setPreferencesInt(Constants.PLAYER_STATE, 0);
               // status= STOPPED;
              /*  getActivity().stopForeground(true);
                stopSelf();*/
               // doUnbindService();
                //getActivity().stopService(new Intent(getActivity().getBaseContext(), ShabadPlayerForegroundService.class));

                Intent intent2 = new Intent(getActivity(), ShabadPlayerForegroundService.class);
                intent2.addCategory(ShabadPlayerForegroundService.TAG);
                getActivity().stopService(intent2);

                JBSehajBaniPreferences.setRaggiId(mSharedPreferences, "");
                JBSehajBaniPreferences.setLoginId(mSharedPreferences, "");
                JBSehajBaniPreferences.setJwtToken(mSharedPreferences, "");
                SharedPreferences preferences = getActivity().getSharedPreferences("shabadSettingsData", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.clear();
                editor.commit();

//                ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
//                assert manager != null;
//                for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
//                    String serviceStr = "com.sehaj.bani.player.service.ShabadPlayerForegroundService";
//                    if (serviceStr.equals(service.service.getClassName())) {
//                        manager.killBackgroundProcesses(serviceStr);
//                    }
//                }
//
//                ShabadPlayerForegroundService foregroundService = new ShabadPlayerForegroundService();
//                if(foregroundService.getInstance() != null){
//                    foregroundService.getInstance().stop();
//                }
               // getActivity().stopService(new Intent(getActivity(), ShabadPlayerForegroundService.class));

         /*       i = new Intent(getActivity(), WelcomeActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);*/
                ActivityCompat.finishAffinity(getActivity());
                Intent intent = new Intent(getActivity(), WelcomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

                break;

       /*     case R.id.txtLogout:
                JBSehajBaniPreferences.setRaggiId(mSharedPreferences, "");
                JBSehajBaniPreferences.setLoginId(mSharedPreferences, "");
                JBSehajBaniPreferences.setJwtToken(mSharedPreferences, "");
                i = new Intent(getActivity(), WelcomeActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                break;*/

            /*case R.id.rl1:
               // getActivity().stopService(new Intent(getActivity(), ShabadPlayerForegroundService.class));
                player.setPlayWhenReady(false);
                App.setPreferencesInt(Constants.PLAYER_STATE, 0);
                status= STOPPED;
                Intent intent1 = new Intent(getActivity(), ShabadPlayerForegroundService.class);
                intent1.addCategory(ShabadPlayerForegroundService.TAG);
                getActivity().stopService(intent1);
                 JBSehajBaniPreferences.setRaggiId(mSharedPreferences, "");
                JBSehajBaniPreferences.setLoginId(mSharedPreferences, "");
                JBSehajBaniPreferences.setJwtToken(mSharedPreferences, "");
                preferences = getActivity().getSharedPreferences("shabadSettingsData", Context.MODE_PRIVATE);
                editor = preferences.edit();
                editor.clear();
                editor.commit();
                i = new Intent(getActivity(), WelcomeActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);

                break;*/

            case R.id.rl2:
                boolean isClear = Utils.deleteCache(getContext());
                if (isClear == true) {
                    Toast.makeText(getActivity(), "Delete cache successfully", Toast.LENGTH_LONG).show();
                }

                break;

            case R.id.btnSubmit:
                Intent email = new Intent(Intent.ACTION_SEND);
                email.putExtra(Intent.EXTRA_EMAIL, new String[]{binding.emailUsernameET.getText().toString()});
                email.putExtra(Intent.EXTRA_SUBJECT, "Question");
                email.putExtra(Intent.EXTRA_TEXT, binding.megET.getText().toString());
                email.setType("message/rfc822");
                startActivity(Intent.createChooser(email, "Choose an Email client :"));

                break;

            default:
                break;

        }
    }
}
