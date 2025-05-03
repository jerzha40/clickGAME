package com.github.clickGAME;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.List;

public class Main extends ApplicationAdapter {
    private void saveProgress() {
        prefs.putInteger("score", score);
        prefs.putInteger("food", food);
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
    private Texture foodIcon;
    private Texture watchAdIcon;
    private Texture unityAdIcon;
    private Texture waterIcon;
    private Texture[] walkTextures;
    private Sprite sprite;
    private BitmapFont font;
    private int score;
    private int food;
    private Vector3 touchPos;
    private OrthographicCamera camera;
    private Viewport viewport;
    private OrthographicCamera UIcamera;
    private Viewport UIviewport;
    private boolean spriteActive = false;

    private Vector2 direction;
    private float moveTimer = 0;
    private float walkFrameTimer = 0;
    private int walkFrameIndex = 0;
    private float moveSpeed = 50f;
    private boolean isMoving = false;
    private float moveDuration = 0;

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

        image = new Texture("cat_idle.png");
        imageActive = new Texture("cat_active.png");
        foodIcon = new Texture("food_icon.png");
        watchAdIcon = new Texture("watch_ad.png");
        unityAdIcon = new Texture("unity_ad.png");
        waterIcon = new Texture("water_icon.png");

        walkTextures = new Texture[] {
                new Texture("cat_walk1.png"),
                new Texture("cat_walk2.png"),
                new Texture("cat_walk3.png")
        };

        font = new BitmapFont();
        font.getData().setScale(2.0f);
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        touchPos = new Vector3();
        direction = new Vector2();

        camera = new OrthographicCamera();
        viewport = new ScreenViewport(camera);

        UIcamera = new OrthographicCamera();
        UIviewport = new ExtendViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, UIcamera);

        prefs = Gdx.app.getPreferences("ClickCatSave");

        score = prefs.getInteger("score", 0);
        food = prefs.getInteger("food", 3);
        float catX = prefs.getFloat("cat_x", VIRTUAL_WIDTH / 2f - 75);
        float catY = prefs.getFloat("cat_y", VIRTUAL_HEIGHT / 2f - 75);
        cat = new Cat(catX, catY);

        shopItems = new ArrayList<>();

        ShopItem adMobItem = new ShopItem(ShopItem.Type.ADS, 0, watchAdIcon, 300, 0);
        ShopItem unityAdItem = new ShopItem(ShopItem.Type.ADS, 0, unityAdIcon, 500, 0);
        ShopItem foodItem = new ShopItem(ShopItem.Type.FOOD, 100, foodIcon, 0, 0);
        ShopItem waterItem = new ShopItem(ShopItem.Type.WATER, 0, waterIcon, 125, 0);

        // 调用接口判断是否启用（跨平台安全）
        adMobItem.setEnabled(adController instanceof AdController && ((AdController) adController).isAdMobReady());
        unityAdItem.setEnabled(adController instanceof AdController && ((AdController) adController).isUnityAdReady());

        shopItems.add(adMobItem);
        shopItems.add(foodItem);
        shopItems.add(unityAdItem);
        shopItems.add(waterItem);

        Gdx.app.log("Main", "Application created");
    }

    @Override
    public void render() {
        // 实时检测广告是否加载完成，启用广告按钮
        for (ShopItem item : shopItems) {
            if (item.getType() == ShopItem.Type.ADS) {
                if (item.getSprite().getTexture() == watchAdIcon) {
                    boolean ready = adController != null && adController.isAdMobReady();
                    if (ready && !item.isEnabled())
                        item.setEnabled(true);
                } else if (item.getSprite().getTexture() == unityAdIcon) {
                    boolean ready = adController != null && adController.isUnityAdReady();
                    if (ready && !item.isEnabled())
                        item.setEnabled(true);
                }
            }
        }
        float delta = Gdx.graphics.getDeltaTime();

        cat.update(delta);
        moveTimer -= delta;

        if (!isMoving && moveTimer <= 0) {
            if (MathUtils.randomBoolean(0.2f)) {
                direction.set(MathUtils.random(-1f, 1f), MathUtils.random(-1f, 1f)).nor();
                moveDuration = MathUtils.random(1f, 2f);
                isMoving = true;
                walkFrameIndex = 0;
                cat.getSprite().setTexture(walkTextures[walkFrameIndex]);
            }
            moveTimer = MathUtils.random(2f, 4f);
        }

        if (isMoving) {
            cat.getSprite().translate(direction.x * moveSpeed * delta, direction.y * moveSpeed * delta);
            cat.setPosition(cat.getSprite().getX(), cat.getSprite().getY());
            moveDuration -= delta;

            if (cat.getSprite().getX() < 0 || cat.getSprite().getX() > VIRTUAL_WIDTH - cat.getSprite().getWidth())
                direction.x *= -1;
            if (cat.getSprite().getY() < 0 || cat.getSprite().getY() > VIRTUAL_HEIGHT - cat.getSprite().getHeight())
                direction.y *= -1;

            walkFrameTimer += delta;
            if (walkFrameTimer > 0.2f) {
                walkFrameIndex = (walkFrameIndex + 1) % walkTextures.length;
                cat.getSprite().setTexture(walkTextures[walkFrameIndex]);
                walkFrameTimer = 0;
            }

            if (moveDuration <= 0) {
                isMoving = false;
                cat.getSprite().setTexture(image);
            }
        }

        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);

            if (cat.getSprite().getBoundingRectangle().contains(touchPos.x, touchPos.y)) {
                if (!spriteActive) {
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
                                    if (score >= item.getPrice()) {
                                        score -= item.getPrice();
                                        food++;
                                        cat.feed();
                                        saveProgress();
                                    }
                                    break;
                                case MEDICINE:
                                    adController.showUnityRewardedAd(() -> {
                                        score += 1000;
                                        cat.giveMedicine();
                                        saveProgress();
                                    });
                                    break;
                                case TOY:
                                    adController.showAdMobRewardedAd(() -> {
                                        score += 500;
                                        cat.play();
                                        saveProgress();
                                    });
                                    break;
                                case WATER:
                                    if (score >= item.getPrice()) {
                                        score -= item.getPrice();
                                        cat.drink();
                                        saveProgress();
                                    }
                                    break;
                                case ADS:
                                    if (item.getSprite().getTexture() == watchAdIcon) {
                                        adController.showAdMobRewardedAd(() -> {
                                            score += 500;
                                            cat.play();
                                            saveProgress();
                                        });
                                    } else if (item.getSprite().getTexture() == unityAdIcon) {
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
        } else if (spriteActive) {
            cat.getSprite().setTexture(image);
            spriteActive = false;
        }

        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        viewport.apply();
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (ShopItem item : shopItems) {
            item.getSprite().draw(batch);
        }
        cat.getSprite().draw(batch);
        for (ShopItem item : shopItems) {
            float textX = item.getSprite().getX() + item.getSprite().getWidth() / 2 - 40;
            float textY = item.getSprite().getY() + item.getSprite().getHeight() + 30;
            switch (item.getType()) {
                case TOY:
                    font.draw(batch, "+500 (AdMob)", textX, textY);
                    break;
                case FOOD:
                    font.draw(batch, "Buy Food", textX, textY);
                    break;
                case MEDICINE:
                    font.draw(batch, "+1000 (Unity Ad)", textX, textY);
                    break;
                case WATER:
                    font.draw(batch, "Buy Water", textX, textY);
                    break;
                case ADS:
                    if (item.getSprite().getTexture() == watchAdIcon) {
                        font.draw(batch, "+500 (AdMob)", textX, textY);
                    } else if (item.getSprite().getTexture() == unityAdIcon) {
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
        font.draw(batch, "Food: " + food, 0, VIRTUAL_HEIGHT - 40);
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
        foodIcon.dispose();
        watchAdIcon.dispose();
        unityAdIcon.dispose();
        waterIcon.dispose();
        for (Texture t : walkTextures)
            t.dispose();
        font.dispose();
        saveProgress();
    }
}
