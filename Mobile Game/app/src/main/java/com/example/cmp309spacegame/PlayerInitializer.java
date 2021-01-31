package com.example.cmp309spacegame;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

//this stores all the data for the player
class PlayerInitializer {
    int x,y, width, height;
    Bitmap playerSprite;

    Bitmap fireSprite;

    PlayerInitializer(int screenX, int screenY, Resources res)
    {
        //gets the attributes/ data of the sprites
        playerSprite = BitmapFactory.decodeResource(res, R.drawable.sprite);
        fireSprite = BitmapFactory.decodeResource(res, R.drawable.sprite_upside_down);

        //gets the needed size of the player sprite for the screen size
        width = screenX/10;
        height = screenY/12;

        //edits the player sprites size
        playerSprite = Bitmap.createScaledBitmap(playerSprite, width, height, false);
        //edits the fire sprites size to half of the player sprite
        fireSprite = Bitmap.createScaledBitmap(fireSprite, width/2, height/2, false);

        //sets the initial location of the sprite to the middle of the screen and slightly about the bottom
        x = screenX / 2;
        y = (int)(screenY / 1.32);
    }

    //creates a rectangle over the player sprite (this is used for collision detection)
    Rect getCollisionShape()
    {
        return  new Rect(x+30,y+80,x +(width-30), y+(height-20));
    }
}
