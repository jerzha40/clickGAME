package com.github.clickGAME;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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

    private SpriteBatch batch; // 用于绘制2D图形的批处理器
    private Texture image; // 游戏中显示的图片资源
    private BitmapFont font; // 用于显示得分的字体
    private int score; // 当前得分
    private Vector3 touchPos; // 触摸位置，三维向量便于与摄像机转换结合使用
    private OrthographicCamera camera; // 摄像机，用于管理视图坐标到屏幕坐标的转换
    private Viewport viewport; // 视口，用于管理摄像机的适配与缩放
    private OrthographicCamera UIcamera; // 摄像机，用于管理视图坐标到屏幕坐标的转换
    private Viewport UIviewport; // 视口，用于管理摄像机的适配与缩放

    @Override
    public void create() {
        batch = new SpriteBatch();
        image = new Texture("libgdx.png");
        font = new BitmapFont();
        score = 0;
        touchPos = new Vector3();
        camera = new OrthographicCamera();
        viewport = new ScreenViewport(camera); // 使用ScreenViewport进行屏幕适配

        UIcamera = new OrthographicCamera();
        UIviewport = new ExtendViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, UIcamera); // 使用ExtendViewport进行虚拟分辨率适配
        Gdx.app.log("Main", "Application created");
    }

    @Override
    public void render() {

        if (Gdx.input.justTouched()) { // 检测是否有点击
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0); // 获取点击位置
            camera.unproject(touchPos); // 将屏幕坐标转换为世界坐标
            Gdx.app.log("Main", "Touched at: (" + touchPos.x + ", " + touchPos.y + ")");

            // 判断点击位置是否在图片范围内（图片绘制在140,210，尺寸为256x256）
            if (touchPos.x >= 140 && touchPos.x <= 396 &&
                    touchPos.y >= 210 && touchPos.y <= 466) {
                score++; // 命中图片，得分加一
                Gdx.app.log("Main", "Hit! Score: " + score);
            } else {
                Gdx.app.log("Main", "Missed the target area");
            }
        }

        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f); // 清屏背景色

        viewport.apply(); // 应用视口设置
        camera.update();
        batch.setProjectionMatrix(camera.combined); // 使用摄像机更新后的矩阵作为渲染基准

        batch.begin();
        batch.draw(image, 0, 0); // 绘制图片
        batch.draw(image, 0, 500); // 绘制图片
        batch.draw(image, 500, 500); // 绘制图片
        batch.draw(image, 500, 0); // 绘制图片
        batch.end();

        UIviewport.apply(); // 应用视口设置
        UIcamera.update();
        batch.setProjectionMatrix(UIcamera.combined); // 使用摄像机更新后的矩阵作为渲染基准
        batch.begin();
        font.draw(batch, "Score: " + score, 0, VIRTUAL_HEIGHT); // 显示得分
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true); // 更新视口并保持相机居中
        camera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        UIviewport.update(width, height, true); // 更新视口并保持相机居中
        UIcamera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
    }

    @Override
    public void dispose() {
        batch.dispose(); // 释放资源
        image.dispose();
        font.dispose();
        Gdx.app.log("Main", "Resources disposed");
    }
}
