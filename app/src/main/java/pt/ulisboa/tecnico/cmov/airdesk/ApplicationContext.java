package pt.ulisboa.tecnico.cmov.airdesk;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.FileNotFoundException;

import pt.ulisboa.tecnico.cmov.airdesk.other.FileManager;
import pt.ulisboa.tecnico.cmov.airdesk.other.User;

public class ApplicationContext extends Application {

    private User activeUser;

    public User getActiveUser() {
        if(activeUser == null) {
            Log.e("ApplicationContext", "User missing: reloading");
            SharedPreferences prefs = getSharedPreferences(LogInActivity.PREFERENCES, Context.MODE_PRIVATE);
            loadUser(prefs.getString(MainMenu.EMAIL, ""), prefs.getString(MainMenu.NICKNAME, ""));
        }
        return activeUser;
    }

    public void loadUser(String email, String nickName) {
        try {
            activeUser = (User) FileManager.fileToObject(email, getApplicationContext());
            Log.e("ApplicationContext", "user loaded: " + email + "," + nickName + ".");
        } catch (FileNotFoundException e) {
            activeUser = new User(email, nickName);
            Log.e("ApplicationContext", "user created: " + email + "," + nickName + ".");
        }
    }

    public void removeUser() {
        Log.e("ApplicationContext", "user unload: " + activeUser.getID());
        activeUser = null;
    }

    public void commit() {
        if (FileManager.objectToFile(activeUser.getID(), activeUser, getApplicationContext()))
            Log.e("User", "user committed:" + activeUser.getID());
    }
}
