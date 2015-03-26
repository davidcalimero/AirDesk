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
    private ArrayList<String> _workspaceList = new ArrayList<>();

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

    /* Client Workspace List */
    public ArrayList<String> getWorkspaceList() {
        return _workspaceList;
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
    public boolean removeWorkspace(String name){
        return _workspaceList.remove(name);
    }

    public boolean addWorkspace(String name){
        try{
            return _workspaceList.add(name);
        } catch (Exception e){
            Log.e("User", "Can't add keyword. Had " + e.toString());
            return false;
        }
    }
    //endregion
}
