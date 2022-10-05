package com.example.andriodkeypadvibration;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    EditText editText, nameInput;
    Button btn_vibration_on, btn_vibration_off, savebtn, startbtn;
    TextView instruction_bar, statistics;

    private String contents = "";
    private String gyrocontents = "";
    private String acccontents = "";
    private String vib = "no";

    private SensorManager sensorManager;
    private Sensor gyrosensor, accsensor;
    private AccelorlateListener AccelorlateListener;
    private GyroscopeListener GyroscopeListener;
    private Boolean recording = false;

    private Long tsLong;
    private String ts;

    private String GyroFileName, AccFileName;
    private File GyroFile, AccFile;
    private OutputStream gyroOutput, accOutput;

    private Map<String, Integer> map = new HashMap<String, Integer>() {{
        put("0", 0);
        put("1", 0);
        put("2", 0);
        put("3", 0);
        put("4", 0);
        put("5", 0);
        put("6", 0);
        put("7", 0);
        put("8", 0);
        put("9", 0);
        put("10", 0);
    }};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File appDirectory = new File( getFilesDir().toString()+"/data" );
        if ( !appDirectory.exists() ) {
            appDirectory.mkdirs();
            Log.e("INFO", "Created ... "+ appDirectory.getAbsolutePath());
        }

        editText = findViewById(R.id.editText);
        nameInput = findViewById(R.id.nameInput);
        instruction_bar = findViewById(R.id.instruction_bar);
        btn_vibration_on = findViewById(R.id.btn_vibration_on);
        btn_vibration_off = findViewById(R.id.btn_vibration_off);
        startbtn = findViewById(R.id.btn_start_measure);
        savebtn = findViewById(R.id.save);
        statistics = findViewById(R.id.statistics);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gyrosensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        accsensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        GyroscopeListener = new GyroscopeListener();
        AccelorlateListener = new AccelorlateListener();

        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        final long[] vibratePattern = new long[]{0, 10000, 0, 10000};
        final int[] aplitudePattern = new int[]{255,255,255,255};
        final int repeat = 2;

        final InputMethodManager manager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE) ;
        btn_vibration_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vib = "yes";
                    vibrator.vibrate(VibrationEffect.createWaveform(vibratePattern, aplitudePattern, repeat));
                }
//                manager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            }
        });
        btn_vibration_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vib = "no";
                vibrator.cancel();
            }
        });
        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (recording) {
                    if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {

                        Long tsLong = System.currentTimeMillis();
                        String ts = String.format("%.3f", tsLong / 1000.000);
                        String text = ts + "," + nameInput.getText().toString() + "," + KeyEvent.keyCodeToString(keyEvent.getKeyCode()) + ",ACTION_DOWN\n";
                        contents = contents + text;

                        try {
                            gyrocontents = GyroscopeListener.popContents();
                            acccontents = AccelorlateListener.popContents();
                            gyroOutput.write(gyrocontents.getBytes(StandardCharsets.UTF_8));
                            accOutput.write(acccontents.getBytes(StandardCharsets.UTF_8));
                            gyroOutput.flush();
                            accOutput.flush();

                            Log.i("INFO", "Saved at " + GyroFile);
                            Log.i("INFO", "Saved at " + AccFile);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if (i-7 < 11 ){
                            map.put(Integer.toString(i-7), map.get(Integer.toString(i-7))+1);
                        }
                        return false;

                    } else if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                        if (nameInput.getText().toString().isEmpty()) {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Please Type your Name")
                                    .setCancelable(false)
                                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // Whatever...
                                        }
                                    }).show();
                            return true;
                        }
                        Long tsLong = System.currentTimeMillis();
                        String ts = String.format("%.3f", tsLong / 1000.000);
                        String text = ts + "," + nameInput.getText().toString() + "," + KeyEvent.keyCodeToString(keyEvent.getKeyCode()) + ",ACTION_UP\n";
                        contents = contents + text;

                        String statistics_text = "";
                        for(Map.Entry<String, Integer> entry: map.entrySet()) {
                            statistics_text += (entry.getKey() + " : " + Integer.toString(entry.getValue()) +" \t ");
                        }
                        statistics.setText(statistics_text);

                        return false;
                    }
                }else{
                    if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Please Start Measurement")
                                .setCancelable(false)
                                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                }).show();
                    }
                }
                return true;
            }
        });
        startbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recording = true;
                contents = "";
                sensorManager.registerListener(GyroscopeListener, gyrosensor, 5000);
                sensorManager.registerListener(AccelorlateListener, accsensor, 5000);
                tsLong = System.currentTimeMillis()/1000;
                ts = tsLong.toString();
                GyroFileName = "gyroscope_"+ nameInput.getText().toString() + "_" + ts +"_"+vib+".csv";
                GyroFile = new File(appDirectory, GyroFileName);
                AccFileName = "accelerate_"+ nameInput.getText().toString() + "_" + ts +"_"+vib+".csv";
                AccFile = new File(appDirectory, AccFileName);
                try {
                    gyroOutput = new BufferedOutputStream(new FileOutputStream(GyroFile, true));
                    accOutput = new BufferedOutputStream(new FileOutputStream(AccFile, true));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        savebtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (recording){
                    sensorManager.unregisterListener(GyroscopeListener);
                    sensorManager.unregisterListener(AccelorlateListener);

                    String FileName = "touchevent_" + nameInput.getText().toString() + "_" + ts +"_"+vib+".csv";
                    File file = new File(appDirectory, FileName);
                    OutputStream myOutput;
                    try {
                        myOutput = new BufferedOutputStream(new FileOutputStream(file, true));
                        myOutput.write(contents.getBytes(StandardCharsets.UTF_8));
                        myOutput.flush();
                        myOutput.close();
                        Log.i("INFO", "Saved at " + file);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        gyrocontents = GyroscopeListener.popContents();
                        gyroOutput.write(gyrocontents.getBytes(StandardCharsets.UTF_8));
                        gyroOutput.flush();
                        acccontents = AccelorlateListener.popContents();
                        accOutput.write(acccontents.getBytes(StandardCharsets.UTF_8));
                        accOutput.flush();
                        Log.i("INFO", "Saved at " + AccFile);
                        Log.i("INFO", "Saved at " + GyroFile);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    gyrocontents = "";
                    acccontents = "";
                    contents = "";
                    recording = false;
                    editText.setText("");
                    try {
                        gyroOutput.close();
                        accOutput.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    map = new HashMap<String, Integer>() {{
                        put("0", 0);
                        put("1", 0);
                        put("2", 0);
                        put("3", 0);
                        put("4", 0);
                        put("5", 0);
                        put("6", 0);
                        put("7", 0);
                        put("8", 0);
                        put("9", 0);
                        put("10", 0);
                    }};
                    String statistics_text = "";
                    for(Map.Entry<String, Integer> entry: map.entrySet()) {
                        statistics_text += (entry.getKey() + " : " + Integer.toString(entry.getValue()) +" \t ");
                    }
                    statistics.setText(statistics_text);
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Successfully Saved, Thank You")
                            .setCancelable(false)
                            .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Whatever...
                                }
                            }).show();
                }
            }
        });
    }

}

