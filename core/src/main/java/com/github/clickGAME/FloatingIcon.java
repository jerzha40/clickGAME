package com.github.clickGAME;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class FloatingIcon {
    private Texture texture;
    private float x, y;
    private float alpha = 1f;
    private float life = 1f;

    public FloatingIcon(Texture texture, float x, float y) {
        this.texture = texture;
        this.x = x;
        this.y = y;
    }

    public void update(float delta) {
        y += delta * 40;
        alpha -= delta;
        life -= delta;
    }

    public boolean isDead() {
        return life <= 0;
    }

    public void render(SpriteBatch batch) {
        batch.setColor(1, 1, 1, Math.max(0, alpha));
        batch.draw(texture, x, y, 32, 32);
        batch.setColor(1, 1, 1, 1);
    }
}
