package com.example.cmp309spacegame;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

//this handles the sensor inputs
public class OrientationData implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnometer;

    private float[] accelOutput;
    private float[] magOutput;

    private float[] orientation = new float[3];
    float[] getOrientation(){
        return orientation;
    }

    private float[] startOrientation = null;
    float[] getStartOrientation(){
        return startOrientation;
    }

    //gets the sensorManager, accelerometer and magnometer
    OrientationData(GameActivity activity){
        sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        assert sensorManager != null;
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    //registers the accelerometer and magnometer
    void register()
    {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, magnometer, SensorManager.SENSOR_DELAY_GAME);
    }

    //unregisters the accelerometer and magnometer
    void pause()
    {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) //sets the accelerometer inputs to accelOutput
            accelOutput = event.values;
        else if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) //sets the magnometer to magOutput
            magOutput = event.values;

        //checks to make sure but sensors have given a reading
        if(accelOutput != null && magOutput != null)
        {
            float[] rotation = new  float[9];
            float[] inclination = new  float[9];
            boolean success = SensorManager.getRotationMatrix(rotation, inclination, accelOutput, magOutput);

            //check to see if a rotation matrix can be calculated
            if(success)
            {
                //gets the devices orientation base on the matrix
                SensorManager.getOrientation(rotation, orientation);
                //check to see if this is the first orientation reading
                if(startOrientation == null)
                {
                    //set the startOrientation to the orientation
                    startOrientation = new float[orientation.length];
                    System.arraycopy(orientation, 0 ,startOrientation,0, orientation.length);
                }
            }
        }
    }
}
