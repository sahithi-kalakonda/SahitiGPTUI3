package com.example.chatgptuiapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    EditText promptEditText;
    TextView response;
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        promptEditText = findViewById(R.id.prompt);
        Button send = findViewById(R.id.send);
        Button cancel = findViewById(R.id.cancel);
        response = findViewById(R.id.response);
        progressBar = findViewById(R.id.progressBar);


        send.setOnClickListener(view -> {
            String prompt = promptEditText.getText().toString().trim();
            if (!prompt.isEmpty()) {
                // Send prompt to OpenAI API
                progressBar.setVisibility(View.VISIBLE);
                new GenerateResponseTask().execute(prompt);
            } else {
                Toast.makeText(MainActivity.this, "Please enter a prompt", Toast.LENGTH_SHORT).show();
            }
        });

        cancel.setOnClickListener(view -> {
            promptEditText.setText("");
            response.setText("");
        });
    }

    private class GenerateResponseTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String prompt = params[0];
            String apiKey = "";
            String apiUrl = "https://api.openai.com/v1/completions";
            String model = "gpt-3.5-turbo-instruct";

            try {
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + apiKey);

                JSONObject jsonRequest = new JSONObject();
                jsonRequest.put("prompt", prompt);
                jsonRequest.put("model", model);
                jsonRequest.put("max_tokens", 50);

                conn.setDoOutput(true);
                conn.getOutputStream().write(jsonRequest.toString().getBytes());

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                return response.toString();

            } catch (IOException | JSONException e) {
                Log.e("EXCEPTION",e.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    String generatedText = jsonResponse.getJSONArray("choices").getJSONObject(0).getString("text");
                    response.setText(generatedText);
                    progressBar.setVisibility(View.GONE);
                } catch (JSONException e) {
                    Log.e("EXCEPTION",e.toString());
                    response.setText("Error parsing response");
                }
            } else {
                response.setText("Error connecting to server");
            }
        }
    }
}

