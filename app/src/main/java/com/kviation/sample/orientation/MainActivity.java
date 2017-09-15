package com.kviation.sample.orientation;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Manifest;

import static android.R.attr.data;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

  //private Orientation mOrientation;
  private AttitudeIndicator mAttitudeIndicator;
  private SensorManager sManager;
  private SensorEventListener sListener;

  private Boolean status;

  ArrayList<float[]> accelLog = new ArrayList<float[]>();
  ArrayList<String> acceltime = new ArrayList<String>();
  ArrayList<float[]> magLog = new ArrayList<float[]>();
  ArrayList<String> magtime = new ArrayList<String>();

  // Gravitational
  float Rot[]=null;
  // Magnetic
  float I[]=null;
  // Values for acceleration
  float accels[]=new float[3];
  // Values for gyro
  float mags[]=new float[3];
  float[] values = new float[3];
  // Final pitch and roll saved in these
  float pitch;
  float roll;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    // On start of app, keep the visualization turned off
    status = Boolean.FALSE;

    //mOrientation = new Orientation(this);
    mAttitudeIndicator = (AttitudeIndicator) findViewById(R.id.attitude_indicator);
    sManager = (SensorManager) getSystemService(SENSOR_SERVICE);

    ToggleButton toggle = (ToggleButton) findViewById(R.id.toggle);
    toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            accelLog.clear();
            acceltime.clear();
            magLog.clear();
            magtime.clear();
            sManager.registerListener(MainActivity.this, sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);
            sManager.registerListener(MainActivity.this, sManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            sManager.unregisterListener(MainActivity.this,sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
            sManager.unregisterListener(MainActivity.this,sManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD));
            saveLogs();
        }
      }
    });

  }

  public void saveLogs(){
      // Location of logs
      File folder = new File(Environment.getExternalStorageDirectory() + "/Logs");

      // Create folder if doesn't exist
      boolean var = false;
      if (!folder.exists())
        var = folder.mkdir();

      // Name of log file
      File childfile[] = folder.listFiles();

      String fname = "0";

      if (childfile != null){
        fname = Integer.toString(childfile.length);
      }
      final String accPath = folder.toString() + "/" + fname + "-acc.csv";
      final String gyroPath = folder.toString() + "/" + fname + "-gyro.csv";

      File accfile = new File(accPath);
      File gyrofile = new File(gyroPath);

    try {

        // Write accelerometer logs
        FileOutputStream fileStream = new FileOutputStream(accfile);
        for(int i = 0; i < accelLog.size(); i++){
          String data = "";
          data = acceltime.get(i) + "," + Float.toString(accelLog.get(i)[0]) + "," + Float.toString(accelLog.get(i)[1]) + "," + Float.toString(accelLog.get(i)[2]) + "\n";
          byte[] content = data.getBytes();
          fileStream.write(content);
        }
        fileStream.close();

        // Write gyro logs
        fileStream = new FileOutputStream(gyrofile);
        for(int i = 0; i < magLog.size(); i++){
          String data = "";
          data = magtime.get(i) + "," + Float.toString(magLog.get(i)[0]) + "," + Float.toString(magLog.get(i)[1]) + "," + Float.toString(magLog.get(i)[2]) + "\n";
          fileStream.write(data.getBytes());
          byte[] content = data.getBytes();
          fileStream.write(content);
        }
        fileStream.close();

        Toast.makeText(getApplicationContext(),"Logs Saved", Toast.LENGTH_SHORT).show();

      }
      catch (IOException e) {
        Log.e("Exception", "File write failed: " + e.toString());
      }

  }

  public void logData(float accels[], float mags[]){
    // Get timestamp
    Long tsLong = System.currentTimeMillis()/1000;
    String ts = tsLong.toString();

    //Log acceleration
    accelLog.add(accels);
    acceltime.add(ts);

    // Log gryo
    magLog.add(mags);
    magtime.add(ts);

  }

  @Override
  public void onSensorChanged(SensorEvent event)
  {

    switch (event.sensor.getType())
    {
      case Sensor.TYPE_MAGNETIC_FIELD:
        mags = event.values.clone();
        break;
      case Sensor.TYPE_ACCELEROMETER:
        accels = event.values.clone();
        break;
    }

    if (mags != null && accels != null) {

      logData(accels,mags);

      Rot = new float[9];
      I= new float[9];
      SensorManager.getRotationMatrix(Rot, I, accels, mags);

      float[] outR = new float[9];
      SensorManager.remapCoordinateSystem(Rot, SensorManager.AXIS_X,SensorManager.AXIS_Z, outR);
      SensorManager.getOrientation(outR, values);

      pitch = values[1]*57.2957795f;
      roll = values[2]*57.2957795f;

      // Pass data on to visualizer
      mAttitudeIndicator.setAttitude(pitch, roll);

    }
  }

  // Rewuired to implement SensorEventListener
  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {}


}
