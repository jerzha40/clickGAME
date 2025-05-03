package com.github.clickGAME;

public interface AdController {
    boolean isAdMobReady();

    boolean isUnityAdReady();

    void showAdMobRewardedAd(Runnable onReward);

    void showUnityRewardedAd(Runnable onReward);
}
