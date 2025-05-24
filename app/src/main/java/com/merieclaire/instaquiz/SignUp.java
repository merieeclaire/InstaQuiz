package com.merieclaire.instaquiz;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity {

    public static final String TAG = "TAG";
    TextView signIn;
    EditText name, em, phone, passwd;
    Button register;
    ProgressBar progressBar;
    FirebaseAuth mAuth;
    FirebaseFirestore mStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        signIn = findViewById(R.id.signIn);
        name = findViewById(R.id.name);
        em = findViewById(R.id.em);
        phone = findViewById(R.id.phone);
        passwd = findViewById(R.id.passwd);
        register = findViewById(R.id.register);
        progressBar = findViewById(R.id.progressBar);
        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(SignUp.this, MainActivity.class));
            finishAffinity();
        }


        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent( SignUp.this, SignIn.class));
                finish();
            }
        });


        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mName = name.getText().toString();
                String mEmail = em.getText().toString().trim();
                String mPhone = phone.getText().toString().trim();
                String mPasswd = passwd.getText().toString();

                if (TextUtils.isEmpty(mName)) {
                    name.setError("Please enter your name");
                    return;
                }
                if (TextUtils.isEmpty(mEmail)) {
                    em.setError("Please enter your email");
                    return;
                }
                if (TextUtils.isEmpty(mPhone)) {
                    phone.setError("Please enter your phone number");
                    return;
                }
                if (TextUtils.isEmpty(mPasswd)) {
                    passwd.setError("Please enter your password");
                    return;
                }
                if (mPasswd.length() < 8) {
                    passwd.setError("Password must be at least 8     characters");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                mAuth.createUserWithEmailAndPassword(mEmail, mPasswd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser mUser = mAuth.getCurrentUser();
                            mUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(SignUp.this, "Registration Successful!", Toast.LENGTH_SHORT).show();

                                }
                            }) .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "On Failure: Email not sent" + e.getMessage());

                                }
                            });

                            DocumentReference documentReference = mStore.collection("users").document(mUser.getUid());
                            Map<String, Object> user = new HashMap<>();
                            user.put("Name", mName);
                            user.put("email", mEmail);
                            user.put("phone", mPhone);
                            user.put("password", mPasswd);
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d(TAG, "User profile created");
                                }
                            }) .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "On failed: " + e.getMessage());
                                }
                            });

                            Toast.makeText(SignUp.this, "User Registered!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SignUp.this, MainActivity.class));
                            finishAffinity();
                        }
                        else {
                            Toast.makeText(SignUp.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            }
        });
    }
}