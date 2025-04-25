package com.github.clickGAME;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Main extends ApplicationAdapter {
    private static final float VIRTUAL_WIDTH = 500;
    private static final float VIRTUAL_HEIGHT = 500;

    private SpriteBatch batch;
    private Texture image;
    private Texture imageActive;
    private Sprite sprite;
    private Sprite sprite1, sprite2, sprite3;
    private BitmapFont font;
    private int score;
    private Vector3 touchPos;
    private OrthographicCamera camera;
    private Viewport viewport;
    private OrthographicCamera UIcamera;
    private Viewport UIviewport;
    private boolean spriteActive = false;

    @Override
    public void create() {
        batch = new SpriteBatch();

        image = new Texture("cat.png");
        imageActive = new Texture("cat_active.png"); // 激活状态图片

        sprite = new Sprite(image);
        sprite.setPosition(0, 0);
        sprite.setSize(150, 150);

        sprite1 = new Sprite(image);
        sprite1.setPosition(250, 0);
        sprite1.setSize(150, 150);

        sprite2 = new Sprite(image);
        sprite2.setPosition(0, 250);
        sprite2.setSize(150, 150);

        sprite3 = new Sprite(image);
        sprite3.setPosition(250, 250);
        sprite3.setSize(150, 150);

        font = new BitmapFont();
        font.getData().setScale(2.0f);
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        score = 0;
        touchPos = new Vector3();

        camera = new OrthographicCamera();
        viewport = new ScreenViewport(camera);

        UIcamera = new OrthographicCamera();
        UIviewport = new ExtendViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, UIcamera);

        Gdx.app.log("Main", "Application created");
    }

    @Override
    public void render() {
        if (Gdx.input.isTouched()) {
            Gdx.app.log("Main", "Raw touch: (" + Gdx.input.getX() + ", " + Gdx.input.getY() + ")");
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            Gdx.app.log("Main", "World touch: (" + touchPos.x + ", " + touchPos.y + ")");

            if (sprite.getBoundingRectangle().contains(touchPos.x, touchPos.y)) {
                if (!spriteActive) {
                    sprite.setTexture(imageActive);
                    spriteActive = true;
                    score++;
                    Gdx.app.log("Main", "Hit! Score: " + score);
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
        sprite1.draw(batch);
        sprite2.draw(batch);
        sprite3.draw(batch);
        sprite.draw(batch);
        batch.end();

        UIviewport.apply();
        UIcamera.update();
        batch.setProjectionMatrix(UIcamera.combined);
        batch.begin();
        font.draw(batch, "Score: " + score, 0, VIRTUAL_HEIGHT);
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
        font.dispose();
        Gdx.app.log("Main", "Resources disposed");
    }
}
