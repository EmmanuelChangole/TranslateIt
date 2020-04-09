package com.translateit.translateit.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.translateit.translateit.ui.MainActivity;
import com.translateit.translateit.R;
import com.translateit.translateit.ui.RegisterActivity;
import com.translateit.translateit.models.User;
import com.translateit.translateit.ui.CompleteReg;

import java.util.concurrent.TimeUnit;

public class FireBaseMethods extends AppCompatActivity {
    private static FirebaseAuth mAuth;
    private static Context context;
    private String codeSent;
    private PhoneAuthProvider phoneAuthProvider;
    private FirebaseDatabase mRefDatabase;
    private static DatabaseReference mRefDatabaseRef;
    private String phoneNumber;
    private static String userId;
    private FirebaseAuth.AuthStateListener mAuthLitener;
    private static ProgressDialog mDialog;


    public FireBaseMethods(Context context) {
        mAuth = FirebaseAuth.getInstance();
        mRefDatabase = FirebaseDatabase.getInstance();
        mRefDatabaseRef = mRefDatabase.getReference();
        phoneAuthProvider = PhoneAuthProvider.getInstance();
        this.context = context;
        mDialog = new ProgressDialog(context);
        mDialog.setTitle("Loading");
        mDialog.setMessage("please wait");


    }

    public void sendVerificationCode(String number) {
        phoneNumber = number;
        phoneAuthProvider.verifyPhoneNumber(
                number,//phone number
                30,// duration
                TimeUnit.SECONDS,// duration in seconds
                TaskExecutors.MAIN_THREAD,//Threads for duration
                mCallBack//mCallBack
        );

    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            codeSent = s;
        }

        @Override

        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

            String code = phoneAuthCredential.getSmsCode();
            if (code != null)
                verifyCode(code);


        }

        @Override
        public void onVerificationFailed(FirebaseException e) {

            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();

        }
    };

    public void verifyCode(String code) {
        try{
            mDialog.show();
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeSent, code);
            signWithCredentials(credential);
        }

        catch (Exception e)
        {
            mDialog.dismiss();
            Toast.makeText(context,e.getMessage() , Toast.LENGTH_SHORT).show();
        }


    }

    private void signWithCredentials(PhoneAuthCredential credential) {


        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    mDialog.dismiss();
                    Intent intent = new Intent(context.getApplicationContext(), CompleteReg.class);
                    intent.putExtra(context.getString(R.string.intent_number), phoneNumber);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(intent);

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mDialog.dismiss();
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void addUser(String userName,String langauge,String phoneNumber,Context context) {

        userId = mAuth.getCurrentUser().getUid();
        User users = new User(userId, userName, "default", "online",userName.toLowerCase(), langauge, phoneNumber);
        mRefDatabaseRef.child(context.getString(R.string.dbName_users)).child(userId).setValue(users);

        Intent intent = new Intent(context.getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);


    }

    public void initFirebase() {

        mAuthLitener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                checkerLoggedIn(user);

            }
        };


    }

    public boolean onChangeState() {
        mAuth.addAuthStateListener(mAuthLitener);
       return checkerLoggedIn(mAuth.getCurrentUser());

    }

    public void clearState() {
        if (mAuthLitener != null) {
            mAuth.removeAuthStateListener(mAuthLitener);
        }

    }

    public boolean checkerLoggedIn(FirebaseUser currentUser) {
        if (currentUser == null) {
            Intent login = new Intent(context, RegisterActivity.class);
            login.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(login);
            return false;

        }

    return true;
    }
}
