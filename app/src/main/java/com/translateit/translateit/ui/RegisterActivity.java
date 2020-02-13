package com.translateit.translateit.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.hbb20.CountryCodePicker;
import com.translateit.translateit.R;
import com.translateit.translateit.utils.NetworkCheck;

public class RegisterActivity extends AppCompatActivity {

    private String TAG = "ActivityRegister";
    private CountryCodePicker countryCodePicker;
    private Button butRegister;
    private EditText edNumber;
    private FirebaseAuth mAuth;
    private View contentView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        intWidgets();
        checkNetwork();
    }

    private void checkNetwork()
    {
        NetworkCheck net = new NetworkCheck();

        if (net.isNetworkAvailable(RegisterActivity.this))
        {
            getItems();

        } else
        {
            Snackbar snackbar=Snackbar.make(contentView,"No network,please enable your data network to continue",Snackbar.LENGTH_LONG);
            snackbar.show();
            butRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkNetwork();
                }
            });
        }

    }


    /*Method that listen to a click listener*/
    private void getItems() {
        butRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNumber();
            }
        });

    }

    /*Method used to get number from widgets*/
    private void getNumber() {

        String num = edNumber.getText().toString();
        if (num.isEmpty() || num.length() != 9) {
            edNumber.setError(getString(R.string.edNumber_error));
            edNumber.requestFocus();
            return;
        }

        String number = "+" + countryCodePicker.getSelectedCountryCodeAsInt() + num;

        Intent intent = new Intent(this, ActivityOtp.class);
        intent.putExtra(getString(R.string.intent_number), number);
        startActivity(intent);


    }

    /*Method used to intilizie the widgets*/
    private void intWidgets() {
        countryCodePicker = (CountryCodePicker) findViewById(R.id.countryCodeHolder);
        butRegister = (Button) findViewById(R.id.butRegister);
        edNumber = (EditText) findViewById(R.id.edNumber);
        contentView=(View)findViewById(R.id.contentView);


    }

    @Override
    protected void onResume() {
        super.onResume();
        checkNetwork();

    }
}
