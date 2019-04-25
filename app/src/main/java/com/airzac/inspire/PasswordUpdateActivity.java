package com.airzac.inspire;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

public class PasswordUpdateActivity extends AppCompatActivity {

    private Button updatePasswordButton;
    private TextView verifypasstxt, newpasstxt, confirmpasstxt;
    private Toolbar mToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_update);

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Settings");

        verifypasstxt = (TextView) findViewById(R.id.verify_password);
        newpasstxt = (TextView) findViewById(R.id.new_password);
        confirmpasstxt = (TextView) findViewById(R.id.confirm_password);

        updatePasswordButton = (Button) findViewById(R.id.update_password_btn);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
           sendUserToMainActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendUserToMainActivity() {
        Intent homeIntent = new Intent(PasswordUpdateActivity.this, MainActivity.class);
        startActivity(homeIntent);
    }

}
