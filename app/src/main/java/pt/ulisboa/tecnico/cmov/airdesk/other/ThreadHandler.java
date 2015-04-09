package pt.ulisboa.tecnico.cmov.airdesk.other;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.Serializable;

import pt.ulisboa.tecnico.cmov.airdesk.R;

public abstract class ThreadHandler<T extends Serializable> extends Handler {

    private static String MESSAGE = "message";
    private ProgressDialog dialog;

    public ThreadHandler(Context context) {
        super();
        this.dialog = new ProgressDialog(context);
        this.dialog.setTitle(context.getString(R.string.dialog_please_wait));
        this.dialog.setCancelable(false);
        this.dialog.setIndeterminate(false);
    }

    public abstract T start();

    @Override
    public void handleMessage(Message msg) {
        onFinish((T) msg.getData().getSerializable(MESSAGE));
    }

    public abstract void onFinish(T result);

    public static void startWorkerThread(String message, final ThreadHandler runnable){
        runnable.dialog.setMessage(message);
        runnable.dialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                /*try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                Bundle bundle = new Bundle();
                bundle.putSerializable(MESSAGE, runnable.start());
                runnable.dialog.dismiss();
                Message msg = runnable.obtainMessage();
                msg.setData(bundle);
                runnable.sendMessage(msg);
            }
        }, message).start();
    }
}
