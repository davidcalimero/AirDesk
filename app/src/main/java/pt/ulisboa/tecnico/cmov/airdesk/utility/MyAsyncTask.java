package pt.ulisboa.tecnico.cmov.airdesk.utility;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.Serializable;

public abstract class MyAsyncTask<BackgroundType, ProgressType extends Serializable, PostType extends Serializable> extends Handler {

    private static String POST_EXECUTE = "postExecute";
    private static String PROGRESS_UPDATE = "progressUpdate";

    private Thread thread = null;

    @Override
    public void handleMessage(Message msg) {
        Serializable data = msg.getData().getSerializable(PROGRESS_UPDATE);
        if(data != null)
            onProgressUpdate((ProgressType) data);

        data = msg.getData().getSerializable(POST_EXECUTE);
        if(data != null)
            onPostExecute((PostType) data);
}

    protected abstract PostType doInBackground(BackgroundType param);

    protected void onProgressUpdate(ProgressType param){}

    protected void onPostExecute(PostType param){}

    public void publishProgress(ProgressType param){
        Bundle bundle = new Bundle();
        bundle.putSerializable(PROGRESS_UPDATE, param);
        Message msg = obtainMessage();
        msg.setData(bundle);
        sendMessage(msg);
    }

    public void cancel(){
        thread.interrupt();
    }

    public void execute(final BackgroundType param){
        if(thread != null)
            cancel();

        thread = new Thread() {
            @Override
            public void run() {
                Bundle bundle = new Bundle();
                bundle.putSerializable(POST_EXECUTE, doInBackground(param));
                Message msg = obtainMessage();
                msg.setData(bundle);
                sendMessage(msg);
            }
        };
        thread.start();
    }
}
