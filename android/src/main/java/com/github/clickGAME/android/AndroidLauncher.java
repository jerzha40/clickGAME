package com.github.clickGAME.android;

import android.os.Bundle;
import android.widget.Toast;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.github.clickGAME.Main;
import com.github.clickGAME.AdController;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;

import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.IUnityAdsInitializationListener;

import android.util.Log;
import androidx.annotation.NonNull;

public class AndroidLauncher extends AndroidApplication implements AdController {
    @Override
    public boolean isAdMobReady() {
        return admobRewarded != null;
    }

    @Override
    public boolean isUnityAdReady() {
        return unityAdLoaded;
    }

    private RewardedAd admobRewarded;
    private static final String UNITY_GAME_ID = "5841813";
    private static final String UNITY_REWARDED_PLACEMENT_ID = "500";
    private boolean unityAdLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
        configuration.useImmersiveMode = true;

        MobileAds.initialize(this, status -> {
        });
        UnityAds.initialize(this, UNITY_GAME_ID, false, new IUnityAdsInitializationListener() {
            @Override
            public void onInitializationComplete() {
                Log.i("UnityAds", "Initialization complete");
            }

            @Override
            public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
                Log.e("UnityAds", "Init failed: " + message);
            }
        });

        loadAdMobRewarded();
        loadUnityRewarded();

        initialize(new Main(this), configuration);
    }

    private void loadAdMobRewarded() {
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, "ca-app-pub-3940256099942544/5224354917", adRequest,
                new RewardedAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull RewardedAd ad) {
                        admobRewarded = ad;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError error) {
                        admobRewarded = null;
                        Log.e("AdMob", "Load failed: " + error.getMessage());
                    }
                });
    }

    private void showAdMobInternal(Runnable onReward) {
        runOnUiThread(() -> {
            if (admobRewarded != null) {
                admobRewarded.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        loadAdMobRewarded();
                    }
                });
                admobRewarded.show(AndroidLauncher.this, rewardItem -> {
                    if (onReward != null)
                        onReward.run();
                });
                admobRewarded = null;
            } else {
                Toast.makeText(this, "AdMob广告未就绪", Toast.LENGTH_SHORT).show();
                loadAdMobRewarded();
            }
        });
    }

    private void loadUnityRewarded() {
        UnityAds.load(UNITY_REWARDED_PLACEMENT_ID, new IUnityAdsLoadListener() {
            @Override
            public void onUnityAdsAdLoaded(String placementId) {
                unityAdLoaded = true;
            }

            @Override
            public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String msg) {
                unityAdLoaded = false;
                Log.e("UnityAds", "Load failed: " + msg);
            }
        });
    }

    @Override
    public void showAdMobRewardedAd(Runnable onReward) {
        showAdMobInternal(onReward);
    }

    @Override
    public void showUnityRewardedAd(Runnable onReward) {
        runOnUiThread(() -> {
            if (UnityAds.isInitialized() && unityAdLoaded) {
                UnityAds.show(this, UNITY_REWARDED_PLACEMENT_ID, new IUnityAdsShowListener() {
                    @Override
                    public void onUnityAdsShowStart(String placementId) {
                    }

                    @Override
                    public void onUnityAdsShowClick(String placementId) {
                    }

                    @Override
                    public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {
                        if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED && onReward != null)
                            onReward.run();
                        loadUnityRewarded();
                    }

                    @Override
                    public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error,
                            String msg) {
                        Log.e("UnityAds", "Show failed: " + msg);
                        loadUnityRewarded();
                    }
                });
                unityAdLoaded = false;
            } else {
                Toast.makeText(this, "Unity广告未就绪", Toast.LENGTH_SHORT).show();
                if (!unityAdLoaded)
                    loadUnityRewarded();
            }
        });
    }
}
