package com.github.clickGAME;

import java.util.Random;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

public class Cat {

    public static Cat load(CatConfig config, float x, float y) {
        Cat cat = new Cat(x, y);
        cat.setStageTextures(
                new Texture(config.babyTexture),
                new Texture(config.juniorTexture),
                new Texture(config.adultTexture),
                new Texture(config.fullTexture));
        cat.setFeedbackTexture(new Texture(config.heartIcon));
        cat.setMeowSound(Gdx.audio.newSound(Gdx.files.internal(config.meowSound)));
        return cat;
    }

    private Random random = new Random();
    private Sprite sprite;
    private float health = 100f;
    private float happiness = 100f;
    private float fullness = 100f;
    private float thirst = 100f;
    private float growth = 0f;

    private Array<FloatingIcon> floatingIcons = new Array<>();
    private Texture feedbackTexture;
    private Sound meowSound;

    public enum Stage {
        BABY, JUNIOR, ADULT, FULL
    }

    private Stage stage = Stage.BABY;
    private Texture babyTexture;
    private Texture juniorTexture;
    private Texture adultTexture;
    private Texture fullTexture;

    public Cat(float x, float y) {
        sprite = new Sprite(new Texture("cat_idle.png"));
        sprite.setPosition(x, y);
        sprite.setSize(150, 150);
    }

    public void setStageTextures(Texture baby, Texture junior, Texture adult, Texture full) {
        this.babyTexture = baby;
        this.juniorTexture = junior;
        this.adultTexture = adult;
        this.fullTexture = full;
        updateTextureForStage();
    }

    public void updateTextureForStage() {
        switch (stage) {
            case BABY:
                sprite.setTexture(babyTexture);
                break;
            case JUNIOR:
                sprite.setTexture(juniorTexture);
                break;
            case ADULT:
                sprite.setTexture(adultTexture);
                break;
            case FULL:
                sprite.setTexture(fullTexture);
                break;
        }
    }

    private void updateStage() {
        Stage newStage;
        if (growth < 100)
            newStage = Stage.BABY;
        else if (growth < 300)
            newStage = Stage.JUNIOR;
        else if (growth < 600)
            newStage = Stage.ADULT;
        else
            newStage = Stage.FULL;

        if (newStage != stage) {
            stage = newStage;
            updateTextureForStage();
        }
    }

    public void feed() {
        float chance = thirst / 100f;
        if (random.nextFloat() < chance * chance) {
            fullness = Math.min(100, fullness + 30);
            grow(1.5f);
        }
        // 否则这次喂食没有生效
    }

    public void drink() {
        thirst = Math.min(100f, thirst + 50f);
        grow(1f);
    }

    public void giveMedicine() {
        health = Math.min(100f, health + 50f);
        grow(0.5f);
    }

    public void play() {
        happiness = Math.min(100f, happiness + 50f);
        grow(1.0f);
    }

    private void grow(float amount) {
        growth += amount;
        updateStage();
    }

    public void update(float delta) {
        for (FloatingIcon icon : floatingIcons)
            icon.update(delta);
        for (int i = floatingIcons.size - 1; i >= 0; i--) {
            if (floatingIcons.get(i).isDead()) {
                floatingIcons.removeIndex(i);
            }
        }
        fullness = Math.max(0f, fullness - delta * 0.5f);
        thirst = Math.max(0f, thirst - delta * 0.5f);
        health = Math.max(0f, health - (fullness < 20f || thirst < 20f ? delta * 2f : 0));
        happiness = Math.max(0f, happiness - delta * 0.3f);
    }

    public void render(SpriteBatch batch) {
        sprite.draw(batch);
        for (FloatingIcon icon : floatingIcons)
            icon.render(batch);
    }

    public void onTouched() {
        if (feedbackTexture != null)
            floatingIcons.add(new FloatingIcon(feedbackTexture, getX() + 60, getY() + 100));
    }

    public void setFeedbackTexture(Texture texture) {
        this.feedbackTexture = texture;
    }

    public void setMeowSound(Sound sound) {
        this.meowSound = sound;
    }

    public void meow() {
        if (meowSound != null)
            meowSound.play();
    }

    public Sprite getSprite() {
        return sprite;
    }

    public float getX() {
        return sprite.getX();
    }

    public float getY() {
        return sprite.getY();
    }

    public void setPosition(float x, float y) {
        sprite.setPosition(x, y);
    }

    public float getHealth() {
        return health;
    }

    public float getHappiness() {
        return happiness;
    }

    public float getFullness() {
        return fullness;
    }

    public float getThirst() {
        return thirst;
    }

    public float getGrowth() {
        return growth;
    }

    public void dispose() {
        if (sprite != null && sprite.getTexture() != null)
            sprite.getTexture().dispose();
        if (feedbackTexture != null)
            feedbackTexture.dispose();
        if (meowSound != null)
            meowSound.dispose();
        if (babyTexture != null)
            babyTexture.dispose();
        if (juniorTexture != null)
            juniorTexture.dispose();
        if (adultTexture != null)
            adultTexture.dispose();
        if (fullTexture != null)
            fullTexture.dispose();
    }

    public Stage getStage() {
        return stage;
    }
}
