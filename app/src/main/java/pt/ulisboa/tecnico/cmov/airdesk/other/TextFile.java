package pt.ulisboa.tecnico.cmov.airdesk.other;

import android.content.Context;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.Serializable;

public class TextFile implements Serializable{

    private String title;
    private String filename;
    private String text = null;

    public TextFile(Context context, String filename, String title, String content){
        this.filename = filename;
        this.title = title;
        setContent(context, content);
    }

    public TextFile(String filename, String title, String content){
        this.filename = filename;
        this.title = title;
        this.text = content;
    }

    public String getTitle() {
        return title;
    }

    public String getContent(Context context) {
        if(text != null)
            return text;

        String content = "";
        try {
            content =  (String) FileManager.fileToObject(filename, context);
        } catch (FileNotFoundException e) {
            Log.e("TextFile", "File " + title + "not found");
        }
        return content;
    }

    public void setContent(Context context, String content) {
        FileManager.objectToFile(filename, content, context);
    }

    public void saveContent(Context context){
        setContent(context, text);
    }

    public void delete(Context context){
        Log.e("TextFile", "File deleted: " + getTitle());
        context.deleteFile(filename);
    }
}
