package com.github.clickGAME;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.List;

public class Main extends ApplicationAdapter {
    private void handlePurchase(ShopItem item, Runnable onSuccess) {
        if (score >= item.getPrice()) {
            item.onClicked();
            score -= item.getPrice();
            onSuccess.run();
            saveProgress();
        } else {
            item.onFailed();
        }
    }

    private void saveProgress() {
        prefs.putInteger("score", score);
        prefs.putFloat("cat_x", cat.getX());
        prefs.putFloat("cat_y", cat.getY());
        prefs.flush();
    }

    private AdController adController;
    private static float VIRTUAL_WIDTH;
    private static float VIRTUAL_HEIGHT;

    private SpriteBatch batch;
    private Texture image;
    private Texture imageActive;
    private BitmapFont font;
    private int score;
    private Vector3 touchPos;
    private OrthographicCamera camera;
    private Viewport viewport;
    private OrthographicCamera UIcamera;
    private Viewport UIviewport;
    private boolean spriteActive = false;

    private Preferences prefs;
    private Cat cat;
    private List<ShopItem> shopItems;

    public Main(AdController adController) {
        this.adController = adController;
    }

    @Override
    public void create() {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        VIRTUAL_WIDTH = Math.min(width, height);
        VIRTUAL_HEIGHT = Math.min(width, height);

        batch = new SpriteBatch();

        image = new Texture("cat_baby.png");
        imageActive = new Texture("cat_active.png");

        font = new BitmapFont();
        font.getData().setScale(2.0f);
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        touchPos = new Vector3();

        camera = new OrthographicCamera();
        viewport = new ScreenViewport(camera);
        UIcamera = new OrthographicCamera();
        UIviewport = new ExtendViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, UIcamera);

        prefs = Gdx.app.getPreferences("ClickCatSave");
        score = prefs.getInteger("score", 0);
        float catX = prefs.getFloat("cat_x", VIRTUAL_WIDTH / 2f - 75);
        float catY = prefs.getFloat("cat_y", VIRTUAL_HEIGHT / 2f - 75);
        Json json = new Json();
        CatConfig catConfig = json.fromJson(CatConfig.class, Gdx.files.internal("cat_config.json"));
        cat = Cat.load(catConfig, catX, catY);

        FileHandle file = Gdx.files.internal("shop_items.json");
        ShopItemConfig[] configs = json.fromJson(ShopItemConfig[].class, file);
        shopItems = new ArrayList<>();

        for (ShopItemConfig config : configs) {
            ShopItem.Type type = ShopItem.Type.valueOf(config.type);
            Texture icon = new Texture(config.icon);
            ShopItem item = new ShopItem(type, config.price, icon, config.x, config.y);
            item.setIconPath(config.icon);
            item.setFeedbackTexture(new Texture("coin_icon.png"));
            item.setFailureTexture(new Texture("fail_icon.png"));
            if (type == ShopItem.Type.ADS) {
                boolean ready = config.icon.contains("unity") ? adController.isUnityAdReady()
                        : adController.isAdMobReady();
                item.setEnabled(ready);
            }
            shopItems.add(item);
        }

        Gdx.app.log("Main", "Application created");
    }

    @Override
    public void render() {
        // 实时检测广告是否加载完成，启用广告按钮
        for (ShopItem item : shopItems) {
            if (item.getType() == ShopItem.Type.ADS) {
                if (item.getIconPath().contains("watch_ad")) {
                    boolean ready = adController != null && adController.isAdMobReady();
                    if (ready && !item.isEnabled())
                        item.setEnabled(true);
                } else if (item.getIconPath().contains("unity_ad")) {
                    boolean ready = adController != null && adController.isUnityAdReady();
                    if (ready && !item.isEnabled())
                        item.setEnabled(true);
                }
            }
        }
        float delta = Gdx.graphics.getDeltaTime();

        cat.update(delta);
        for (ShopItem item : shopItems)
            item.update(delta);

        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);

            if (cat.getSprite().getBoundingRectangle().contains(touchPos.x, touchPos.y)) {
                if (!spriteActive) {
                    cat.onTouched();
                    cat.getSprite().setTexture(imageActive);
                    spriteActive = true;
                    score += 1;
                    saveProgress();
                }
            } else {
                for (ShopItem item : shopItems) {
                    if (item.isTouched(touchPos.x, touchPos.y)) {
                        if (!spriteActive) {
                            spriteActive = true;
                            switch (item.getType()) {
                                case FOOD:
                                    handlePurchase(item, () -> {
                                        cat.feed();
                                    });
                                    break;
                                case MEDICINE:
                                    handlePurchase(item, () -> {
                                        cat.giveMedicine();
                                    });
                                    break;
                                case TOY:
                                    handlePurchase(item, () -> {
                                        cat.play();
                                    });
                                    break;
                                case WATER:
                                    handlePurchase(item, () -> {
                                        cat.drink();
                                    });
                                    break;
                                case ADS:
                                    if (item.getIconPath().contains("watch_ad")) {
                                        adController.showAdMobRewardedAd(() -> {
                                            score += 500;
                                            cat.play();
                                            saveProgress();
                                        });
                                    } else if (item.getIconPath().contains("unity_ad")) {
                                        adController.showUnityRewardedAd(() -> {
                                            score += 1000;
                                            cat.giveMedicine();
                                            saveProgress();
                                        });
                                    }
                                    break;
                            }
                        }
                    }
                }
            }
        } else if (spriteActive)

        {
            cat.updateTextureForStage();
            spriteActive = false;
        }

        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        viewport.apply();
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (

        ShopItem item : shopItems) {
            item.render(batch);
        }
        cat.render(batch);
        for (ShopItem item : shopItems) {
            float textX = item.getSprite().getX() + item.getSprite().getWidth() / 2 - 40;
            float textY = item.getSprite().getY() + item.getSprite().getHeight() + 30;
            switch (item.getType()) {
                case TOY:
                    font.draw(batch, "Buy Toy", textX, textY);
                    break;
                case FOOD:
                    font.draw(batch, "Buy Food", textX, textY);
                    break;
                case MEDICINE:
                    font.draw(batch, "Buy Medicine", textX, textY);
                    break;
                case WATER:
                    font.draw(batch, "Buy Water", textX, textY);
                    break;
                case ADS:
                    if (item.getIconPath().contains("watch_ad")) {
                        font.draw(batch, "+500 (AdMob)", textX, textY);
                    } else if (item.getIconPath().contains("unity_ad")) {
                        font.draw(batch, "+1000 (Unity Ad)", textX, textY);
                    }
                    break;
            }
        }
        batch.end();

        UIviewport.apply();
        UIcamera.update();
        batch.setProjectionMatrix(UIcamera.combined);
        batch.begin();
        font.draw(batch, "Score: " + score, 0, VIRTUAL_HEIGHT);
        font.draw(batch, String.format("Growth: %.0f", cat.getGrowth()), 0, VIRTUAL_HEIGHT - 240);
        font.draw(batch, "Stage: " + cat.getStage().name(), 0, VIRTUAL_HEIGHT - 280);
        font.draw(batch, String.format("Health: %.0f", cat.getHealth()), 0, VIRTUAL_HEIGHT - 80);
        font.draw(batch, String.format("Happiness: %.0f", cat.getHappiness()), 0, VIRTUAL_HEIGHT - 120);
        font.draw(batch, String.format("Fullness: %.0f", cat.getFullness()), 0, VIRTUAL_HEIGHT - 160);
        font.draw(batch, String.format("Thirst: %.0f", cat.getThirst()), 0, VIRTUAL_HEIGHT - 200);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        camera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        UIviewport.update(width, height, true);
        UIcamera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
    }

    @Override
    public void dispose() {
        batch.dispose();
        image.dispose();
        imageActive.dispose();
        font.dispose();
        if (cat != null)
            cat.dispose();
        if (shopItems != null) {
            for (ShopItem item : shopItems) {
                item.dispose();
            }
        }
        saveProgress();
    }
}
