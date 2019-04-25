package com.airzac.inspire;

import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class AdminPanelActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private ImageView article,user, logout;
    private Button articleButton, userprivilegeButton, logoutButton, homeButton;
    private Toolbar mToolbar;
    String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Admin Panel");

        article = (ImageView) findViewById(R.id.article_image);
        user = (ImageView) findViewById(R.id.user_image);
        logout = (ImageView) findViewById(R.id.logout_image);
        articleButton =(Button) findViewById(R.id.article_button);
        userprivilegeButton =(Button) findViewById(R.id.user_button);
        logoutButton =(Button) findViewById(R.id.logout_button);
        homeButton =(Button) findViewById(R.id.home_button);
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser()!=null) {
            currentUserID = mAuth.getCurrentUser().getUid();
        }

        articleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pendingArticlesActivity();
            }
        });

        userprivilegeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userPrivilegeActivity();
            }
        });

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent homeIntent = new Intent(AdminPanelActivity.this, MainActivity.class);
                startActivity(homeIntent);
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                SendUserToLoginActivity();
            }
        });


    }

    private void pendingArticlesActivity() {
        Intent userIntent = new Intent(AdminPanelActivity.this, PendingArticlesActivity.class);
        startActivity(userIntent);
    }

    private void userPrivilegeActivity() {
        Intent userIntent = new Intent(AdminPanelActivity.this, UserPrivilegeActivity.class);
        startActivity(userIntent);
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(AdminPanelActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
}
