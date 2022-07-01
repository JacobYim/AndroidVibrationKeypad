package com.example.andriodkeypadvibration;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
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

import com.google.android.material.textfield.TextInputLayout;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    EditText editText, nameInput;
    Button btn_vibration_on, btn_vibration_off, savebtn;
    TextView instruction_bar;

    private String contents = "";
    private String vib = "no";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Log.e("e", getFilesDir().toString());
//        Log.e("e", getExternalFilesDir(null).toString());

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
        savebtn = findViewById(R.id.save);

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

//                vibrator.cancel();
//                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getAction()==KeyEvent.ACTION_DOWN) {
                    Long tsLong = System.currentTimeMillis();
                    String ts = tsLong.toString();
                    contents = contents + ts+","+nameInput.getText().toString()+","+keyEvent.toString()+"\n";
                    Log.e("ERROR", ts+","+nameInput.getText().toString()+","+String.valueOf(i)+", "+keyEvent.toString());
                    return true;
                }
                else if (keyEvent.getAction()==KeyEvent.ACTION_UP) {
                    Long tsLong = System.currentTimeMillis();
                    String ts = tsLong.toString();
                    Log.e("ERROR", ts+","+nameInput.getText().toString()+","+String.valueOf(i)+", "+keyEvent.toString());
                    return true;
                }
                return false;
            }
        });
        savebtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Long tsLong = System.currentTimeMillis();
                String ts = tsLong.toString();
                String FileName = nameInput.getText().toString() + "_" + ts +"_"+vib+".csv";
                File file = new File(appDirectory, FileName);
                OutputStream myOutput;
                try {
                    myOutput = new BufferedOutputStream(new FileOutputStream(file, true));
                    Log.e("error", contents);
                    myOutput.write(contents.getBytes(StandardCharsets.UTF_8));
                    myOutput.flush();
                    myOutput.close();
                    Log.i("INFO", "Saved at " + file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                contents = "";

            }
        });

    }
}