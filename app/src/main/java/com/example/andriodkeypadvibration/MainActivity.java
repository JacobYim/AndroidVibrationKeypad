package com.example.andriodkeypadvibration;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    EditText editText;
    Button btn_show_keyboard, btn_hide_keyboard;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.editText);
        btn_show_keyboard = findViewById(R.id.btn_show_keyboard);
        btn_hide_keyboard = findViewById(R.id.btn_hide_keyboard);

        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//        vibrator.vibrate(5000); // 0.5초간 진동
        final long[] vibratePattern = new long[]{0, 10000, 0, 10000};
        final int[] aplitudePattern = new int[]{255,255,255,255};
        final int repeat = 2;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(vibratePattern, aplitudePattern, repeat));
        }

        final InputMethodManager manager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE) ;



        btn_show_keyboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibrator.cancel();
//                manager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        btn_hide_keyboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(vibratePattern, aplitudePattern, repeat));
                }
//                vibrator.cancel();
//                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
    }
}