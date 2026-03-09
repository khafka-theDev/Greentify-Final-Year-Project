package org.oss.greentify.Home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import org.oss.greentify.MainActivity; // ✅ Correct
import org.oss.greentify.R;


public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setTitle("About");

        Button finishButton = findViewById(R.id.buttonFinishAbout);
        finishButton.setOnClickListener(view -> {
            // ✅ Set walkthrough flags correctly
            SharedPreferences prefs = getSharedPreferences("GreentifyPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("isFirstTimeUser", true); // ✅ Leave this true to trigger in MainActivity
            editor.putBoolean("hasSeenMainTutorial", false); // ✅ Reset this to allow walkthrough
            editor.apply();

            // ✅ Ensure MainActivity restarts fresh (not already running behind)
            Intent intent = new Intent(AboutActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // 💥 Wipe back stack
            startActivity(intent);
            finish(); // ✅ Kill AboutActivity
        });
    }
}
