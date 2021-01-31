package com.example.cmp309spacegame;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

//this stores all the data for the enemy bullets
class EnemyBulletInitializer {

    int x,y, width, height;
    Bitmap bulletSprite;

    EnemyBulletInitializer(Resources res, int screenX, int screenY)
    {
        //gets the attributes/ data of the sprite
        bulletSprite = BitmapFactory.decodeResource(res, R.drawable.sprite_upside_down_red);

        //gets the needed size of the sprite for the screen size
        width = screenX/16;
        height = screenY/19;

        //edits the sprites size
        bulletSprite = Bitmap.createScaledBitmap(bulletSprite, width, height, false);

        //sets the initial location of the sprite to off the screen
        x = screenX + 500;
        y = screenY + 500;
    }

    //creates a rectangle over the sprite (this is used for collision detection)
    Rect getCollisionShape()
    {
        return  new Rect(x+2,y+2,x +(width-2), y+(height-2));
    }
}
