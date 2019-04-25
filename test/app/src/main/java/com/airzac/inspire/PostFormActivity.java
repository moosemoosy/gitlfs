package com.airzac.inspire;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class PostFormActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    private DatabaseReference personalityRef;
    private String articleName, personalityName, bgcolor;
    private Button editArticleButton, postVideoButton;
    private EditText articleNameText, videourlText;
    private TextView colorText;
    private Spinner personalitySpinner, articleTypeSpinner;
    private Spinner colorSpinner;
    private CheckBox encryptCheckBox;
    private ImageView colorView;
    private DatabaseReference userRef, postsRef;
    String currentUserId, currentDate, currentTime, randPostName, articleTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_form);

        mAuth = FirebaseAuth.getInstance();;
        currentUserId = mAuth.getCurrentUser().getUid();

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Article Details");

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        personalityRef = FirebaseDatabase.getInstance().getReference().child("Personality");
        articleNameText = (EditText) findViewById(R.id.article_title);
        videourlText = (EditText) findViewById(R.id.video_link);
        videourlText.setVisibility(View.GONE);
        colorText = (TextView)findViewById(R.id.colorText);
        encryptCheckBox = (CheckBox) findViewById(R.id.encrypt_check);
        personalitySpinner = (Spinner)findViewById(R.id.spinner_personality_name);
        articleTypeSpinner = (Spinner)findViewById(R.id.article_type_spinner);

        editArticleButton = (Button) findViewById(R.id.begin_article_btn);
        postVideoButton = (Button) findViewById(R.id.post_video_btn);
        postVideoButton.setVisibility(View.GONE);

        colorSpinner = (Spinner) findViewById(R.id.spinner_color_value);
        colorView = (ImageView) findViewById(R.id.colorbox);

        colorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                bgcolor = colorSpinner.getSelectedItem().toString();
                int color = Color.parseColor(bgcolor);
                colorView.setBackgroundColor(color);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        encryptCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (encryptCheckBox.isChecked()) {
                    if (!(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)) {
                        encryptCheckBox.setChecked(false);
                        //boolean encrypt = encryptCheckBox.isChecked();
                        //String encryptString = Boolean.toString(encrypt);
                        Toast.makeText(PostFormActivity.this, "Your Android Version Doesn't Support Encryption, Please Upgrade to Android 8.0+", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

        articleTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String article_type = articleTypeSpinner.getSelectedItem().toString();
                if (article_type.equals("Video"))
                {
                    editArticleButton.setVisibility(View.GONE);
                    colorSpinner.setVisibility(View.GONE);
                    colorText.setVisibility(View.GONE);
                    postVideoButton.setVisibility(View.VISIBLE);
                    videourlText.setVisibility(View.VISIBLE);
                }
                else if (article_type.equals("Article"))
                {
                    editArticleButton.setVisibility(View.VISIBLE);
                    colorSpinner.setVisibility(View.VISIBLE);
                    colorText.setVisibility(View.VISIBLE);
                    postVideoButton.setVisibility(View.GONE);
                    videourlText.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        postVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calFordDate = Calendar.getInstance();
                SimpleDateFormat getcurrentDate = new SimpleDateFormat("dd-MM-yyyy");
                currentDate = getcurrentDate.format(calFordDate.getTime());

                Calendar calFordTime = Calendar.getInstance();
                SimpleDateFormat getcurrentTime = new SimpleDateFormat("HH:mm");
                currentTime = getcurrentTime.format(calFordTime.getTime());

                randPostName = currentDate + currentTime;
                boolean bRequiresResponse = encryptCheckBox.isChecked();
                personalityRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String personalityIdTemp="";
                        final String personalityId;

                        personalityName = personalitySpinner.getSelectedItem().toString();
                        articleTitle = articleNameText.getText().toString();
                        String videourl = videourlText.getText().toString();
                        for (DataSnapshot personalitySnapshot1: dataSnapshot.getChildren()) {
                            //String personalityname = personalitySnapshot1.child("personalityname").getValue(String.class);
                            //personality.add(personalityname);
                            //personalityId = personalitySnapshot.getKey();
                            personalityIdTemp = personalitySnapshot1.child("personalityname").getValue(String.class);
                            //Toast.makeText(PostFormActivity.this, "personname ="+personalityId,

                            if (personalityIdTemp == personalityName)
                            {
                                personalityIdTemp = personalitySnapshot1.getKey();
                                break;
                            }
                            else
                            {
                                personalityIdTemp = "cant get you!";
                            }
                        }

                        personalityId = personalityIdTemp;

                        final String finalhtml = "<!DOCTYPE html>" +
                                "<html>" +
                                "<head>" +
                                "<title>"+articleTitle+"</title>" +
                                "<style>" +
                                "body {background-color: #000000; margin:0px;}" +
                                "</style>" +
                                "</head>" +
                                "<body>" +
                                "<iframe style='position: absolute; top:0px; left:0px; bottom:0px; right:0px; width:100%; height:100%; border:none; margin:0; padding:0; overflow:hidden; z-index:999999;' src='"+videourl+"?rel=0&modestbranding=1' frameborder='0' allow='accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture' allowfullscreen></iframe>"+
                                "</body>" +
                                "</html>";

                        userRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists())
                                {
                                    String userfullname = dataSnapshot.child("fullname").getValue().toString();
                                    HashMap postMap = new HashMap();
                                    postMap.put("uid", currentUserId);
                                    postMap.put("date", currentDate);
                                    postMap.put("time", currentTime);
                                    postMap.put("personalityid", personalityId);
                                    postMap.put("personalityname", personalityName);
                                    postMap.put("articletitle", articleTitle);
                                    postMap.put("articlehtml", finalhtml);
                                    postMap.put("authorname", userfullname);
                                    postMap.put("articleid",articleTitle + personalityId);
                                    postMap.put("published", 0);

                                    postsRef.child(personalityId + currentUserId + randPostName).updateChildren(postMap)
                                            .addOnCompleteListener(new OnCompleteListener() {
                                                @Override
                                                public void onComplete(@NonNull Task task)
                                                {

                                                    if(task.isSuccessful())
                                                    {
                                                        sendUserToMainActivity();
                                                        //editor.setHtml("<p>​hi Jarvis setup my day</p><p><br></p><p>Here&nbsp; is my program</p><p><br></p><h1><b>Get me <span style=\"background-color: rgb(255, 235, 59); color: rgb(255, 44, 147);\">all</span> i <span style=\"color: rgb(255, 44, 147); background-color: rgb(255, 235, 59);\">need</span> hey</b></h1><p><b><br></b></p>");
                                                        Toast.makeText(PostFormActivity.this, "Video posted successfully.", Toast.LENGTH_LONG).show();
                                                        //Toast.makeText(PostActivity.this, finalhtml, Toast.LENGTH_SHORT).show();
                                                        //loadingBar.dismiss();
                                                    }
                                                    else
                                                    {
                                                        Toast.makeText(PostFormActivity.this, "Error occurred while posting your video.", Toast.LENGTH_LONG).show();
                                                        //loadingBar.dismiss();
                                                    }
                                                }
                                            });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        personalityRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Is better to use a List, because you don't know the size
                // of the iterator returned by dataSnapshot.getChildren() to
                // initialize the array

                final List<String> personality = new ArrayList<String>();

                for (DataSnapshot personalitySnapshot: dataSnapshot.getChildren()) {
                    String personalityname = personalitySnapshot.child("personalityname").getValue(String.class);
                    personality.add(personalityname);
                    //personalityId = personalitySnapshot.getKey();
                }

                Spinner areaSpinner = (Spinner) findViewById(R.id.spinner_personality_name);
                ArrayAdapter<String> areasAdapter = new ArrayAdapter<String>(PostFormActivity.this, android.R.layout.simple_spinner_item, personality);
                areasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                areaSpinner.setAdapter(areasAdapter);


                editArticleButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        personalityRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                String personalityId="";

                                personalityName = personalitySpinner.getSelectedItem().toString();

                                for (DataSnapshot personalitySnapshot1: dataSnapshot.getChildren()) {
                                    //String personalityname = personalitySnapshot1.child("personalityname").getValue(String.class);
                                    //personality.add(personalityname);
                                    //personalityId = personalitySnapshot.getKey();
                                    personalityId = personalitySnapshot1.child("personalityname").getValue(String.class);
                                    //Toast.makeText(PostFormActivity.this, "personname ="+personalityId, Toast.LENGTH_SHORT).show();
                                    if (personalityId == personalityName)
                                    {
                                        personalityId = personalitySnapshot1.getKey();
                                        break;
                                    }
                                    else
                                    {
                                        personalityId = "cant get you!";
                                    }
                                }

                                articleName = articleNameText.getText().toString();
                                bgcolor = colorSpinner.getSelectedItem().toString();
                                boolean encrypt = encryptCheckBox.isChecked();
                                String encryptString = Boolean.toString(encrypt);
                                //int color = Color.parseColor(bgcolor);
                                //colorView.setBackgroundColor(color);


                                //Toast.makeText(PostFormActivity.this, "pid: "+personalityId, Toast.LENGTH_SHORT).show();

                                //sendUserToPosActivity();

                                if (!articleName.isEmpty()) {
                                    Intent postIntent = new Intent(PostFormActivity.this, PostActivity.class);
                                    postIntent.putExtra("personalityname", personalityName);
                                    postIntent.putExtra("articletitle", articleName);
                                    postIntent.putExtra("personalityid", personalityId);
                                    postIntent.putExtra("articleBgColor", bgcolor);
                                    postIntent.putExtra("encryptcheckbox", encryptString);
                                    startActivity(postIntent);
                                }
                                else {
                                    Toast.makeText(PostFormActivity.this, "please enter the article title", Toast.LENGTH_SHORT).show();
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(PostFormActivity.this, MainActivity.class);
        startActivity(mainIntent);
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
