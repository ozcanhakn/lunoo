package com.lumoo;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

public class RateThisApp {

    public static void openRateDialog(final Context context) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + context.getPackageName())));
        } catch (ActivityNotFoundException e1) {
            try {
                context.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
            } catch (ActivityNotFoundException e2) {
                Toast.makeText(context, "Uygulama mağazası bulunamadı", Toast.LENGTH_SHORT).show();
            }
        }
    }
}