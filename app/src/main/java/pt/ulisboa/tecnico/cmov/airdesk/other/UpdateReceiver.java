package pt.ulisboa.tecnico.cmov.airdesk.other;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class UpdateReceiver extends BroadcastReceiver {
    public UpdateReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("UpdateReceiver", "Received update notification");

        // Buscar informação ao intent, para fazer o update aos fragments

        //throw new UnsupportedOperationException("Not yet implemented");
    }
}
