package pt.ulisboa.tecnico.cmov.airdesk.utility;

import android.content.Context;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.Serializable;

public class TextFile implements Serializable {

    private String title;
    private String filename;
    private boolean availability = true;
    private String userEditing = "";

    public TextFile(Context context, String filename, String title, String content) {
        this.filename = filename;
        this.title = Utils.trim(title);
        setContent(context, content);
    }

    public String getTitle() {
        return title;
    }

    public String getContent(Context context) {
        String content = "";
        try {
            content = (String) FileManager.fileToObject(filename, context);
        } catch (FileNotFoundException e) {
            Log.e("TextFile", "File " + title + " not found");
        }
        return content;
    }

    public void setContent(Context context, String content) {
        FileManager.objectToFile(filename, content, context);
    }

    public void delete(Context context) {
        Log.e("TextFile", "File deleted: " + getTitle());
        context.deleteFile(filename);
    }

    public boolean isAvailable(){
        return availability;
    }

    public void setAvailability(String userEditing, boolean isAvailable){
        availability = isAvailable;
        this.userEditing = userEditing;
    }

    public String getUserEditing(){
        return userEditing;
    }
}
