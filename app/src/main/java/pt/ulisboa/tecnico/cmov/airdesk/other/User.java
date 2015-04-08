package pt.ulisboa.tecnico.cmov.airdesk.other;

import android.content.Context;
import android.util.Log;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

import pt.ulisboa.tecnico.cmov.airdesk.exception.AlreadyExistsException;

public class User implements Serializable {

    /*********************************/
    /*********** VARIABLES ***********/
    /*********************************/

    /* E-mail. Used as ID. */
    private String _email;

    /* Nickname */
    private String _nickname;

    /* Owned Workspace List */
    private HashMap<String, Workspace> _workspaceList = new HashMap<>();

    /* Keywords List */
    private HashSet<CharSequence> _subscriptions = new HashSet<>();

    /*********************************/
    /********** CONSTRUCTOR **********/
    /*********************************/

    public User(String email, String nickname) {
        _email = email;
        _nickname = nickname;
    }

    /*********************************/
    /****** GETTERS AND SETTERS ******/
    /*********************************/

    /* Client ID. The client Email is the identifier.*/
    public String getID() {
        return _email;
    }

    public String getNickname() {
        return _nickname;
    }

    /* Client Owned Workspace List */
    public HashMap<String, Workspace> getWorkspaces() {
        return _workspaceList;
    }

    /* Client Keyword List */
    public HashSet<CharSequence> getSubscriptions() {
        return _subscriptions;
    }

    public void setSubscriptions(HashSet<CharSequence> value) {
        Log.e("User", "subscriptions modified");
        _subscriptions = value;
    }

    /*********************************/
    /******** LIST MANAGEMENT ********/
    /*********************************/

    public void addWorkspace(Workspace workspace) throws AlreadyExistsException {
        if (_workspaceList.containsKey(workspace.getName()))
            throw new AlreadyExistsException();

        Log.e("User", "workspace added: " + workspace.getName());
        _workspaceList.put(workspace.getName(), workspace);
    }

    /* Workspace List */
    public void removeWorkspace(String name, Context context) {
        _workspaceList.remove(name).delete(context);
    }
}


