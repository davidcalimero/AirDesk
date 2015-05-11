package pt.ulisboa.tecnico.cmov.airdesk.utility;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public abstract class MyAsyncTask<BackgroundType, ProgressType, PostType> extends Handler {

    private static String POST_EXECUTE = "postExecute";
    private static String PROGRESS_UPDATE = "progressUpdate";

    private Thread thread = null;

    @Override
    public void handleMessage(Message msg) {
        byte[] data = msg.getData().getByteArray(PROGRESS_UPDATE);
        if(data != null)
            onProgressUpdate((ProgressType) Utils.bytesToObject(data));

        data = msg.getData().getByteArray(POST_EXECUTE);
        if(data != null)
            onPostExecute((PostType) Utils.bytesToObject(data));
}

    protected abstract PostType doInBackground(BackgroundType param);

    protected void onProgressUpdate(ProgressType param){}

    protected void onPostExecute(PostType param){}

    public void publishProgress(ProgressType param){
        Bundle bundle = new Bundle();
        bundle.putByteArray(PROGRESS_UPDATE, Utils.objectToBytes(param));
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
                bundle.putSerializable(POST_EXECUTE, Utils.objectToBytes(doInBackground(param)));
                Message msg = obtainMessage();
                msg.setData(bundle);
                sendMessage(msg);
            }
        };
        thread.start();
    }
}
