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
        prefs.putFloat("cat_x", sprite.getX());
        prefs.putFloat("cat_y", sprite.getY());
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

        walkTextures = new Texture[] {
                new Texture("cat_walk1.png"),
                new Texture("cat_walk2.png"),
                new Texture("cat_walk3.png")
        };

        sprite = new Sprite(image);
        sprite.setSize(150, 150);

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
        sprite.setPosition(catX, catY);

        cat = new Cat();

        shopItems = new ArrayList<>();
        shopItems.add(new ShopItem(ShopItem.Type.TOY, 0, watchAdIcon, 250, 0));
        shopItems.add(new ShopItem(ShopItem.Type.FOOD, 100, foodIcon, 0, 250));
        shopItems.add(new ShopItem(ShopItem.Type.MEDICINE, 0, unityAdIcon, 500, 0));

        Gdx.app.log("Main", "Application created");
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        cat.update(delta);
        moveTimer -= delta;

        if (!isMoving && moveTimer <= 0) {
            if (MathUtils.randomBoolean(0.2f)) {
                direction.set(MathUtils.random(-1f, 1f), MathUtils.random(-1f, 1f)).nor();
                moveDuration = MathUtils.random(1f, 2f);
                isMoving = true;
                walkFrameIndex = 0;
                sprite.setTexture(walkTextures[walkFrameIndex]);
            }
            moveTimer = MathUtils.random(2f, 4f);
        }

        if (isMoving) {
            sprite.translate(direction.x * moveSpeed * delta, direction.y * moveSpeed * delta);
            moveDuration -= delta;

            if (sprite.getX() < 0 || sprite.getX() > VIRTUAL_WIDTH - sprite.getWidth())
                direction.x *= -1;
            if (sprite.getY() < 0 || sprite.getY() > VIRTUAL_HEIGHT - sprite.getHeight())
                direction.y *= -1;

            walkFrameTimer += delta;
            if (walkFrameTimer > 0.2f) {
                walkFrameIndex = (walkFrameIndex + 1) % walkTextures.length;
                sprite.setTexture(walkTextures[walkFrameIndex]);
                walkFrameTimer = 0;
            }

            if (moveDuration <= 0) {
                isMoving = false;
                sprite.setTexture(image);
            }
        }

        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);

            if (sprite.getBoundingRectangle().contains(touchPos.x, touchPos.y)) {
                if (!spriteActive) {
                    sprite.setTexture(imageActive);
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
                            }
                        }
                    }
                }
            }
        } else if (spriteActive) {
            sprite.setTexture(image);
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
        sprite.draw(batch);
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
        for (Texture t : walkTextures)
            t.dispose();
        font.dispose();
        saveProgress();
    }
}
