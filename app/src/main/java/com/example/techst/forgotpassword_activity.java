package com.example.techst;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class forgotpassword_activity extends AppCompatActivity implements View.OnClickListener {

    private EditText forgotpassword_mail;
    private Button forgotpassword_send_button;
    private ProgressBar forgotpassword_progressbar;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpassword);

        //UI components
        UI_init();

        //set onClicks
        forgotpassword_send_button.setOnClickListener(this);

        //Firebase instance
        mAuth = FirebaseAuth.getInstance();
    }

    private void UI_init(){
        forgotpassword_mail = findViewById(R.id.forgotpassword_mail);
        forgotpassword_send_button = findViewById(R.id.forgotpassword_send_button);
        forgotpassword_progressbar = findViewById(R.id.forgotpassword_progressbar);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.forgotpassword_send_button:
                forgotpasswordemailsend();
                break;
        }
    }

    private void forgotpasswordemailsend() {

        String email = forgotpassword_mail.getText().toString().trim();

        if(email.isEmpty())
        {
            forgotpassword_mail.setError("Please enter valid email");
            forgotpassword_mail.requestFocus();
        }

        forgotpassword_progressbar.setVisibility(View.VISIBLE);
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {

                    sendUsertoLogin();
                    Toast.makeText(forgotpassword_activity.this, "Please check you email", Toast.LENGTH_LONG).show();
                }
                else
                {
                    forgotpassword_progressbar.setVisibility(View.GONE);
                    Toast.makeText(forgotpassword_activity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void sendUsertoLogin() {
            Intent intent = new Intent(forgotpassword_activity.this, login_activity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(forgotpassword_activity.this, login_activity.class));
    }
}
