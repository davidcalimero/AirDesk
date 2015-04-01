package pt.ulisboa.tecnico.cmov.airdesk.other;

import android.content.Context;
import android.util.Log;

import java.io.FileNotFoundException;

public class TextFile {

    private String title;
    private String filename;
    private Context context;

    public TextFile(Context context, String ownerID, String workspaceName, String title, String  content){
        filename = ownerID + "-" + workspaceName + "-" + title;
        this.context = context;
        this.title = title;
        setContent(content);
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        String content = "";
        try {
            content =  (String) FileManager.fileToObject(filename, context);
        } catch (FileNotFoundException e) {
            Log.e("TextFile", "File " + title + "not found");
        }
        return content;
    }

    public void setContent(String content) {
        FileManager.objectToFile(filename, content, context);
    }
}
