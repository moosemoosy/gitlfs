package com.airzac.inspire;

import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ViewArticleActivity extends AppCompatActivity {

    private WebView webView;
    private ImageView lock;
    private TextView encryptedText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_article);
        lock = (ImageView) findViewById(R.id.encrypted_lock);
        lock.setVisibility(View.GONE);
        encryptedText = (TextView) findViewById(R.id.encrypted_text_msg);
        encryptedText.setVisibility(View.GONE);

        final String html= getIntent().getStringExtra("artihtml");
        String encrypted = getIntent().getStringExtra("encrypted");
        final String decryptkey = getIntent().getStringExtra("decryptkey");
        if (html.contains("<iframe")) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        webView = (WebView) findViewById(R.id.webview);
        webView.setWebChromeClient(new WebChromeClient());
        //Toast.makeText(this, "html: "+html, Toast.LENGTH_LONG).show();
        webView.loadDataWithBaseURL(null, html,"text/html", "UTF-8", null);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        if (encrypted.equals("true")){
            lock.setVisibility(View.VISIBLE);
            encryptedText.setVisibility(View.VISIBLE);

            lock.setOnClickListener(new View.OnClickListener() {

                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(View view) {
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        String decrypted = AES.decrypt(html, decryptkey);
                        webView.loadDataWithBaseURL(null, decrypted, "text/html", "UTF-8", null);
                        lock.setVisibility(View.GONE);
                        encryptedText.setVisibility(View.GONE);
                    }
                    else {
                        Toast.makeText(ViewArticleActivity.this, "Your Android Version Doesn't Support Decryption, Please Upgrade to Android 8.0+", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }

    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Shows the system bars by removing all the flags
// except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }


}
