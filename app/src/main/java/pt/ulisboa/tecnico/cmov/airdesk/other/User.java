package pt.ulisboa.tecnico.cmov.airdesk.other;

import android.content.Context;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class User implements Serializable{

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
    private ArrayList<CharSequence> _subscriptions = new ArrayList<>();

    /*********************************/
    /********** CONSTRUCTOR **********/
    /*********************************/

    private User(String mail, String nick){
        _email = mail;
        _nickname = nick;
    }

    /*********************************/
    /****** GETTERS AND SETTERS ******/
    /*********************************/

    /* Client ID. The client Email is the identifier.*/
    public String getID(){
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
    public ArrayList<CharSequence> getSubscriptions() { return _subscriptions; }

    public void setSubscriptions(ArrayList<CharSequence> value) {
        _subscriptions.clear();
        _subscriptions = value;
        Log.e("ForeignFragment", "subscriptions modified");
    }

    /*********************************/
    /******** LIST MANAGEMENT ********/
    /*********************************/

    /* Workspace List */
    public void removeWorkspace(String name){
        Log.e("User", "workspace removed: " + name);
        _workspaceList.remove(name);
    }

    public void addWorkspace(Workspace workspace){
        Log.e("User", "workspace added: " + workspace.getName());
        _workspaceList.put(workspace.getName(), workspace);
    }

    /*********************************/
    /******** USER MANAGEMENT ********/
    /*********************************/

    public static User LoadUser(String email, String nickName, Context context){
        User user;
        try {
            user = (User) FileManager.fileToObject(email, context);
            Log.e("User", "user loaded: " + user.getID());
        } catch (FileNotFoundException e) {
            user = new User(email, nickName);
            Log.e("User", "user created: " + user.getID());
        }
        return user;
    }

    public void commit(Context context){
        if(FileManager.objectToFile(getID(), this, context))
            Log.e("MainMenu", "user committed:" + getID());
    }
}


