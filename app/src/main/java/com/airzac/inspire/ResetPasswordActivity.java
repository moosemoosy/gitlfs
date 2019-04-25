package com.airzac.inspire;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import org.w3c.dom.Text;

public class ResetPasswordActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText resetemail;
    private Button sendemailbutton;
    private FirebaseAuth mAuth;
    String current_user_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            current_user_email = mAuth.getCurrentUser().getEmail();
        }
        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Reset Password");

        resetemail = (EditText) findViewById(R.id.forgot_email);
        sendemailbutton = (Button) findViewById(R.id.send_reset_button);

        if (mAuth.getCurrentUser()!= null)
        {
            resetemail.setText(current_user_email);
        }

        sendemailbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = resetemail.getText().toString();
                if (TextUtils.isEmpty(email))
                {
                    Toast.makeText(ResetPasswordActivity.this, "enter email address!", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful())
                            {
                                Toast.makeText(ResetPasswordActivity.this, "email sent. Please check your email to reset your password.", Toast.LENGTH_LONG).show();
                                if (mAuth.getCurrentUser()!= null) {
                                    mAuth.signOut();
                                }
                                startActivity(new Intent(ResetPasswordActivity.this, LoginActivity.class));
                            }
                            else
                            {
                                String errmsg = task.getException().getMessage();
                                Toast.makeText(ResetPasswordActivity.this, "error: " +errmsg, Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
