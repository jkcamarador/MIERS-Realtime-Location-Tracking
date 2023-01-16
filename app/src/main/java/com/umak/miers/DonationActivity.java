package com.umak.miers;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;

public class DonationActivity extends AppCompatActivity {

    WebView donatePaypal;
    ImageButton back_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation);

        showDialogMessage();
        loadDonatePage();

        back_button = findViewById(R.id.imageButtonBack2);

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void loadDonatePage() {
        donatePaypal = findViewById(R.id.webViewPaypal);

        WebSettings webSettings = donatePaypal.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        donatePaypal.setWebViewClient(new MyWebViewClient());
        donatePaypal.loadUrl("https://www.paypal.com/paypalme/lozer14");
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            donatePaypal.loadUrl(url);
            return true;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && donatePaypal.canGoBack()) {
            donatePaypal.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showDialogMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DonationActivity.this);

        builder.setMessage("Hi from the Zeltron Team!\n\n" +
                "Thank you for using our MIERS App!\n\n" +
                "Zeltron Enterprise has developers that depend on your contributions and support. " +
                "In order for the app to continue working, you might want to consider giving a donation to us.");

        builder.setTitle("Do you want to donate?");

        builder.setCancelable(false);

        builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog, which) -> {
        });

        builder.setNegativeButton("No", (DialogInterface.OnClickListener) (dialog, which) -> {
            finish();
//            dialog.cancel();
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}