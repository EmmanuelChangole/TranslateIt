package com.translateit.translateit.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.chaos.view.PinView;
import com.translateit.translateit.R;
import com.translateit.translateit.utils.FireBaseMethods;

public class ActivityOtp extends AppCompatActivity {
    /*Widgets*/
    private Button butSubmit;
    private PinView pinView;
    private TextView tvResend;
    private String number;
    private FireBaseMethods fireBaseMethods;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        getIncomingIntent();
        intWidgets();
        sendVerificationCode();
        submitCode();
        resendCode();
    }

    private void submitCode() {
        butSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = pinView.getText().toString().trim();
                fireBaseMethods.verifyCode(code);

            }
        });

    }

    private void resendCode() {
        tvResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fireBaseMethods.sendVerificationCode(number);
            }
        });

    }

    private void sendVerificationCode() {
        fireBaseMethods = new FireBaseMethods(this);
        fireBaseMethods.sendVerificationCode(number);

    }

    private void intWidgets() {
        butSubmit = (Button) findViewById(R.id.butSubmit);
        tvResend = (TextView) findViewById(R.id.tvResend);
        pinView= (PinView) findViewById(R.id.pinView);

    }

    private void getIncomingIntent() {
        number = getIntent().getStringExtra(getString(R.string.intent_number));
    }
}
