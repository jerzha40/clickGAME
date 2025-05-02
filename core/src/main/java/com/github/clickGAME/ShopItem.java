package com.github.clickGAME;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class ShopItem {
    public enum Type {
        FOOD, WATER, MEDICINE, TOY
    }

    private Type type;
    private int price;
    private Texture icon;
    private Sprite sprite;

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
        return sprite.getBoundingRectangle().contains(x, y);
    }
}
