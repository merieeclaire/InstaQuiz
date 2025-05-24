package com.merieclaire.instaquiz;

import android.app.Application;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;

public class InstaQuizApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        PDFBoxResourceLoader.init(getApplicationContext());
    }
}
