package pt.ulisboa.tecnico.cmov.airdesk.utility;

public interface ConnectionHandler<T> {
    void onSuccess(T result);
    void onFailure();
}
