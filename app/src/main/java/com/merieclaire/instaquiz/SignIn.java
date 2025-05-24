package com.merieclaire.instaquiz;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignIn extends AppCompatActivity {

    TextView signUp;
    EditText email, pass;
    Button cont;
    ProgressBar progressBar;
    FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        signUp = findViewById(R.id.signUp);
        email = findViewById(R.id.email);
        pass = findViewById(R.id.pass);
        cont = findViewById(R.id.cont);
        progressBar = findViewById(R.id.progressBar);
        mAuth = FirebaseAuth.getInstance();


        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignIn.this, SignUp.class));
                finish();
            }
        });

        cont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent( SignIn.this, SignUp.class));
                String mUserName = email.getText().toString().trim();
                String mPassword = pass.getText().toString();

                if(TextUtils.isEmpty(mUserName)){
                    email.setError("Please enter your email");
                    return;
                }
                if(TextUtils.isEmpty(mPassword)){
                    pass.setError("Please enter your password");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                mAuth.signInWithEmailAndPassword(mUserName, mPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()){
                            Toast.makeText(SignIn.this, "Logged in Successfully!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SignIn.this, MainActivity.class));
                            finishAffinity();
                        }
                        else {
                            Toast.makeText(SignIn.this, "Please check your email and password or Create a new Account", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }

                    }
                });
            }
        });


    }
}