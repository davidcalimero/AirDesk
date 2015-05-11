package pt.ulisboa.tecnico.cmov.airdesk.dto;

import java.util.HashSet;

public class UserDto implements Dto {

    public String id;
    public HashSet<String> subscriptions;

    @Override
    public String toString() {
        return id + " " + (subscriptions != null ? subscriptions.size() : 0);
    }
}
