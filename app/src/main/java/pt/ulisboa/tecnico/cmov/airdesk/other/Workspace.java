package pt.ulisboa.tecnico.cmov.airdesk.other;


import android.content.Context;
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

    /* Workspace Owner */
    private String _owner = "";

    /* User List */
    private ArrayList<String> _users = new ArrayList<>();

    /* Public Profile */
    private ArrayList<String> _tags = new ArrayList<>();

    /* Privacy */
    private MODE _privacy = MODE.PRIVATE;

    /* Maximum Quota */
    private long _maximumQuota = 0;

    /* Actual Quota */
    private long _quota = 0;

    /*********************************/
    /********** CONSTRUCTOR **********/
    /*********************************/

    /* Workspace */
    public Workspace(String name, String owner, MODE privacy, long quota){
        _name = name;
        _owner = owner;
        _privacy = privacy;
        _maximumQuota = quota;
        _quota = 0;
    }

    /*********************************/
    /****** GETTERS AND SETTERS ******/
    /*********************************/

    /* Name */
    public String getName() {
        return _name;
    }

    /* Owner */
    public String getOwnerID(){
        return _owner;
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

    /* Maximum Quota */
    public long getMaximumQuota() {
        return _maximumQuota;
    }

    public void setMaximumQuota(long quota){
        _maximumQuota = quota;
    }

    /* Actual Quota */
    public long getQuota(){
        return _quota;
    }

    public void incrementQuota(long amount) {
        _quota += amount;
    }

    public void decrementQuota(long amount) {
        _quota -= amount;
    }

    /* Equals */
    @Override
    public boolean equals(Object o) {
        if(o instanceof Workspace){
            Workspace workspace = (Workspace) o;
            if(this.getOwnerID().equals((workspace.getOwnerID())) && this.getName().equals(workspace.getName()))
                return true;
        }
        return super.equals(o);
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

    //region CENAS AINDA NÃO TESTADAS

    /*********************************/
    /******** FILE MANAGEMENT ********/
    /*********************************/

    public boolean addFile(Context context, String title, String content){
        if(!verifySpace(content))
            return false;
        String path = getWorkspacePath(context);
        if(!FileManager.createFile(path, title + ".txt"))
            return false;
        path += FileManager.LINE_SEP + title + ".txt";
        if(!FileManager.writeFile(path, content))
            return false;
        incrementQuota(content.length());
        return true;
    }

    public boolean removeFile(Context context, String filename){
        String fileLocation = getWorkspacePath(context) + FileManager.LINE_SEP + filename + ".txt";
        long amount = FileManager.getUsedSpace(fileLocation);
        FileManager.deleteFile(fileLocation);
        decrementQuota(amount);
        return true;
    }

    /*********************************/
    /************* UTILS *************/
    /*********************************/

    private String getWorkspacePath(Context context){
        return context.getFilesDir().getAbsolutePath() + FileManager.LINE_SEP + _owner + FileManager.LINE_SEP + _name;
    }

    public boolean verifySpace(String content){
        return !(content.length() + _quota > _maximumQuota);
    }

    //endregion
}
