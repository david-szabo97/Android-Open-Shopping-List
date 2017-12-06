package com.messedcode.openshoppinglist.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import com.messedcode.openshoppinglist.ActiveUsersUpdater;
import com.messedcode.openshoppinglist.R;
import com.messedcode.openshoppinglist.ui.list.ShoppingListActivity;

public class UsersDialogFragment extends AppCompatDialogFragment implements ActiveUsersUpdater.OnUserConnectionChanged {

    public static final String ARG_NAMES = "names";

    private ArrayAdapter<String> adapter;
    private ActiveUsersUpdater activeUsersUpdater;

    public static UsersDialogFragment newInstance(ArrayList<String> names) {
        UsersDialogFragment f = new UsersDialogFragment();

        Bundle args = new Bundle();
        args.putStringArrayList("names", names);
        f.setArguments(args);

        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        ListView view = (ListView) inflater.inflate(R.layout.dialog_users, null);
        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, getArguments().getStringArrayList(ARG_NAMES));
        view.setAdapter(adapter);

        builder.setView(view)
                .setTitle(R.string.dialog_users_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // TODO: Remove, add to newInstance
        ShoppingListActivity activity = (ShoppingListActivity) context;
        activeUsersUpdater = activity.activeUsersUpdater;
        activeUsersUpdater.addListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        activeUsersUpdater.removeListener(this);
        adapter = null;
    }

    @Override
    public void onConnected(String connectedName) {

    }

    @Override
    public void onDisconnected(String disconnectedName) {

    }

    @Override
    public void onActivesChanged(ArrayList<String> actives) {
        adapter.clear();
        adapter.addAll(actives);
    }

}
