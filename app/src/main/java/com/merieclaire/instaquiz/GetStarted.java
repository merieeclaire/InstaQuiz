package com.merieclaire.instaquiz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class GetStarted extends AppCompatActivity {

    TextView appname, description, developer;
    Button gtstbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_get_started);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        appname = findViewById(R.id.appname);
        description = findViewById(R.id.description);
        developer = findViewById(R.id.developer);
        gtstbtn = findViewById(R.id.gtstbtn);

        // Button click to go to SignIn activity
        gtstbtn.setOnClickListener(v -> {
            Intent intent = new Intent(GetStarted.this, SignUp.class);
            startActivity(intent);
        });
    }
}