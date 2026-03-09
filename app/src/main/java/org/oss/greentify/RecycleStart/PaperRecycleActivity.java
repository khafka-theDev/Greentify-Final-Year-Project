package org.oss.greentify.RecycleStart;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;


import androidx.appcompat.app.AppCompatActivity;

import org.oss.greentify.R;

public class PaperRecycleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paper_recycle);

        Button startButton = findViewById(R.id.btnStartRecycling);
        startButton.setOnClickListener(v -> {
            Intent intent = new Intent(PaperRecycleActivity.this, LocationSelectionActivity.class);
            intent.putExtra("type", "paper");
            startActivity(intent);
        });
    }
}
