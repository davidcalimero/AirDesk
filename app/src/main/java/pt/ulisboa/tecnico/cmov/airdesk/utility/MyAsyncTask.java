package pt.ulisboa.tecnico.cmov.airdesk.utility;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.Serializable;

public abstract class MyAsyncTask<T extends Serializable> extends Handler {

    private static String MESSAGE = "message";

    @Override
    public void handleMessage(Message msg) {
        onPostExecute((T) msg.getData().getSerializable(MESSAGE));
    }

    protected abstract T doInBackground();

    protected abstract void onPostExecute(T result);

    public static void start(final MyAsyncTask<Serializable> runnable){
        new Thread() {
            @Override
            public void run() {
                Bundle bundle = new Bundle();
                bundle.putSerializable(MESSAGE, runnable.doInBackground());
                Message msg = runnable.obtainMessage();
                msg.setData(bundle);
                runnable.sendMessage(msg);
            }
        }.start();
    }
}
