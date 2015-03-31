package pt.ulisboa.tecnico.cmov.airdesk.other;

import android.content.Context;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable{

    /*********************************/
    /*********** VARIABLES ***********/
    /*********************************/

    /* E-mail. Used as ID. */
    private String _email;

    /* Nickname */
    private String _nickname;

    /* Owned Workspace List */
    private ArrayList<Workspace> _ownedWorkspaceList = new ArrayList<>();

    /* Foreign Workspace List */
    private ArrayList<Workspace> _foreignWorkspaceList = new ArrayList<>();

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
    public ArrayList<Workspace> getOwnedWorkspaceList() {
        return _ownedWorkspaceList;
    }

    /* Client Foreign Workspace List */
    public ArrayList<Workspace> getForeignWorkspaceList() {
        return _foreignWorkspaceList;
    }

    /* Client Keyword List */
    public ArrayList<CharSequence> getSubscriptions() {
        return _subscriptions;
    }

    public void setSubscriptions(ArrayList<CharSequence> value) {
        _subscriptions.clear();
        _subscriptions = value;
    }

    /*********************************/
    /******** LIST MANAGEMENT ********/
    /*********************************/

    /* Workspace List */
    //region DEPRECATED
    public boolean removeWorkspace(Workspace workspace){
        if(_ownedWorkspaceList.contains(workspace))
            return _ownedWorkspaceList.remove(workspace);
        else
            return _foreignWorkspaceList.remove(workspace);
    }

    public boolean addWorkspace(Workspace workspace){
        try{
            if(workspace.getOwnerID().equals(getID()))
                return _ownedWorkspaceList.add(workspace);
            else
                return _foreignWorkspaceList.add(workspace);
        } catch (Exception e){
            Log.e("User", "Can't add workspace. Had " + e.toString());
            return false;
        }
    }
    //endregion

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
        FileManager.objectToFile(getID(), this, context);
    }
}
