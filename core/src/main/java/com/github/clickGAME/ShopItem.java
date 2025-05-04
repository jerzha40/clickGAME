package com.github.clickGAME;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

public class ShopItem {
    public enum Type {
        FOOD, WATER, MEDICINE, TOY, ADS
    }

    private Type type;
    private int price;
    private Texture icon;
    private Sprite sprite;
    private boolean enabled = true;

    private Array<FloatingIcon> floatingIcons = new Array<>();
    private Texture feedbackTexture;

    public ShopItem(Type type, int price, Texture icon, float x, float y) {
        this.type = type;
        this.price = price;
        this.icon = icon;
        this.sprite = new Sprite(icon);
        this.sprite.setPosition(x, y);
        this.sprite.setSize(150, 150);
    }

    public Type getType() {
        return type;
    }

    public int getPrice() {
        return price;
    }

    public void update(float delta) {
        for (FloatingIcon icon : floatingIcons) {
            icon.update(delta);
        }
        for (int i = floatingIcons.size - 1; i >= 0; i--) {
            if (floatingIcons.get(i).isDead()) {
                floatingIcons.removeIndex(i);
            }
        }
    }

    public void render(SpriteBatch batch) {
        sprite.draw(batch);
        for (FloatingIcon icon : floatingIcons) {
            icon.render(batch);
        }
    }

    public void onClicked() {
        if (feedbackTexture != null) {
            floatingIcons.add(new FloatingIcon(feedbackTexture, sprite.getX() + 50, sprite.getY() + 100));
        }
    }

    public void setFeedbackTexture(Texture texture) {
        this.feedbackTexture = texture;
    }

    public Sprite getSprite() {
        return sprite;
    }

    public boolean isTouched(float x, float y) {
        return enabled && sprite.getBoundingRectangle().contains(x, y);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            sprite.setColor(1f, 1f, 1f, 1f);
        } else {
            sprite.setColor(0.5f, 0.5f, 0.5f, 1f);
        }
    }
}
