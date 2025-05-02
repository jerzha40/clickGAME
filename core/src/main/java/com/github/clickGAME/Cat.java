package com.github.clickGAME;

public class Cat {
    private float fullness; // 饱食度：0~100
    private float thirst; // 口渴度：0~100
    private float health; // 健康状态：0~100
    private float happiness; // 开心值：0~100

    private float timeSinceLastUpdate = 0;

    public Cat() {
        fullness = 100;
        thirst = 100;
        health = 100;
        happiness = 100;
    }

    public void update(float delta) {
        timeSinceLastUpdate += delta;

        if (timeSinceLastUpdate >= 5f) {
            fullness = Math.max(0, fullness - 1);
            thirst = Math.max(0, thirst - 2);

            if (fullness < 20 || thirst < 20)
                health = Math.max(0, health - 1);

            happiness = health * 0.8f;

            timeSinceLastUpdate = 0;
        }
    }

    public void feed() {
        fullness = Math.min(100, fullness + 30);
    }

    public void drink() {
        thirst = Math.min(100, thirst + 40);
    }

    public void giveMedicine() {
        health = Math.min(100, health + 20);
    }

    public void play() {
        happiness = Math.min(100, happiness + 15);
    }

    public float getFullness() {
        return fullness;
    }

    public float getThirst() {
        return thirst;
    }

    public float getHealth() {
        return health;
    }

    public float getHappiness() {
        return happiness;
    }
}
