package com.github.clickGAME.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.github.clickGAME.Main;
import com.github.clickGAME.AdController;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired())
            return; // This handles macOS support and helps on Windows.
        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        return new Lwjgl3Application(new Main(new DesktopAdController()), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("clickGAME");
        configuration.useVsync(true);
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);
        configuration.setWindowedMode(1206, 540);
        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
        return configuration;
    }

    static class DesktopAdController implements AdController {
        @Override
        public void showAdMobRewardedAd(Runnable onReward) {
            System.out.println("[Desktop] Simulating AdMob rewarded ad...");
            simulateAd(onReward);
        }

        @Override
        public void showUnityRewardedAd(Runnable onReward) {
            System.out.println("[Desktop] Simulating Unity rewarded ad...");
            simulateAd(onReward);
        }

        @Override
        public boolean isAdMobReady() {
            return false;
        }

        @Override
        public boolean isUnityAdReady() {
            return false;
        }

        private void simulateAd(Runnable onReward) {
            new Thread(() -> {
                try {
                    Thread.sleep(2000); // Simulate ad duration
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("[Desktop] Reward granted!");
                if (onReward != null)
                    onReward.run();
            }).start();
        }
    }
}
