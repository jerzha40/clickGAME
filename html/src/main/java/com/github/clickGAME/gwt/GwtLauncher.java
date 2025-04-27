package com.github.clickGAME.gwt;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.github.clickGAME.Main;
import com.github.clickGAME.AdController;
import com.google.gwt.user.client.Window;

/** Launches the GWT application. */
public class GwtLauncher extends GwtApplication {
    @Override
    public GwtApplicationConfiguration getConfig() {
        GwtApplicationConfiguration cfg = new GwtApplicationConfiguration(true);
        cfg.padVertical = 0;
        cfg.padHorizontal = 0;
        return cfg;
    }

    @Override
    public ApplicationListener createApplicationListener() {
        return new Main(new GwtAdController());
    }

    static class GwtAdController implements AdController {
        @Override
        public void showAdMobRewardedAd(Runnable onReward) {
            Window.alert("[GWT] Simulating AdMob rewarded ad...");
            if (onReward != null) {
                onReward.run();
            }
        }

        @Override
        public void showUnityRewardedAd(Runnable onReward) {
            Window.alert("[GWT] Simulating Unity rewarded ad...");
            if (onReward != null) {
                onReward.run();
            }
        }
    }
}
