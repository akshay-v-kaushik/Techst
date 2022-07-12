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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class login_activity extends AppCompatActivity implements View.OnClickListener {

    private TextView register_link, forgot_password_link;
    private EditText login_email, login_password;
    private Button login_button;
    private ProgressBar login_progressbar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //UI connection
        UI_init();

        //set onclicks
        login_button.setOnClickListener(this);
        register_link.setOnClickListener(this);
        forgot_password_link.setOnClickListener(this);

        //Firebase instance
        mAuth = FirebaseAuth.getInstance();



    }


    private void UI_init(){
        register_link = findViewById(R.id.register_link);
        forgot_password_link = findViewById(R.id.forgotpassword_link);
        login_email = findViewById(R.id.login_email);
        login_password = findViewById(R.id.login_password);
        login_button = findViewById(R.id.login_button);
        login_progressbar = findViewById(R.id.login_progressbar);
    }

    @Override
    public void onClick(View v) {

        switch(v.getId())
        {
            case R.id.register_link:
                startActivity(new Intent(login_activity.this, register_activity.class));
                break;
            case R.id.forgotpassword_link:
                startActivity(new Intent(login_activity.this, forgotpassword_activity.class));
                break;
            case R.id.login_button:
                loginAuth();
                break;
        }

    }

    private void loginAuth() {

        String email = login_email.getText().toString().trim();
        String password = login_password.getText().toString().trim();

        if (email.isEmpty()) {
            login_email.setError("Please provide a valid email");
            login_email.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            login_password.setError("Password is required");
            login_password.requestFocus();
            return;
        }
        if (password.length() < 6) {
            login_password.setError("Minimum password length should be 6 characters");
            login_password.requestFocus();
            return;
        }

        login_progressbar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful())
                {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if(user.isEmailVerified())
                    {
                        Toast.makeText(login_activity.this, "Welcome user", Toast.LENGTH_LONG).show();
                        //start activity
                        sendUsertoUserspage();
                    }
                    else
                    {
                        //email verification
                        login_progressbar.setVisibility(View.GONE);
                        user.sendEmailVerification();
                        Toast.makeText(login_activity.this, "Check email to verify your account", Toast.LENGTH_LONG).show();
                    }
                }
                else
                {
                    login_progressbar.setVisibility(View.GONE);
                    Toast.makeText(login_activity.this, "Failed to login", Toast.LENGTH_LONG).show();

                }
            }
        });

    }

    private void sendUsertoUserspage() {
            Intent intent = new Intent(login_activity.this, user_activity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
    }
}
