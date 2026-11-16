package com.cesia.misfechas;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Al reiniciar el celular se reprograman todos los avisos.
        Plan.programarAvisos(context);
    }
}
