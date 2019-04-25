package com.airzac.inspire;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

import net.dankito.richtexteditor.android.RichTextEditor;
import net.dankito.richtexteditor.android.toolbar.AllCommandsEditorToolbar;
import net.dankito.richtexteditor.callback.GetCurrentHtmlCallback;
import net.dankito.utils.android.permissions.IPermissionsService;
import net.dankito.utils.android.permissions.PermissionsService;

import java.util.HashMap;
import java.util.Map;

public class EditArticleActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RichTextEditor editor;
    private AllCommandsEditorToolbar editorToolbar;
    private Button updateArticleButton;
    private String finalhtml, articleid, htmlText, personalityName,personalityId, articleTitle, articleBgColor;
    private String currentUserId, identifier;
    private DatabaseReference htmlRef, postsRef;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private IPermissionsService permissionsService = new PermissionsService(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_article);

        mAuth = FirebaseAuth.getInstance();
        mToolbar = (Toolbar) findViewById(R.id.add_post_toolbar);
        updateArticleButton = (Button) findViewById(R.id.edit_article_button);
        currentUserId = mAuth.getCurrentUser().getUid();
        //userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Edit Your Article");

        articleTitle = getIntent().getStringExtra("articletitle");
        finalhtml = getIntent().getStringExtra("artihtml");
        identifier = getIntent().getStringExtra("identifier");

        editor = (RichTextEditor) findViewById(R.id.editor);

        editorToolbar = (AllCommandsEditorToolbar) findViewById(R.id.editorToolbar);
        editorToolbar.setEditor(editor);

        editor.setEditorFontSize(20);
        editor.setEditorFontSize(20);
        editor.setPadding((int) (4 * getResources().getDisplayMetrics().density));
        //int color = Color.parseColor(articleBgColor);
        //editor.setEditorBackgroundColor(color);

        if (finalhtml.isEmpty())
        {
            editor.setHtml("<p><b>THIS ARTICLE HAS NO CONTENT</b>...you can begin writing here<p>");

        }
         else
        {editor.setHtml(finalhtml);
        }

        editor.focusEditorAndShowKeyboardDelayed();

        updateArticleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
            }
        });
    }

    private void save() {
        editor.getCurrentHtmlAsync(new GetCurrentHtmlCallback() {

            @Override
            public void htmlRetrieved(@NotNull String html) {
                saveHtml(html);
            }
        });
    }

    private void saveHtml(String html) {
        // ...
        htmlText=html;
        /*finalhtml = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<title>"+articleTitle+"</title>" +
                "<style>" +
                "body {background-color:"+ articleBgColor+";}" +
                "</style>" +
                "</head>" +
                "<body>" +
                htmlText+
                "</body>" +
                "</html>";*/
        //Toast.makeText(this, htmlText, Toast.LENGTH_SHORT).show();

        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(identifier);
        //postsRef.child("articlehtml").setValue(htmlText).toString();
        Map<String, Object> updates = new HashMap<String,Object>();
        updates.put("articlehtml", htmlText);
        postsRef.updateChildren(updates);
        //Toast.makeText(this, postsRef.toString(), Toast.LENGTH_SHORT).show();
        //Toast.makeText(this, articleid , Toast.LENGTH_SHORT).show();
        postsRef.child("published").setValue(0);
        Toast.makeText(this, "your article has been updated.", Toast.LENGTH_LONG).show();
    }

}
