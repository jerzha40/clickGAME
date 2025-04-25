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
import android.util.Log;
import androidx.annotation.NonNull;

public class AndroidLauncher extends AndroidApplication implements AdController {
    private RewardedAd rewardedAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
        configuration.useImmersiveMode = true;

        MobileAds.initialize(this, initializationStatus -> {
        });
        loadRewardedAd();

        initialize(new Main(this), configuration);
    }

    private void loadRewardedAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, "ca-app-pub-3940256099942544/5224354917", adRequest,
                new RewardedAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull RewardedAd ad) {
                        rewardedAd = ad;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError error) {
                        rewardedAd = null;
                        Log.e("AdMob", "广告加载失败: " + error.getMessage());
                    }
                });
    }

    @Override
    public void showRewardedAd(Runnable onReward) {
        runOnUiThread(() -> {
            if (rewardedAd != null) {
                rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        loadRewardedAd();
                    }
                });

                rewardedAd.show(AndroidLauncher.this, rewardItem -> {
                    if (onReward != null)
                        onReward.run();
                });

                rewardedAd = null;
            } else {
                Toast.makeText(this, "广告尚未加载完成", Toast.LENGTH_SHORT).show();
                loadRewardedAd();
            }
        });
    }
}
