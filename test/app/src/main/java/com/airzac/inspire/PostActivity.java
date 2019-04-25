package com.airzac.inspire;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
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
import com.google.firebase.database.annotations.NotNull;

import net.dankito.richtexteditor.android.RichTextEditor;
import net.dankito.richtexteditor.android.toolbar.AllCommandsEditorToolbar;
import net.dankito.richtexteditor.callback.GetCurrentHtmlCallback;
import net.dankito.utils.android.permissions.IPermissionsService;
import net.dankito.utils.android.permissions.PermissionsService;

import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RichTextEditor editor;
    private AllCommandsEditorToolbar editorToolbar;
    private Button savePostButton;
   // private TextView htmlTextView;
    private String finalhtml, htmlText, personalityName,personalityId, articleTitle, articleBgColor, secretKey;
    private String currentDate, currentTime, randPostName, currentUserId;
    private String encrypt;
    private DatabaseReference userRef, postsRef;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private IPermissionsService permissionsService = new PermissionsService(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        personalityName= getIntent().getStringExtra("personalityname");
        articleTitle= getIntent().getStringExtra("articletitle");
        personalityId= getIntent().getStringExtra("personalityid");
        articleBgColor = getIntent().getStringExtra("articleBgColor");
        encrypt = getIntent().getStringExtra("encryptcheckbox");

        final Boolean encrypted = false;
        Toast.makeText(this, "color:" +articleBgColor, Toast.LENGTH_SHORT).show();
        //Toast.makeText(this, articleTitle, Toast.LENGTH_SHORT).show();
        mAuth = FirebaseAuth.getInstance();
        mToolbar = (Toolbar) findViewById(R.id.add_post_toolbar);
        currentUserId = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Add New Post");

        savePostButton = (Button) findViewById(R.id.save_post_button);
        loadingBar = new ProgressDialog(this);

        editor = (RichTextEditor) findViewById(R.id.editor);
        //htmlTextView = (TextView) findViewById(R.id.html_text_view);

        editorToolbar = (AllCommandsEditorToolbar) findViewById(R.id.editorToolbar);
        editorToolbar.setEditor(editor);

        editor.setEditorFontSize(20);
        editor.setEditorFontSize(20);
        editor.setPadding((int) (4 * getResources().getDisplayMetrics().density));
        int color = Color.parseColor(articleBgColor);
        editor.setEditorBackgroundColor(color);

        editor.setPlaceholder("start writing here...");

        // some properties you also can set on editor
        // editor.setEditorBackgroundColor(Color.YELLOW);
        // editor.setEditorFontColor(Color.MAGENTA);
        // editor.setEditorFontFamily("cursive");

        // show keyboard right at start up
        editor.focusEditorAndShowKeyboardDelayed();

        savePostButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {

                Calendar calFordDate = Calendar.getInstance();
                SimpleDateFormat getcurrentDate = new SimpleDateFormat("dd-MM-yyyy");
                currentDate = getcurrentDate.format(calFordDate.getTime());

                Calendar calFordTime = Calendar.getInstance();
                SimpleDateFormat getcurrentTime = new SimpleDateFormat("HH:mm");
                currentTime = getcurrentTime.format(calFordTime.getTime());

                randPostName = currentDate + currentTime;
                //htmlText=editor.getCurrentHtmlAsync();
                save();
                //htmlTextView.setText(htmlText);
                //Toast.makeText(getBaseContext(), "uploading", Toast.LENGTH_SHORT).show();
                loadingBar.setTitle("Uploading");
                loadingBar.setMessage("please wait, article uploading...");
                loadingBar.show();
                savePostToDatabase();
            }
        });

    }

    private void save() {
        editor.getCurrentHtmlAsync(new GetCurrentHtmlCallback() {

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void htmlRetrieved(@NotNull String html) {
                saveHtml(html);
            }
        });
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void saveHtml(String html) {
        // ...
        htmlText=html;
        finalhtml = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<title>"+articleTitle+"</title>" +
                "<style>" +
                "body {background-color:"+ articleBgColor+";font-size: 20px;}" +
                "</style>" +
                "</head>" +
                "<body>" +
                htmlText+
                "</body>" +
                "</html>";

        if (encrypt.equals("true")) {
            secretKey = RandomString.getAlphaNumericString(12);
            final String encryptedhtml = AES.encrypt(finalhtml, secretKey);
            finalhtml = encryptedhtml;
            Toast.makeText(this, finalhtml, Toast.LENGTH_LONG).show();
        }
        else {
            secretKey = "";
            Toast.makeText(this, "encrypt is not checked", Toast.LENGTH_SHORT).show();
        }
    }

    //encryption code here
    private void savePostToDatabase() {
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
                    postMap.put("encrypted", encrypt);
                    postMap.put("decryptkey", secretKey);

                    postsRef.child(personalityId + currentUserId + randPostName).updateChildren(postMap)
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task)
                                {

                                    if(task.isSuccessful())
                                    {
                                        sendUserToMainActivity();
                                        //editor.setHtml("<p>​hi Jarvis setup my day</p><p><br></p><p>Here&nbsp; is my program</p><p><br></p><h1><b>Get me <span style=\"background-color: rgb(255, 235, 59); color: rgb(255, 44, 147);\">all</span> i <span style=\"color: rgb(255, 44, 147); background-color: rgb(255, 235, 59);\">need</span> hey</b></h1><p><b><br></b></p>");
                                        Toast.makeText(PostActivity.this, "Article posted successfully.", Toast.LENGTH_LONG).show();
                                        //Toast.makeText(PostActivity.this, finalhtml, Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                    else
                                    {
                                        Toast.makeText(PostActivity.this, "Error occurred while posting your article.", Toast.LENGTH_LONG).show();
                                        loadingBar.dismiss();
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
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            sendUserToMainActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsService.onRequestPermissionsResult(requestCode, permissions, grantResults);

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }


}