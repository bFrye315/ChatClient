package edu.jsu.mcis.cs408.chatclient;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import edu.jsu.mcis.cs408.chatclient.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ChatClientViewModel model;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        model = new ViewModelProvider(this).get(ChatClientViewModel.class);

        model.sendGetRequest();
        final Observer<JSONObject> jsonObserver = new Observer<JSONObject>() {
            @Override
            public void onChanged(@Nullable final JSONObject newJson) {
                if(newJson != null) {
                    try {
                        setOutputText(model.jsonParse(newJson));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        model.getJsonData().observe(this, jsonObserver);

        binding.postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                model.setMessage(binding.input.getText().toString());
                model.sendPostRequest();
            }
        });
        binding.clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                model.sendClearRequest();
            }
        });
    }

    private void setOutputText(String s){
        binding.chatLog.setText(s);
    }
}