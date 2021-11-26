package com.translateit.translateit.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.translateit.translateit.R;
import com.translateit.translateit.utils.FireBaseMethods;
import com.translateit.translateit.utils.NetworkCheck;
import com.translateit.translateit.utils.QueryUtils;

import java.util.ArrayList;

import static com.translateit.translateit.utils.GlobalVars.BASE_REQ_URL;
import static com.translateit.translateit.utils.GlobalVars.DEFAULT_LANG_POS;
import static com.translateit.translateit.utils.GlobalVars.LANGUAGE_CODES;

public class CompleteReg extends AppCompatActivity {

    volatile boolean activityRunning;
    private Spinner mSpinnerLanguage;
    private EditText edUserName;
    private View netErrorView;
    private String userName;
    private String language;
    private Button butGo;
    private String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_reg);

        activityRunning=true;

        getIncomingIntent();
        initWidgets();
        checkNetwork();

        butGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getData();
            }
        });
        mSpinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                language=LANGUAGE_CODES.get(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                Toast.makeText(getApplicationContext(), "No option selected", Toast.LENGTH_SHORT).show();

            }
        });
    }


    private void initWidgets()
    {
        mSpinnerLanguage = (Spinner) findViewById(R.id.spLanguage);
        netErrorView=(View)findViewById(R.id.netErrorView);
        butGo=(Button)findViewById(R.id.butComplete);
        edUserName=(EditText)findViewById(R.id.edUserName);


    }

    private void getIncomingIntent()
    {
        phone = getIntent().getStringExtra(getString(R.string.intent_number));

    }


    protected void onResume() {
        super.onResume();
        checkNetwork();
    }

    private void getData()
    {
        userName=edUserName.getText().toString();
        /*Validation*/
        FireBaseMethods.addUser(userName,language,phone,getApplicationContext());




    }

    private void checkNetwork()
    {
        NetworkCheck net = new NetworkCheck();

        if (net.isNetworkAvailable(CompleteReg.this))
        {
            new GetLanguages().execute();



        } else
        {
            Snackbar snackbar=Snackbar.make(netErrorView,"No network,please enable your data network to continue",Snackbar.LENGTH_LONG);
            snackbar.show();

        }

    }

    private class GetLanguages extends AsyncTask<Void,Void, ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            Uri baseUri = Uri.parse(BASE_REQ_URL);
            Uri.Builder uriBuilder = baseUri.buildUpon();
            uriBuilder.appendPath("languages")
                    .appendQueryParameter("key",getString(R.string.Yandex_Api))
                    .appendQueryParameter("target","en");
            Log.e("String Url ---->",uriBuilder.toString());
            return QueryUtils.fetchLanguages(uriBuilder.toString());
        }
        @Override
        protected void onPostExecute(ArrayList<String> result) {
            if (activityRunning) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(CompleteReg.this, android.R.layout.simple_spinner_item, result);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mSpinnerLanguage.setAdapter(adapter);

                //  SET DEFAULT LANGUAGE SELECTIONS
                mSpinnerLanguage.setSelection(DEFAULT_LANG_POS);

            }
        }
    }
}
