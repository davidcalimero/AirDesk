package pt.ulisboa.tecnico.cmov.airdesk.utility;


import android.content.Context;
import android.util.Log;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

import pt.ulisboa.tecnico.cmov.airdesk.exception.AlreadyExistsException;
import pt.ulisboa.tecnico.cmov.airdesk.exception.OutOfMemoryException;

public class Workspace implements Serializable {

    /* Privacy */
    public enum PRIVACY { PUBLIC, PRIVATE }

    /*********************************/
    /*********** VARIABLES ***********/
    /*********************************/
    /* Name of the workspace. */
    private String _name = "";
    /* User List */
    private HashSet<String> _users = new HashSet<>();
    /* Public Profile */
    private HashSet<String> _tags = new HashSet<>();
    /* File List */
    private HashMap<String, TextFile> _files = new HashMap<>();
    /* Privacy */
    private PRIVACY _privacy = PRIVACY.PRIVATE;
    /* Maximum Quota */
    private long _maximumQuota = 0;
    /* Actual Quota */
    private long _currentSize = 0;


    /*********************************/
    /********** CONSTRUCTOR **********/
    /*********************************/
    /* Workspace */
    public Workspace(String name, PRIVACY privacy, long quota) {
        _name = name;
        _privacy = privacy;
        _maximumQuota = quota;
    }

    /*********************************/
    /****** GETTERS AND SETTERS ******/
    /*********************************/
    /* Name */
    public String getName() {
        return _name;
    }

    /* User List */
    public HashSet<String> getUsers() {
        return _users;
    }

    public void setUsers(HashSet<String> users){
        Log.e("Workspace", "users changed: " + getName());
        _users = users;
    }

    public void addUser(String id)  {
        Log.e("Workspace", "User added: " + id);
        _users.add(id);
    }

    public void removeUser(String id){
        Log.e("Workspace", "User removed: " + id);
        _users.remove(id);
    }

    /* Public Profile */
    public HashSet<String> getTags() {
        return _tags;
    }

    public void setTags(HashSet<String> tags) {
        Log.e("Workspace", "tags changed: " + getName());
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
        Log.e("Workspace", "privacy changed to: " + privacy.name());
        _privacy = privacy;
    }

    /* Maximum Quota */
    public long getMaximumQuota() {
        return _maximumQuota;
    }

    public void setMaximumQuota(long quota) {
        Log.e("Workspace", "max quota changed to: " + quota);
        _maximumQuota = quota;
    }

    /* Actual Quota */
    public long getCurrentMemorySize() {
        return _currentSize;
    }

    private void incrementMemorySize(long amount) {
        _currentSize += amount;
    }

    private void decrementMemorySize(long amount) {
        _currentSize -= amount;
    }

    /*********************************/
    /******** FILE MANAGEMENT ********/
    /*********************************/

    public void addFile(Context context, String fileName, String title, String content) throws AlreadyExistsException, OutOfMemoryException {
        int size = content.length();

        if (_files.containsKey(title))
            throw new AlreadyExistsException();

        if (!haveSpace(size))
            throw new OutOfMemoryException();

        incrementMemorySize(size);
        _files.put(title, new TextFile(context, fileName, title, content));
        Log.e("Workspace", "file added: " + title + "[quota]: " + getCurrentMemorySize());

    }

    public void removeFile(Context context, String name) {
        TextFile file = _files.remove(name);
        decrementMemorySize(file.getContent(context).length());
        file.delete(context);
        Log.e("Workspace", "file removed: " + name + "[quota]: " + getCurrentMemorySize());
    }

    public void editFile(Context context, String fileName, String newContent) throws OutOfMemoryException {
        TextFile file = _files.get(fileName);
        int sizeDif = newContent.length() - file.getContent(context).length();
        if (!haveSpace(sizeDif))
            throw new OutOfMemoryException();

        incrementMemorySize(sizeDif);
        file.setContent(context, newContent);
        Log.e("Workspace", "file edited: " + fileName + "[quota]: " + getCurrentMemorySize());
    }

    public void delete(Context context){
        Log.e("Workspace", "workspace deleted: " + getName());
        for(TextFile f : _files.values())
            f.delete(context);
    }

    /**
     * *****************************
     */

    public boolean haveSpace(int contentSize) {
        return ((contentSize + getCurrentMemorySize() <= getMaximumQuota()) && (FileManager.getInternalFreeSpace() > contentSize));
    }
}
