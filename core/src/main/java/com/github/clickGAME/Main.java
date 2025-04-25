package com.github.clickGAME;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
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

public class Main extends ApplicationAdapter {
    private AdController adController;
    private static float VIRTUAL_WIDTH;
    private static float VIRTUAL_HEIGHT;

    private SpriteBatch batch;
    private Texture image;
    private Texture imageActive;
    private Texture foodIcon;
    private Texture[] walkTextures;
    private Sprite sprite;
    private Sprite sprite1, sprite2, sprite3;
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

        walkTextures = new Texture[] {
                new Texture("cat_walk1.png"),
                new Texture("cat_walk2.png"),
                new Texture("cat_walk3.png")
        };

        sprite = new Sprite(image);
        sprite.setPosition(VIRTUAL_WIDTH / 2f - 75, VIRTUAL_HEIGHT / 2f - 75);
        sprite.setSize(150, 150);

        sprite1 = new Sprite(image);
        sprite1.setPosition(250, 0);
        sprite1.setSize(150, 150);

        sprite2 = new Sprite(foodIcon);
        sprite2.setPosition(0, 250);
        sprite2.setSize(150, 150);

        sprite3 = new Sprite(image);
        sprite3.setPosition(250, 250);
        sprite3.setSize(150, 150);

        font = new BitmapFont();
        font.getData().setScale(2.0f);
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        score = 0;
        food = 3;
        touchPos = new Vector3();
        direction = new Vector2();

        camera = new OrthographicCamera();
        viewport = new ScreenViewport(camera);

        UIcamera = new OrthographicCamera();
        UIviewport = new ExtendViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, UIcamera);

        Gdx.app.log("Main", "Application created");
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        moveTimer -= delta;

        if (!isMoving && moveTimer <= 0) {
            if (MathUtils.randomBoolean(0.2f)) { // 20% 概率开始移动
                direction.set(MathUtils.random(-1f, 1f), MathUtils.random(-1f, 1f)).nor();
                moveDuration = MathUtils.random(1f, 2f);
                isMoving = true;
                walkFrameIndex = 0;
                sprite.setTexture(walkTextures[walkFrameIndex]);
            }
            moveTimer = MathUtils.random(2f, 4f); // 下一次检查移动的等待时间
        }

        if (isMoving) {
            sprite.translate(direction.x * moveSpeed * delta, direction.y * moveSpeed * delta);
            moveDuration -= delta;

            // 保持在屏幕内
            if (sprite.getX() < 0 || sprite.getX() > VIRTUAL_WIDTH - sprite.getWidth())
                direction.x *= -1;
            if (sprite.getY() < 0 || sprite.getY() > VIRTUAL_HEIGHT - sprite.getHeight())
                direction.y *= -1;

            // 播放移动动画帧
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
                    Gdx.app.log("Main", "Clicked cat! Score: " + score);
                }
            } else if (sprite1.getBoundingRectangle().contains(touchPos.x, touchPos.y)) {
                if (!spriteActive) {
                    sprite1.setTexture(imageActive);
                    spriteActive = true;
                    adController.showRewardedAd(() -> {
                        score += 500;
                        Gdx.app.log("Main", "Reward received! Score: " + score);
                    });
                }
            } else if (sprite2.getBoundingRectangle().contains(touchPos.x, touchPos.y)) {
                if (!spriteActive && score >= 100) {
                    sprite2.setTexture(foodIcon);
                    spriteActive = true;
                    score -= 100;
                    food += 1;
                    Gdx.app.log("Main", "Bought food! Food: " + food + " Score: " + score);
                }
            }
        } else if (spriteActive) {
            sprite.setTexture(image);
            sprite1.setTexture(image);
            sprite2.setTexture(foodIcon);
            spriteActive = false;
        }

        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        viewport.apply();
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        sprite1.draw(batch);
        sprite2.draw(batch);
        sprite3.draw(batch);
        sprite.draw(batch);
        font.draw(batch, "+1", sprite.getX() + sprite.getWidth() / 2 - 10, sprite.getY() + sprite.getHeight() + 30);
        font.draw(batch, "+500 (Ad)", sprite1.getX() + sprite1.getWidth() / 2 - 40,
                sprite1.getY() + sprite1.getHeight() + 30);
        font.draw(batch, "Buy Food", sprite2.getX() + sprite2.getWidth() / 2 - 40,
                sprite2.getY() + sprite2.getHeight() + 30);
        batch.end();

        UIviewport.apply();
        UIcamera.update();
        batch.setProjectionMatrix(UIcamera.combined);
        batch.begin();
        font.draw(batch, "Score: " + score, 0, VIRTUAL_HEIGHT);
        font.draw(batch, "Food: " + food, 0, VIRTUAL_HEIGHT - 40);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        Gdx.app.log("Main", "Resized to: " + width + "x" + height);
        viewport.update(width, height, true);
        camera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        UIviewport.update(width, height, true);
        UIcamera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
    }

    @Override
    public void dispose() {
        Gdx.app.log("Main", "Disposing resources");
        batch.dispose();
        image.dispose();
        imageActive.dispose();
        foodIcon.dispose();
        for (Texture t : walkTextures)
            t.dispose();
        font.dispose();
        Gdx.app.log("Main", "Resources disposed");
    }
}
