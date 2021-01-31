package com.example.cmp309spacegame;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.SoundPool;
import android.view.MotionEvent;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.os.Handler;

//this is a custom view, handling all aspects of the game play
@SuppressLint("ViewConstructor")
public class GameController extends SurfaceView implements Runnable{

    GameActivity activity;
    Thread thread;
    boolean isPlaying;
    int screenX, screenY;
    public static float speedModifier;
    Paint paint;
    List<EnemyBulletInitializer> enemyBullets;
    List<EnemyInitializer> enemies;
    int amountOfEnemiesToBeSpawned = 0;
    int currentEnemyShooting = 0;
    Random random;
    PlayerBulletInitializer[] playerBullets;
    int amountOfPlayerBullets = 10;
    int currentPlayerBullet = 0;
    PlayerInitializer player;
    boolean isGameOver = false;
    int score = 0, scoreMultiplier = 0;
    SharedPreferences prefs;
    boolean hasMovedPlayer, hasSpawnedEnemy;
    boolean motionControls;
    int controlSensitivity;
    OrientationData orientationData;
    long frameTime;
    SoundPool soundPool;
    int point,death;
    float volume;

    public GameController(GameActivity activity, int screenX, int screenY, SoundPool soundPool, int pointSound, int deathSound)
    {
        super(activity);
        //stores the GameActiviy
        this.activity = activity;

        //stores the screen size
        this.screenX = screenX;
        this.screenY = screenY;
        //this is to insure the bullet speeds are constant no matter the height of the screen
        speedModifier = screenY / 2500f;

        //stores the sound pool and sound that where initialized on the activity
        this.soundPool = soundPool;
        point = pointSound;
        death = deathSound;

        //spawns the player
        player = new PlayerInitializer(screenX, screenY,getResources());
        playerBullets = new PlayerBulletInitializer[amountOfPlayerBullets];

       //spawns 10 player bullets (these will be recycled not destroyed)
        for(int i = 0; i< amountOfPlayerBullets;i++)
        {
           PlayerBulletInitializer playerBullet = new PlayerBulletInitializer(getResources(), screenX, screenY);
           playerBullet.x = 0;
           playerBullet.y = -500;
           playerBullets[i] = playerBullet;
        }

        //initializes the style of the view
        paint = new Paint();
        paint.setTextSize(128);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setTextAlign(Paint.Align.CENTER);

        random = new Random();

        enemies = new ArrayList<>();
        enemyBullets = new ArrayList<>();

        //initializes the motion control sensors
        orientationData = new OrientationData(activity);
        frameTime = System.currentTimeMillis();

        //gets the data for the game that is stored on the device
        prefs = activity.getSharedPreferences("cmp309game", Context.MODE_PRIVATE);
        motionControls = prefs.getBoolean("motionControls", false);
        controlSensitivity = (prefs.getInt("motionSensitivity", 500) - 1001) * -1;
        volume = prefs.getInt("volume", 100)/100f;
    }

    //updates the view every 17ms
    @Override
    public void run()
    {
        while (isPlaying)
        {
            spawn();
            update();
            draw();
            sleep();
        }
    }

    //spawn a new enemy instance and if the is an odd number of enemies an enemy bullet is spawned as well
    private void spawn()
    {
        if(amountOfEnemiesToBeSpawned > 0) {
            EnemyInitializer enemy = new EnemyInitializer(screenX, screenY, getResources());
            //set the new enemy above the screen with a random horizontal position
            enemy.y = -20 - enemy.height;
            enemy.x = random.nextInt(screenX - enemy.width);

            enemies.add(enemy);

            //check if there is an odd number of enemies
            if (enemies.size() % 2 != 0) {
                EnemyBulletInitializer enemyBullet = new EnemyBulletInitializer(getResources(), screenX, screenY);
                enemyBullets.add(enemyBullet);
            }

            scoreMultiplier++;
            amountOfEnemiesToBeSpawned--;
        }
    }

    private void update()
    {
        //check if the motion controls are enabled
        if(motionControls) {
            //get the detects horizontal movement of the phone and apply a correlated amount to the players x position
            int elapsedTime = (int) (System.currentTimeMillis() - frameTime);
            frameTime = System.currentTimeMillis();
            if (orientationData.getOrientation() != null && orientationData.getStartOrientation() != null) {
                float roll = orientationData.getOrientation()[2] - orientationData.getStartOrientation()[2];
                float xSpeed = 2 * roll * screenX / controlSensitivity;
                player.x += Math.abs(xSpeed * elapsedTime) > 5 ? xSpeed * elapsedTime : 0;
            }
        }

        //make sure the player is not off the screen
        if(player.x < 0) {
            player.x = 0;
        }

        if(player.x > screenX - player.width) {
            player.x = screenX - player.width;
        }

        //loop through and manipulate the 10 player bullets
        for(PlayerBulletInitializer playerBullet : playerBullets)
        {
            //if the bullets are off the screen move them far away so they wont hit any enemies
            //this is tests first as normally there will be more bullets off screen than on
            if(playerBullet.y < 0)
                playerBullet.y = -500;
            else
                playerBullet.y -= 50 * speedModifier; //move the bullets up the screen

            //loop through all the enemies
            for (EnemyInitializer enemy : enemies)
            {
                //check if the current bullet rectangle is touching the current enemy rectangle
                if(Rect.intersects(enemy.getCollisionShape(),playerBullet.getCollisionShape()))
                {
                    //play sound, give the player points and move the too objects for off the screen
                    soundPool.play(point, volume, volume,0,0,1);
                    score += scoreMultiplier;
                    enemy.y = screenY + 500;
                    playerBullet.y = -500;
                }
            }
        }

        //loop through and manipulate all the enemies
        for(EnemyInitializer enemy : enemies)
        {
            //move the enemies down the screen
            enemy.y += enemy.speed;

            //if the enemies reach the bottom put them back up to the to with a new random speed and x position
            if(enemy.y > screenY)
            {
                int bound = (int) (30*speedModifier);
                enemy.speed = random.nextInt(bound);

                //if the new random speed is too slow set the speed to what the enemy was initialized with
                if(enemy.speed  < 10 * speedModifier)
                    enemy.speed = (int) (10* speedModifier);

                enemy.y = 0;
                enemy.x = random.nextInt(screenX - enemy.width);
            }
        }

        //loop through and manipulate all the enemy bullets
        for(EnemyBulletInitializer enemyBullet : enemyBullets)
        {
            //move the enemy bullets down the screen
            enemyBullet.y += 50 * speedModifier;

            //if the bullets reach the bottom, set the position to an enemy
            if(enemyBullet.y > screenY)
            {
                //the is a 1:50 chance the bullet spawn off screen above the player to keep the player moving
                int movePlayerShot = random.nextInt(50);
                if(movePlayerShot == 0)
                {
                    enemyBullet.x = player.x + ((player.width/2) - (enemyBullet.width/2));
                    enemyBullet.y = 0 - enemyBullet.height;
                }
                else {
                    enemyBullet.x = enemies.get(currentEnemyShooting).x + ((enemies.get(currentEnemyShooting).width / 2) - (enemyBullet.width / 2));
                    enemyBullet.y = enemies.get(currentEnemyShooting).y;
                    currentEnemyShooting++;
                    if (currentEnemyShooting >= enemies.size())
                        currentEnemyShooting = 0;
                }
            }

            //check if the current bullet touches the player
            if(Rect.intersects(enemyBullet.getCollisionShape(), player.getCollisionShape()))
            {
                //player the game over sound and set the game to game over
                //game over is handled on draw()
                soundPool.play(death, volume, volume,0,0,1);
                isGameOver = true;
                return;
            }
        }
    }

    private void draw()
    {
        if(getHolder().getSurface().isValid()) {
            //create and lock the canvas
            Canvas canvas = getHolder().lockCanvas();
            canvas.drawColor(Color.BLACK);

            //draw the score text at the top of the screen
            if(score > 0)
            canvas.drawText(score + " x " + scoreMultiplier + "", screenX / 2f, 164, paint);

            //drawn the controls on the screen until the player uses them
            //this is if the motion controls are off
            if(!prefs.getBoolean("motionControls", false)) {
                if (!hasMovedPlayer) {
                    canvas.drawRect(2, screenY / 2f, screenX - 3, screenY - 2, paint);
                    canvas.drawText("MOVE PLAYER", (screenX / 2f), screenY / 1.33f, paint);
                }

                if (!hasSpawnedEnemy) {
                    canvas.drawRect(2, 2, screenX - 3, screenY / 2f, paint);
                    canvas.drawText("SPAWN ENEMY", screenX / 2f, screenY / 4f, paint);
                }
            }
            else //this is if the motion controls are on
            {
                if (!hasSpawnedEnemy) {
                    canvas.drawRect(2, 2, screenX - 3, screenY - 2, paint);
                    canvas.drawText("SPAWN ENEMY", screenX / 2f, screenY / 2f, paint);
                }
            }

            //draw the player
            canvas.drawBitmap(player.playerSprite, player.x, player.y,paint);
            canvas.drawBitmap(player.fireSprite, player.x + player.width/4f, player.y + player.height,paint);

            //draw all the enemies
            for(EnemyInitializer enemy : enemies)
            {
                canvas.drawBitmap(enemy.enemySprite, enemy.x, enemy.y,paint);
                canvas.drawBitmap(enemy.fireSprite, enemy.x + enemy.width/4f, enemy.y - enemy.height/2f,paint);
            }

            //draw all the player bullets
            for(PlayerBulletInitializer playerBullet : playerBullets)
            {
                canvas.drawBitmap(playerBullet.bulletSprite, playerBullet.x, playerBullet.y,paint);
            }

            //draw all the enemy bullets
            for(EnemyBulletInitializer enemyBullet : enemyBullets)
            {
                canvas.drawBitmap(enemyBullet.bulletSprite, enemyBullet.x, enemyBullet.y,paint);
            }

            //check if the game is over
            //game over is handled here because the canvas MUST be unlocked before leaving
            if(isGameOver)
            {
                //unregister the sensors
                if(motionControls)
                    orientationData.pause();
                //stop the shoot runnable form looping
                stopShooting();
                isPlaying = false;

                //draw game over
                canvas.drawText("GAME OVER", screenX / 2f, screenY / 2f, paint);
                //unlock the canvas
                getHolder().unlockCanvasAndPost(canvas);

                saveIfHighScore();
                waitBeforeExiting();
                return;
            }

            //unlock the canvas
            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    //this delay is to ensure all unresolved action are finished
    private void waitBeforeExiting()
    {
        try {
            Thread.sleep(1000);
            Intent returnIntent = new Intent(activity, MainActivity.class);
            //return the score to the main activity
            returnIntent.putExtra("lastScore", score + "");
            activity.setResult(Activity.RESULT_OK, returnIntent);
            activity.finish();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //checks if the current score beats the locally saved high score, if it does replace the high score
    private void saveIfHighScore()
    {
        if(prefs.getInt("highscore", 0) < score)
        {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("highscore", score);
            editor.apply();
        }
    }

    //delay the game refreshing
    private void sleep()
    {
        try {
            Thread.sleep(17);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume()
    {
        //register the sensors if using motion controls
        if(motionControls)
            orientationData.register();
        //start the shoot runnable form looping
        pRunnable.run();
        isPlaying = true;
        //restart the view
        thread = new Thread(this);
        thread.start();
    }

    public void pause()
    {
        //unregister the sensors
        if(motionControls)
            orientationData.pause();
        //stop the shoot runnable form looping
        stopShooting();
        isPlaying = false;

        try {
            //join the view
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!prefs.getBoolean("motionControls", false)) {
            //check if the user is touching the bottom of the screen, if so move the player to the x of the press
            if (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN) {
                if (event.getY() > screenY / 2f) {
                    hasMovedPlayer = true;
                    player.x = (int) (event.getX() - (player.width / 2));
                }
            }
        }

        //check if the user is touching the top of the screen, if so indicate another enemy needs to be spawned
        if (event.getAction() == MotionEvent.ACTION_UP) {
            int maxNumberOfEnemies = 20;
            if (event.getY() < screenY / 2f || prefs.getBoolean("motionControls", false)) {
                if ((enemies.size() + amountOfEnemiesToBeSpawned) < maxNumberOfEnemies) {
                    hasSpawnedEnemy = true;
                    amountOfEnemiesToBeSpawned++;
                }
            }
        }
        return true;
    }

    private Handler pHandler = new Handler();

    //stop the shoot runnable form looping
    public void stopShooting()
    {
        pHandler.removeCallbacks(pRunnable);
    }

    //shoot runnable - calls shootPlayerBullet() then calls itself 600ms later
    private Runnable pRunnable = new Runnable() {
        @Override
        public void run() {
            shootPlayerBullet();
            pHandler.postDelayed(this, 600);
        }
    };

    //moves a bullet to the players location
    public void shootPlayerBullet()
    {
        playerBullets[currentPlayerBullet].x = player.x + ((player.width/2) - (playerBullets[currentPlayerBullet].width/2));
        playerBullets[currentPlayerBullet].y = player.y;
        currentPlayerBullet++;
        if(currentPlayerBullet >= amountOfPlayerBullets)
            currentPlayerBullet = 0;
    }


}
