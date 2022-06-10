package com.sonya.filmdiary;

import android.app.Application;
import android.content.Intent;

import com.google.android.material.color.DynamicColors;
import com.google.firebase.database.FirebaseDatabase;

public class FilmDiary extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        DynamicColors.applyToActivitiesIfAvailable(this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
