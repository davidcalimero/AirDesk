package pt.ulisboa.tecnico.cmov.airdesk.listener;

public interface ConnectionHandler<T> {
    void onSuccess(T result);
    void onFailure();
}
