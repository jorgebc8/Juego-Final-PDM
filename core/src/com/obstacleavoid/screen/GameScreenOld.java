package com.obstacleavoid.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.obstacleavoid.assets.AssetPaths;
import com.obstacleavoid.config.DifficultyLevel;
import com.obstacleavoid.config.GameConfig;
import com.obstacleavoid.entity.Obstacle;
import com.obstacleavoid.entity.Player;
import com.obstacleavoid.util.GdxUtils;
import com.obstacleavoid.util.ViewportUtils;
import com.obstacleavoid.util.debug.DebugCameraController;

@Deprecated
public class GameScreenOld implements Screen {

    private static final Logger log = new Logger(GameScreenOld.class.getName(), Logger.DEBUG);
    private OrthographicCamera camera;
    private Viewport viewport;
    private ShapeRenderer renderer;

    private OrthographicCamera hudCamera;
    private Viewport hudViewport;
    private SpriteBatch batch;
    private BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();
    private DifficultyLevel difficultyLevel = DifficultyLevel.MEDIUM;

    private Player player;
    private Array<Obstacle> obstacles = new Array<Obstacle>();
    private float obstacleTimer;
    private float scoreTimer;
    private int lives = GameConfig.LIVES_START;
    private int score;
    private int displayScore;


    private DebugCameraController debugCameraController;

    @Override
    public void show() {

        camera = new OrthographicCamera();
        viewport = new FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT, camera);
        renderer = new ShapeRenderer();

        hudCamera = new OrthographicCamera();
        hudViewport = new FitViewport(GameConfig.HUB_WIDTH, GameConfig.HUB_HEIGHT, hudCamera);
        batch = new SpriteBatch();
        font = new BitmapFont(Gdx.files.internal(AssetPaths.UI_FONT));
        // controladores de la camara
        debugCameraController = new DebugCameraController();
        debugCameraController.setStartPosition(GameConfig.WORLD_CENTER_X, GameConfig.WORLD_CENTER_Y);

        player = new Player(); //Instancia del Jugador

        //calacular Posicion
        float startPlayerX = GameConfig.WORLD_WIDTH / 2f;
        float startPlayerY = 1;


        // posicion del jugador
        player.setPosition(startPlayerX, startPlayerY);


    }

    @Override
    public void render(float delta) {

        debugCameraController.handleDebugInput(delta);
        debugCameraController.applyTo(camera);
        // actualizar mundo
        update(delta);


        //limpiar pantalla
        GdxUtils.clearScreen();

        //render / fuentes(letras) ui/hud
        renderUi();

        //renderizar graficos
        rendererDebug();
    }

    private void update(float delta) {

        if (isGameOver()) {
            log.debug("Game Over!!!");
            return;
        }

        updatePlayer();

        updateObstacles(delta);

        updateScore(delta);

        updateDisplayScore(delta);

        if (isPlayerCollidingWithObstcle()) {
            log.debug("Collision detected");
            lives--;
        }
    }

    private boolean isGameOver() {
        return lives <= 0;
    }

    private boolean isPlayerCollidingWithObstcle() {
        for (Obstacle obstacle : obstacles) {
            if (obstacle.isNotHit() && obstacle.isPlayerColliding(player)) {
                return true;
            }
        }
        return false;
    }

    private void updatePlayer() {

        blockPlayerFromLeavingTheWorld();
    }

    private void blockPlayerFromLeavingTheWorld() {

        float playerX = MathUtils.clamp(player.getX(), player.getWidth() / 2f, GameConfig.WORLD_WIDTH - player.getWidth() / 2f);

        player.setPosition(playerX, player.getY());
    }

    private void updateObstacles(float delta) {
        for (Obstacle obstacle : obstacles) {
            obstacle.update();
        }
        createNewObstacle(delta);
    }

    private void createNewObstacle(float delta) {
        obstacleTimer += delta;
        if (obstacleTimer >= GameConfig.OBSTACLE_SPAWN_TIME) {
            float min = 0f;
            float max = GameConfig.WORLD_WIDTH;
            float obstacleX = MathUtils.random(min, max);
            float obstacleY = GameConfig.WORLD_HEIGHT;

            Obstacle obstacle = new Obstacle();
            obstacle.setYSpeed(difficultyLevel.getObstacleSpeed());
            obstacle.setPosition(obstacleX, obstacleY);

            obstacles.add(obstacle);
            obstacleTimer = 0f;
        }
    }

    private void renderUi() {
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        String livesText = "LIVES: " + lives;
        layout.setText(font, livesText);

        font.draw(batch, livesText, 20, GameConfig.HUB_HEIGHT - layout.height);

        String scoreText = "SCORE: " + displayScore;
        layout.setText(font, scoreText);
        font.draw(batch, scoreText, GameConfig.HUB_WIDTH - layout.width - 20, GameConfig.HUB_HEIGHT - layout.height);
        batch.end();
    }

    private void rendererDebug() {

        renderer.setProjectionMatrix(camera.combined);
        renderer.begin(ShapeRenderer.ShapeType.Line);
        drawDebug();
        renderer.end();
        ViewportUtils.drawGrid(viewport, renderer);
    }

    private void drawDebug() {

        player.drawDebug(renderer);
        for (Obstacle obstacle : obstacles) {
            obstacle.drawDebug(renderer);
        }

    }

    private void updateScore(float delta) {
        scoreTimer += delta;

        if (scoreTimer >= GameConfig.SCORE_MAX_TIME) {
            score += MathUtils.random(1, 5);
            scoreTimer = 0.0f;
        }
    }

    private void updateDisplayScore(float delta) {
        if (displayScore < score) {
            displayScore = Math.min(score, displayScore + (int) (60 * delta));
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        hudViewport.update(width, height, true);
        ViewportUtils.debugPixelPerUnit(viewport);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

        renderer.dispose();
        batch.dispose();
        font.dispose();
    }
}
