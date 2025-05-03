package com.github.clickGAME;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class ShopItem {
    public enum Type {
        FOOD, WATER, MEDICINE, TOY, ADS
    }

    private Type type;
    private int price;
    private Texture icon;
    private Sprite sprite;
    private boolean enabled = true;

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
            sprite.setColor(1f, 1f, 1f, 1f); // 正常颜色
        } else {
            sprite.setColor(0.5f, 0.5f, 0.5f, 1f); // 变灰
        }
    }
}
