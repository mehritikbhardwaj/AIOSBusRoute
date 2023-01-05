package com.app.aiosbusroute;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.app.aiosbusroute.common.Constants;
import com.app.aiosbusroute.common.Utilities;
import com.app.aiosbusroute.common.Utils;
import com.app.aiosbusroute.retrofit.ApiService;
import com.app.aiosbusroute.retrofit.RetroClient;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LoginActivity extends AppCompatActivity {
    Button btn_login;
    ProgressDialog pd;
    EditText et_password, et_BusNumber;
    TextView login;
    Spinner spinner_Route;
    String route;
    ArrayList<String> spinnerArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btn_login = findViewById(R.id.btn_validate);
        spinner_Route = findViewById(R.id.spinner_Route);
        et_password = findViewById(R.id.et_password);
        et_BusNumber = findViewById(R.id.et_BusNumber);
        pd = new ProgressDialog(this);
        pd.setCancelable(false);
        initializeSpinner();
        login = findViewById(R.id.login);


        btn_login.setOnClickListener(view -> {
            if (et_password.getText().toString().isEmpty() || et_BusNumber.getText().toString().isEmpty()) {
                Toast.makeText(this, "Please enter bus number and password.", Toast.LENGTH_SHORT).show();
            } else {
                if (Utils.isNetworkAvailable(this)) {
                    callLoginApi();
                } else {
                    Toast.makeText(this, "Please check your internet connection!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void callLoginApi() {
        pd.setMessage("Please Wait...");
        pd.show();
        ApiService apiService = RetroClient.getApiService();

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("apiusername", Constants.API_USER_NAME);
        headers.put("apipassword", Constants.API_PASSWORD);
        headers.put("uid", "0");

        Call<LoginModel> call = apiService.BusInsertUpdate(headers,
                et_password.getText().toString(), Integer.valueOf(route), et_BusNumber.getText().toString());

        call.enqueue(new Callback<LoginModel>() {
            @Override
            public void onResponse(Call<LoginModel> call, Response<LoginModel> response) {
                if (pd.isShowing())
                    pd.dismiss();

                if (response.isSuccessful()) {
                    LoginModel model = response.body();

                    Toast.makeText(LoginActivity.this, model.getStatus(), Toast.LENGTH_SHORT).show();
                    if (model.getStatus().equalsIgnoreCase("success")) {
                        Utilities.setStringPreference(getApplicationContext(), Constants.IS_LOGGED_IN,
                                "YES", Constants.PREF_NAME);

                        Utilities.setStringPreference(getApplicationContext(), Constants.BUS_ID,
                                model.getId().toString(), Constants.PREF_NAME);

                        Intent intent = new Intent(getApplicationContext(), BrowserActivity.class);
                        startActivity(intent);
                        finish();
                    } else if (model.getStatus().equalsIgnoreCase("")) {
                        Toast.makeText(LoginActivity.this, "Invalid", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginModel> call, Throwable t) {
                if (pd.isShowing()) {
                    pd.dismiss();
                }
                Log.e("ONFAILURE", t.toString());
            }
        });
    }


    private void initializeSpinner() {
        // populate arraylist with data
        for (int i = 1; i <= 10; i++) {
            spinnerArrayList.add(String.valueOf(i));
        }
        // create spinner adapter with the above arraylist
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, spinnerArrayList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attach adapter to spinner
        spinner_Route.setAdapter(dataAdapter);
        spinner_Route.setOnItemSelectedListener(onItemSelectedListener1); // Here we call the function which is getting the value according to selected text.


    }

    AdapterView.OnItemSelectedListener onItemSelectedListener1 =
            new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int position, long id) {
                    route = spinnerArrayList.get(position);
                    //    Toast.makeText(LoginActivity.this, route, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            };


}
