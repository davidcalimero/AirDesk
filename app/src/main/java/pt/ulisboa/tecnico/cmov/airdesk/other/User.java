package pt.ulisboa.tecnico.cmov.airdesk.other;

import android.util.Log;

import java.util.ArrayList;

public class User {

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
    private ArrayList<String> _keywordList = new ArrayList<>();

    /*********************************/
    /********** CONSTRUCTOR **********/
    /*********************************/

    public User(String mail, String nick){
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
    public ArrayList<Workspace> geOwnedWorkspaceList() {
        return _ownedWorkspaceList;
    }

    /* Client Foreign Workspace List */
    public ArrayList<Workspace> geForeignWorkspaceList() {
        return _foreignWorkspaceList;
    }

    /* Client Keyword List */
    public ArrayList<String> getKeywordList() {
        return _keywordList;
    }

    /*********************************/
    /******** LIST MANAGEMENT ********/
    /*********************************/

    /* Keyword List */
    public boolean removeKeyword(String keyword){
        return _keywordList.remove(keyword);
    }

    public boolean addKeyword(String keyword){
        try{
            return _keywordList.add(keyword);
        } catch (Exception e){
            Log.e("User", "Can't add keyword. Had " + e.toString());
            return false;
        }
    }

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
}
