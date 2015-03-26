package pt.ulisboa.tecnico.cmov.airdesk.other;


import android.util.Log;

import java.util.ArrayList;

public class Workspace {

    /*********************************/
    /************ MACROS *************/
    /*********************************/

    /* Privacy */
    public enum MODE {PUBLIC, PRIVATE}

    /*********************************/
    /*********** VARIABLES ***********/
    /*********************************/

    /* Name of the workspace. */
    private String _name = "";

    /* User List */
    private ArrayList<String> _users = new ArrayList<>();

    /* Public Profile */
    private ArrayList<String> _tags = new ArrayList<>();

    /* Privacy */
    private MODE _privacy = MODE.PRIVATE;

    /* Quota */
    private float _quota = 0;

    /*********************************/
    /********** CONSTRUCTOR **********/
    /*********************************/

    /* Workspace */
    public Workspace(String name, MODE privacy, float quota){
        _name = name;
        _privacy = privacy;
        _quota = quota;
    }

    /*********************************/
    /****** GETTERS AND SETTERS ******/
    /*********************************/

    /* Name */
    public String getName() {
        return _name;
    }

    /* User List */
    public ArrayList<String> getUserList() {
        return _users;
    }

    /* Public Profile */
    public ArrayList<String> getPublicProfile() {
        return _tags;
    }

    /* Privacy */
    public MODE getPrivacy() {
        return _privacy;
    }

    public void setPrivacy(MODE privacy) {
        _privacy = privacy;
    }

    /* Quota */
    public float getQuota() {
        return _quota;
    }

    public void setQuota(float quota) {
        // TODO - Possivelmente necessário completar esta informação
        _quota = quota;
    }

    /*********************************/
    /******** LIST MANAGEMENT ********/
    /*********************************/

    /* User List */
    public boolean removeUser(String id){
        return _users.remove(id);
    }

    public boolean addUser(String id){
        try{
            return _users.add(id);
        } catch (Exception e){
            Log.e("Workspace", "Can't add user. Had " + e.toString());
            return false;
        }
    }

    /* Public Profile */
    //region DEPRECATED
    // Seems like it's deprecated :P
    public boolean addTag(String tag){
        try{
            return _tags.add(tag);
        } catch (Exception e){
            Log.e("User", "Can't add keyword. Had " + e.toString());
            return false;
        }
    }

    public boolean removeTag(String tag){
        return _tags.remove(tag);
    }
    //endregion
}
