package com.example.mobile_group_assignment;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private ImageView earth, airplane;
    private ProgressBar loadingBar;
    private final int SPLASH_DURATION = 4000; // 4 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        earth = findViewById(R.id.earth);
        airplane = findViewById(R.id.airplane);
        loadingBar = findViewById(R.id.loadingBar);

        // Load animations
        Animation rotateEarth = AnimationUtils.loadAnimation(this, R.anim.rotate_earth);
        Animation flyAirplane = AnimationUtils.loadAnimation(this, R.anim.fly_airplane);

        earth.startAnimation(rotateEarth);
        airplane.startAnimation(flyAirplane);

        // Animate progress bar
        animateProgressBar();

        // Splash screen delay
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }, SPLASH_DURATION);
    }

    private void animateProgressBar() {
        ObjectAnimator animator = ObjectAnimator.ofInt(loadingBar, "progress", 0, 100);
        animator.setDuration(SPLASH_DURATION);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }
}
