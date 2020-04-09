package com.translateit.translateit.ui;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class MyFirebase extends Application
{
    @Override
    public void onCreate() {
        super.onCreate();
        /* Enable disk persistence  */
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
