package com.example.techst;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

public class register_activity extends AppCompatActivity implements View.OnClickListener {

    private EditText register_email, register_username, register_password;
    private Button register_button;
    private FirebaseAuth mAuth;
    private ProgressBar register_progressbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //UI connection
        UI_init();

        //set onclicks
        register_button.setOnClickListener(this);

        //Firebase instances
        mAuth = FirebaseAuth.getInstance();


    }


    private void UI_init() {
        register_email = findViewById(R.id.register_email);
        register_username = findViewById(R.id.register_username);
        register_password = findViewById(R.id.register_password);
        register_button = findViewById(R.id.register_button);
        register_progressbar = findViewById(R.id.register_progressbar);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register_button:
                registerUser();

                break;
        }
    }

    private void registerUser() {

        String email = register_email.getText().toString().trim();
        String username = register_username.getText().toString().trim();
        String password = register_password.getText().toString().trim();

        if (email.isEmpty()) {
            register_email.setError("Please provide a valid email");
            register_email.requestFocus();
            return;
        }
        if (username.isEmpty()) {
            register_username.setError("Username is required");
            register_username.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            register_password.setError("Password is required");
            register_password.requestFocus();
            return;
        }
        if (password.length() < 6) {
            register_password.setError("Minimum password length should be 6 characters");
            register_email.requestFocus();
            return;
        }

        register_progressbar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {

                    User user = new User(email, username);
                    FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(username).build();
                                user.updateProfile(profileUpdates);
                                Toast.makeText(register_activity.this, "Successful registered", Toast.LENGTH_LONG).show();
                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                sendUserToLogin();
                            }
                            else
                            {
                                register_progressbar.setVisibility(View.GONE);
                                Toast.makeText(register_activity.this, "Failed to register", Toast.LENGTH_LONG).show();

                            }
                        }
                    });
                }
                else{
                    register_progressbar.setVisibility(View.GONE);
                    Toast.makeText(register_activity.this, "Failed to register", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void sendUserToLogin() {
        Intent intent = new Intent(register_activity.this, login_activity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(register_activity.this, login_activity.class));
    }
}