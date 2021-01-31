package com.example.cmp309spacegame;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

//this stores all the data for the enemies
class EnemyInitializer {

    int speed = 20;
    int x,y, width,height;
    Bitmap enemySprite;
    Bitmap fireSprite;

    EnemyInitializer(int screenX, int screenY, Resources res)
    {
        //gets the attributes/ data of the sprites
        enemySprite = BitmapFactory.decodeResource(res, R.drawable.sprite_upside_down);
        fireSprite = BitmapFactory.decodeResource(res, R.drawable.sprite);

        //gets the needed size of the enemy sprite for the screen size
        width = screenX/10;
        height = screenY/12;

        //edits the enemy sprites size
        enemySprite = Bitmap.createScaledBitmap(enemySprite, width, height, false);

        //sets the initial location of the sprite to just off the screen
        x = 0;
        y -= height;

        //edits the fire sprites size to half of the enemy sprite
        fireSprite = Bitmap.createScaledBitmap(fireSprite, width/2, height/2, false);
    }
    //creates a rectangle over the enemy sprite (this is used for collision detection)
    Rect getCollisionShape()
    {
        return  new Rect(x+2,y+2,x +(width-2), y+(height-2));
    }
}
