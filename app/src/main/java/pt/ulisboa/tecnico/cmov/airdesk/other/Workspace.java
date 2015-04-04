package pt.ulisboa.tecnico.cmov.airdesk.other;


import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Workspace implements Serializable{

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
    private ArrayList<CharSequence> _users = new ArrayList<>();

    /* Public Profile */
    private ArrayList<CharSequence> _tags = new ArrayList<>();

    /* File List */
    private HashMap<String, TextFile> _files = new HashMap<>();

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
        _maximumQuota = 30;
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
    public ArrayList<CharSequence> getUserList() {
        if(_users.isEmpty())
            Log.e("Workspace", "HAS NOTHING!");
        return _users;
    }

    public void setUserList(ArrayList<CharSequence> users){
        if(users != null)
            Log.e("Workspace","IT HAS SOMETHING");
        //_users.clear();
        //_users = users;
        _users = new ArrayList<>(users);
        Log.e("Workspace","User List Updated.");
    }

    /* Public Profile */
    public ArrayList<CharSequence> getPublicProfile() {
        return _tags;
    }

    /* Files List */
    public HashMap<String, TextFile> getFiles() { return _files; }

    public void setPublicProfile(ArrayList<CharSequence> tags) {
        Log.e("Workspace", "public profile changed: " + getName());
        _tags.clear();
        _tags = tags;
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
    public boolean equals(Object object) {
        if(object instanceof Workspace){
            Workspace workspace = (Workspace) object;
            if(this.getOwnerID().equals((workspace.getOwnerID())) && this.getName().equals(workspace.getName()))
                return true;
        }
        return false;
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

    /*********************************/
    /******** EDIT WORKSPACE *********/
    /*********************************/

    public void editWorkspace(ArrayList<CharSequence> users, ArrayList<CharSequence> tags, MODE privacy, long quota){
        setUserList(users);
        setPublicProfile(tags);
        setPrivacy(privacy);
        setMaximumQuota(quota);
    }


    /*********************************/
    /******** FILE MANAGEMENT ********/
    /*********************************/

    public boolean addFile(Context context, String file, String filename, String content){
        Log.e("Workspace", "Current Quota = " + getQuota());
        if(!verifySpace(content.length())) {
            Toast.makeText(context, "Maximum Quota reached. Can't create new file.", Toast.LENGTH_SHORT).show();
            return false;
        }
        TextFile newFile = new TextFile(context, file, filename, content);

        _files.put(newFile.getTitle(), newFile);
        incrementQuota(content.length());

        Log.e("Workspace", "file added: " + newFile.getTitle());
        Log.e("Workspace", "Quota increased by " + content.length());
        Log.e("Workspace", "Current Quota = " + getQuota());
        Toast.makeText(context, "File successfully created.", Toast.LENGTH_SHORT).show();
        return true;

    }

    public boolean removeFile(Context context, String name){
        Log.e("Workspace", "file removed: " + name);
        int size = _files.get(name).getContent(context).length();
        decrementQuota(size);
        Log.e("Workspace","File removed: quota reduced by " + size);
        _files.remove(name).delete(context);
        return true;
    }

    public boolean editFile(Context context, String fileName, String newContent){
        String oldContent = getFiles().get(fileName).getContent(context);
        if(!verifySpace(newContent.length() - oldContent.length())){
            Toast.makeText(context, "Maximum Quota reached. Can't modify file.", Toast.LENGTH_SHORT).show();
            return false;
        }
        incrementQuota(newContent.length() - oldContent.length());
        getFiles().get(fileName).setContent(context, newContent);
        Toast.makeText(context, "File successfully edited.", Toast.LENGTH_SHORT).show();
        return true;
    }

    /*********************************/
    /************* UTILS *************/
    /*********************************/

    private String getWorkspacePath(Context context){
        return context.getFilesDir().getAbsolutePath() + FileManager.LINE_SEP + _owner + FileManager.LINE_SEP + _name;
    }

    public boolean verifySpace(int contentSize){
        return !(contentSize + getQuota() > getMaximumQuota());
    }
}
