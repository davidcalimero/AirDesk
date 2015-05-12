package pt.ulisboa.tecnico.cmov.airdesk.utility;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public abstract class MyAsyncTask<BackgroundType, ProgressType, PostType> extends Handler {

    private final static int POST_EXECUTE = 1;
    private final static int PROGRESS_UPDATE = 2;

    private Thread thread = null;

    public MyAsyncTask(){
        super(Looper.getMainLooper());
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.arg1){
            case PROGRESS_UPDATE:
                onProgressUpdate((ProgressType) msg.obj);
                break;
            case POST_EXECUTE:
                onPostExecute((PostType) msg.obj);
                break;
        }
    }

    protected abstract PostType doInBackground(BackgroundType param);

    protected void onProgressUpdate(ProgressType param){}

    protected void onPostExecute(PostType param){}

    public void publishProgress(ProgressType param){
        Message msg = obtainMessage();
        msg.arg1 = PROGRESS_UPDATE;
        msg.obj = param;
        sendMessage(msg);
    }

    public void cancel(){
        thread.interrupt();
    }

    public void execute(final BackgroundType param){
        if(thread == null) {
            thread = new Thread() {
                @Override
                public void run() {
                    Message msg = obtainMessage();
                    msg.arg1 = POST_EXECUTE;
                    msg.obj = doInBackground(param);
                    sendMessage(msg);
                }
            };
        }
        thread.start();
    }
}
