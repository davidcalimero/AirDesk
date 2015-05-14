package pt.ulisboa.tecnico.cmov.airdesk.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.HashSet;

import pt.ulisboa.tecnico.cmov.airdesk.ListActivity;
import pt.ulisboa.tecnico.cmov.airdesk.MainMenu;
import pt.ulisboa.tecnico.cmov.airdesk.R;
import pt.ulisboa.tecnico.cmov.airdesk.dto.TextFileDto;
import pt.ulisboa.tecnico.cmov.airdesk.dto.UserDto;
import pt.ulisboa.tecnico.cmov.airdesk.dto.WorkspaceDto;
import pt.ulisboa.tecnico.cmov.airdesk.listener.ConnectionHandler;
import pt.ulisboa.tecnico.cmov.airdesk.listener.WorkspacesChangeListener;
import pt.ulisboa.tecnico.cmov.airdesk.utility.FlowManager;
import pt.ulisboa.tecnico.cmov.airdesk.utility.FlowProxy;

public class ForeignFragment extends ExpandableListFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_foreign, container, false);
        setHasOptionsMenu(true);

        final String userId = FlowManager.getActiveUserID(getActivity().getApplicationContext());

        setListener(new WorkspacesChangeListener() {
            @Override
            public void onUserLeft(UserDto userDto) {
                removeOwner(userDto.id);
            }

            @Override
            public void onWorkspaceAdded(WorkspaceDto workspaceDto) {
                if (!userId.equals(workspaceDto.owner)) {
                    addWorkspace(workspaceDto.owner, workspaceDto.name);
                    for (TextFileDto textFileDto : workspaceDto.files)
                        addFile(textFileDto.owner, textFileDto.workspace, textFileDto.title);
                }
            }

            @Override
            public void onWorkspaceRemoved(WorkspaceDto workspaceDto) {
                if (!userId.equals(workspaceDto.owner))
                    removeWorkspace(workspaceDto.owner, workspaceDto.name);
            }

            @Override
            public void onFileAdded(TextFileDto textFileDto) {
                if (!userId.equals(textFileDto.owner)) {
                    addFile(textFileDto.owner, textFileDto.workspace, textFileDto.title);
                }
            }

            @Override
            public void onFileRemoved(TextFileDto textFileDto) {
                if (!userId.equals(textFileDto.owner)) {
                    removeFile(textFileDto.owner, textFileDto.workspace, textFileDto.title);
                }
            }

            @Override
            public void onFileContentChange(TextFileDto textFileDto) {}
        });

        makeAdapter((ExpandableListView) view.findViewById(R.id.foreignListView), R.layout.list_group_foreign, R.layout.list_item);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_foreign, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_refresh:
                refreshView(getView());
                break;

            case R.id.action_subscriptions:
                HashSet<String> subscriptions = FlowManager.getSubscriptions(getActivity().getApplicationContext());
                Intent intent = new Intent(getActivity().getApplicationContext(), ListActivity.class);
                intent.putExtra(ListActivity.LIST, subscriptions);
                intent.putExtra(ListActivity.TITLE, getString(R.string.subscriptions));
                getActivity().startActivityForResult(intent, MainMenu.SUBSCRIPTIONS);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public void refreshView(View view){
        final ProgressDialog dialog;
        dialog = new ProgressDialog(getActivity());
        dialog.setTitle(getString(R.string.dialog_please_wait));
        dialog.setCancelable(false);
        dialog.setIndeterminate(false);
        dialog.setMessage(getString(R.string.dialog_loading_workspaces));
        dialog.show();
        makeAdapter((ExpandableListView) view.findViewById(R.id.foreignListView), R.layout.list_group_foreign, R.layout.list_item);
        UserDto dto = new UserDto();
        dto.id = FlowManager.getActiveUserID(getActivity().getApplicationContext());
        dto.subscriptions = FlowManager.getSubscriptions(getActivity().getApplicationContext());
        FlowProxy.getInstance().send_subscribe(getActivity().getApplicationContext(), dto, new ConnectionHandler<UserDto>() {
            @Override
            public void onSuccess(UserDto result) {
                dialog.dismiss();
            }
            @Override
            public void onFailure() {
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.error_connection_lost_try_again_later), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }
}