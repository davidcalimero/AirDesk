package pt.ulisboa.tecnico.cmov.airdesk.other;


import android.content.Context;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import pt.ulisboa.tecnico.cmov.airdesk.exception.AlreadyExistsException;
import pt.ulisboa.tecnico.cmov.airdesk.exception.OutOfMemoryException;

public class Workspace implements Serializable {

    /*********************************/
    /************ MACROS *************/
    /**
     * *****************************
     */

    /* Name of the workspace. */
    private String _name = "";

    /*********************************/
    /*********** VARIABLES ***********/
    /* Workspace Owner */
    private String _owner = "";
    /* User List */
    private ArrayList<CharSequence> _users = new ArrayList<>();
    /* Public Profile */
    private ArrayList<CharSequence> _tags = new ArrayList<>();
    /* File List */
    private HashMap<String, TextFile> _files = new HashMap<>();
    /* Privacy */
    private PRIVACY _privacy = PRIVACY.PRIVATE;
    /* Maximum Quota */
    private long _maximumQuota = 0;
    /* Actual Quota */
    private long _quota = 0;

    /**
     * *****************************
     */

    /* Workspace */
    public Workspace(String name, String owner, PRIVACY privacy, long quota) {
        _name = name;
        _owner = owner;
        _privacy = privacy;
        _maximumQuota = quota;
        _quota = 0;
    }

    /*********************************/
    /********** CONSTRUCTOR **********/

    /**
     * *****************************
     */

    /* Name */
    public String getName() {
        return _name;
    }

    /*********************************/
    /****** GETTERS AND SETTERS ******/

    /* Owner */
    public String getOwnerID() {
        return _owner;
    }

    /* User List */
    public ArrayList<CharSequence> getUserList() {
        return _users;
    }

    public void setUserList(ArrayList<CharSequence> users) {
        Log.e("Workspace", "User List Updated: " + getName());
        _users.clear();
        _users = users;
    }

    /* Public Profile */
    public ArrayList<CharSequence> getTagList() {
        return _tags;
    }

    public void setTagList(ArrayList<CharSequence> tags) {
        Log.e("Workspace", "public profile changed: " + getName());
        _tags.clear();
        _tags = tags;
    }

    /* Files List */
    public HashMap<String, TextFile> getFiles() {
        return _files;
    }

    /* Privacy */
    public PRIVACY getPrivacy() {
        return _privacy;
    }

    public void setPrivacy(PRIVACY privacy) {
        Log.e("Workspace", "privacy changed to: " + getName() + ": " + privacy.name());
        _privacy = privacy;
    }

    /* Maximum Quota */
    public long getMaximumQuota() {
        return _maximumQuota;
    }

    public void setMaximumQuota(long quota) {
        Log.e("Workspace", "quota changed to: " + getName() + ": " + quota);
        _maximumQuota = quota;
    }

    /* Actual Quota */
    public long getQuota() {
        return _quota;
    }

    private void incrementQuota(long amount) {
        _quota += amount;
    }

    private void decrementQuota(long amount) {
        _quota -= amount;
    }

    /* Equals */
    @Override
    public boolean equals(Object object) {
        if (object instanceof Workspace) {
            Workspace workspace = (Workspace) object;
            if (this.getOwnerID().equals((workspace.getOwnerID())) && this.getName().equals(workspace.getName()))
                return true;
        }
        return false;
    }

    /**
     * *****************************
     */

    public void addFile(Context context, String fileName, String title, String content) throws AlreadyExistsException, OutOfMemoryException {
        int size = content.length();

        if (_files.containsKey(title))
            throw new AlreadyExistsException();

        if (!haveSpace(size))
            throw new OutOfMemoryException();

        incrementQuota(size);
        _files.put(title, new TextFile(context, fileName, title, content));
        Log.e("Workspace", "file added: " + title + "[quota]: " + getQuota());

    }

    /*********************************/
    /******** FILE MANAGEMENT ********/

    public void removeFile(Context context, String name) {
        TextFile file = _files.remove(name);
        decrementQuota(file.getContent(context).length());
        file.delete(context);
        Log.e("Workspace", "file removed: " + name + "[quota]: " + getQuota());
    }

    public void editFile(Context context, String fileName, String newContent) throws OutOfMemoryException {
        TextFile file = _files.get(fileName);
        int sizeDif = newContent.length() - file.getContent(context).length();
        if (!haveSpace(sizeDif))
            throw new OutOfMemoryException();

        incrementQuota(sizeDif);
        file.setContent(context, newContent);
    }

    /**
     * *****************************
     */

    /*private String getWorkspacePath(Context context){
        return context.getFilesDir().getAbsolutePath() + FileManager.LINE_SEP + _owner + FileManager.LINE_SEP + _name;
    }*/
    public boolean haveSpace(int contentSize) {
        return contentSize + getQuota() <= getMaximumQuota();
    }

    /*********************************/
    /************* UTILS *************/
    /**
     * *****************************
     */

    /* Privacy */
    public enum PRIVACY {
        PUBLIC, PRIVATE
    }
}
