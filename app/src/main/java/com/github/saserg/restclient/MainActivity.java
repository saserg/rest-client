package com.github.saserg.restclient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.saserg.webclient.RestClientBuilder;

public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.data);
        button = findViewById(R.id.button);
        button.setOnClickListener(v -> fetchData());
    }

    private void fetchData() {
        String FAKE_API_SERVER = "https://mocki.io/v1/f8a66daa-18e7-4137-9d4b-b0661b31de32";
        RestClientBuilder.build(UserData.class)
                .url(FAKE_API_SERVER)
                .get()
                .success((object, headers, status) -> {
                    button.setVisibility(View.GONE);
                    textView.setText(object.toString());
                })
                .error((status, error) -> Log.e("REST_CLIENT_TAG", "Error message: " + error + " " + "Status server: " + status));

    }
}
