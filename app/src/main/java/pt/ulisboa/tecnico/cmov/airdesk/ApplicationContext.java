package pt.ulisboa.tecnico.cmov.airdesk;

import android.app.Application;
import android.util.Log;

import java.io.FileNotFoundException;

import pt.ulisboa.tecnico.cmov.airdesk.exception.InvalidInputException;
import pt.ulisboa.tecnico.cmov.airdesk.other.FileManager;
import pt.ulisboa.tecnico.cmov.airdesk.other.User;
import pt.ulisboa.tecnico.cmov.airdesk.other.Utils;

public class ApplicationContext extends Application {

    private User activeUser;

    public User getActiveUser() {
        return activeUser;
    }

    public void loadUser(String email, String nickName) throws InvalidInputException {
        if (!Utils.isSingleWord(nickName) || !Utils.isSingleWord(email))
            throw new InvalidInputException();

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
