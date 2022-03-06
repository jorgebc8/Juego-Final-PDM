package com.obstacleavoid.screen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import com.obstacleavoid.common.GameManager;
import com.obstacleavoid.config.DifficultyLevel;
import com.obstacleavoid.config.GameConfig;
import com.obstacleavoid.entity.Background;
import com.obstacleavoid.entity.Obstacle;
import com.obstacleavoid.entity.Player;

public class GameController {

    private static final Logger log = new Logger(GameController.class.getName(), Logger.DEBUG);
    private Player player;
    private Array<Obstacle> obstacles = new Array<Obstacle>();
    private Background background; // Inicion Background
    private float obstacleTimer;
    private float scoreTimer;
    private int lives = GameConfig.LIVES_START;
    private int score;
    private int displayScore;
    private final float startPlayerX = (GameConfig.WORLD_WIDTH - GameConfig.PLAYER_SIZE) / 2f;
    private final float startPlayerY = 1 - GameConfig.PLAYER_SIZE / 2f;

    private Pool<Obstacle> obstaclePool;

    public GameController() {
        init();
    }

    private void init() {

        player = new Player(); //Instacear Jugador
        // posicion del jugador
        player.setPosition(startPlayerX, startPlayerY);
        //create obstacle pool
        obstaclePool = Pools.get(Obstacle.class, 40);
        // crear background y posicion
        background = new Background();
        background.setPosition(0, 0);
        background.setSize(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT);
    }

    // public methods
    public void update(float delta) {

        if (isGameOver()) {
            return;
        }


        updatePlayer();

        updateObstacles(delta);

        updateScore(delta);

        updateDisplayScore(delta);

        if (isPlayerCollidingWithObstcle()) {
            log.debug("Collision detected");
            lives--;

            if (isGameOver()) {
                log.debug("Game Over!!");
                GameManager.INSTANCE.updateHighScore(score);
            } else {
                restart();
            }
        }
    }
    // getters

    public Player getPlayer() {
        return player;
    }

    public Array<Obstacle> getObstacles() {
        return obstacles;
    }

    public Background getBackground() {
        return background;
    }

    public int getLives() {
        return lives;
    }

    public int getDisplayScore() {
        return displayScore;
    }

    // private methods


    private void restart() {
        obstaclePool.freeAll(obstacles);
        obstacles.clear();
        player.setPosition(startPlayerX, startPlayerY);
    }


    public boolean isGameOver() {
        return lives <= 0;
    }

    private void updatePlayer() {


        float xSpeed = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            xSpeed = GameConfig.MAX_PLAYER_X_SPEED;
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            xSpeed = -GameConfig.MAX_PLAYER_X_SPEED;
        }

        player.setX(player.getX() + xSpeed);


        blockPlayerFromLeavingTheWorld();
    }

    private void updateObstacles(float delta) {
        for (Obstacle obstacle : obstacles) {
            obstacle.update();
        }
        createNewObstacle(delta);
        removePassedObstacle();
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

    private void createNewObstacle(float delta) {
        obstacleTimer += delta;
        if (obstacleTimer >= GameConfig.OBSTACLE_SPAWN_TIME) {
            float min = 0;
            float max = GameConfig.WORLD_WIDTH - GameConfig.OBSTACLE_SIZE;
            float obstacleX = MathUtils.random(min, max);
            float obstacleY = GameConfig.WORLD_HEIGHT;

            Obstacle obstacle = obstaclePool.obtain();
            DifficultyLevel difficultyLevel = GameManager.INSTANCE.getDifficultyLevel();
            obstacle.setYSpeed(difficultyLevel.getObstacleSpeed());
            obstacle.setPosition(obstacleX, obstacleY);

            obstacles.add(obstacle);
            obstacleTimer = 0f;
        }
    }

    private void removePassedObstacle() { // desaparece al final los obstaculos del jugador
        if (obstacles.size > 0) {
            Obstacle first = obstacles.first();
            float minObstacleY = -GameConfig.OBSTACLE_SIZE;
            if (first.getY() < minObstacleY) {
                obstacles.removeValue(first, true);
                obstaclePool.free(first);
            }
        }
    }

    private boolean isPlayerCollidingWithObstcle() {
        for (Obstacle obstacle : obstacles) {
            if (obstacle.isNotHit() && obstacle.isPlayerColliding(player)) {
                return true;
            }
        }
        return false;
    }

    private void blockPlayerFromLeavingTheWorld() {

        float playerX = MathUtils.clamp(player.getX(), 0, GameConfig.WORLD_WIDTH - player.getWidth());

        player.setPosition(playerX, player.getY());
    }
}
