package pt.ulisboa.tecnico.cmov.airdesk.other;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import pt.ulisboa.tecnico.cmov.airdesk.exception.AlreadyExistsException;

public class User implements Serializable {

    /*********************************/
    /*********** VARIABLES ***********/
    /**
     * *****************************
     */

    /* E-mail. Used as ID. */
    private String _email;

    /* Nickname */
    private String _nickname;

    /* Owned Workspace List */
    private HashMap<String, Workspace> _workspaceList = new HashMap<>();

    /* Keywords List */
    private ArrayList<CharSequence> _subscriptions = new ArrayList<>();

    /*********************************/
    /********** CONSTRUCTOR **********/
    /**
     * *****************************
     */

    public User(String mail, String nick) {
        _email = mail;
        _nickname = nick;
    }

    /*********************************/
    /****** GETTERS AND SETTERS ******/
    /**
     * *****************************
     */

    /* Client ID. The client Email is the identifier.*/
    public String getID() {
        return _email;
    }

    /* Client Email */
    public String getEmail() {
        return _email;
    }

    /* Client Nickname */
    public String getNickname() {
        return _nickname;
    }

    /* Client Owned Workspace List */
    public HashMap<String, Workspace> getWorkspaceList() {
        return _workspaceList;
    }

    /* Client Keyword List */
    public ArrayList<CharSequence> getSubscriptions() {
        return _subscriptions;
    }

    public void setSubscriptions(ArrayList<CharSequence> value) {
        Log.e("User", "subscriptions modified");
        _subscriptions.clear();
        _subscriptions = value;
    }

    /*********************************/
    /******** LIST MANAGEMENT ********/
    /**
     * *****************************
     */

    /* Workspace List */
    public void removeWorkspace(String name) {
        Log.e("User", "workspace removed: " + name);
        _workspaceList.remove(name);
    }

    public void addWorkspace(Workspace workspace) throws AlreadyExistsException {
        if (_workspaceList.containsKey(workspace.getName()))
            throw new AlreadyExistsException();
        Log.e("User", "workspace added: " + workspace.getName());
        _workspaceList.put(workspace.getName(), workspace);
    }
}


