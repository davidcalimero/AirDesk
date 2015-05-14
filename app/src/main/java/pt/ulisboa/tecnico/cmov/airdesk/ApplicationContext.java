package pt.ulisboa.tecnico.cmov.airdesk;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.IBinder;
import android.util.Log;

import java.io.FileNotFoundException;

import pt.ulisboa.tecnico.cmov.airdesk.listener.ConnectionHandler;
import pt.ulisboa.tecnico.cmov.airdesk.utility.FileManager;
import pt.ulisboa.tecnico.cmov.airdesk.utility.MyAsyncTask;
import pt.ulisboa.tecnico.cmov.airdesk.utility.TextFile;
import pt.ulisboa.tecnico.cmov.airdesk.utility.User;
import pt.ulisboa.tecnico.cmov.airdesk.utility.Workspace;
import pt.ulisboa.tecnico.cmov.airdesk.wifiDirect.RealWifiDirectService;
import pt.ulisboa.tecnico.cmov.airdesk.wifiDirect.SimWifiDirectService;
import pt.ulisboa.tecnico.cmov.airdesk.wifiDirect.WifiDirectService;

public class ApplicationContext extends Application {

    private User activeUser;

    private static final boolean usingSimWifiDirect = true;

    private WifiDirectService wifiDirectService = null;

    private ServiceConnection wifiDirectConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.e("ApplicationContext", "service connected" + className);
            wifiDirectService = ((WifiDirectService.WifiDirectBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.e("ApplicationContext", "service disconnected");
            wifiDirectService = null;
        }
    };

    public User getActiveUser() {
        if(activeUser == null) {
            Log.e("ApplicationContext", "User missing: reloading");
            SharedPreferences prefs = getSharedPreferences(LogInActivity.PREFERENCES, Context.MODE_PRIVATE);
            loadUser(prefs.getString(MainMenu.EMAIL, ""), prefs.getString(MainMenu.NICKNAME, ""));
        }
        return activeUser;
    }

    public WifiDirectService getWifiDirectService(){
        return wifiDirectService;
    }

    public void init(final String email, final String nickName, final ConnectionHandler<Boolean> handler){
        new MyAsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void param) {
                loadUser(email, nickName);
                startService();
                return null;
            }

            @Override
            protected void onPostExecute(Void param) {
                handler.onSuccess(true);
            }
        }.execute(null);
    }

    public void reset() {
        Log.e("ApplicationContext", "user unload: " + activeUser.getID());
        activeUser = null;
        unbindService(wifiDirectConnection);
    }


    private void loadUser(String email, String nickName) {
        try {
            activeUser = (User) FileManager.fileToObject(email, getApplicationContext());
            for(Workspace workspace : activeUser.getWorkspaces().values())
                for (TextFile textFile : workspace.getFiles().values())
                    textFile.setAvailability("", true);
            Log.e("ApplicationContext", "user loaded: " + email + "," + nickName + ".");
        } catch (FileNotFoundException e) {
            activeUser = new User(email, nickName);
            Log.e("ApplicationContext", "user created: " + email + "," + nickName + ".");
        }
    }

    private void startService(){
        if(usingSimWifiDirect)
            bindService(new Intent(getApplicationContext(), SimWifiDirectService.class), wifiDirectConnection, Context.BIND_AUTO_CREATE);
        else
            bindService(new Intent(getApplicationContext(), RealWifiDirectService.class), wifiDirectConnection, Context.BIND_AUTO_CREATE);
    }

    public void commit() {
        if (FileManager.objectToFile(activeUser.getID(), activeUser, getApplicationContext()))
            Log.e("User", "user committed:" + activeUser.getID());
    }
}
