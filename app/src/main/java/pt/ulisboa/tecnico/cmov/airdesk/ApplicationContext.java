package pt.ulisboa.tecnico.cmov.airdesk;

import android.app.Application;

import pt.ulisboa.tecnico.cmov.airdesk.other.User;

public class ApplicationContext extends Application{

    private User activeUser;

    public User getActiveUser() {
        return activeUser;
    }

    public void setActiveUser(User activeUser) {
        this.activeUser = activeUser;
    }

    public boolean hasActiveUser(){
        return activeUser != null;
    }
}
