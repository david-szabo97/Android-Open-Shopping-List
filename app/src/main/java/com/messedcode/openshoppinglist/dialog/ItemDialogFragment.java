package com.messedcode.openshoppinglist.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import com.messedcode.openshoppinglist.R;
import com.messedcode.openshoppinglist.utils.Utils;

public class ItemDialogFragment extends AppCompatDialogFragment {

    private ItemDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_item, null);

        builder.setView(view)
                .setTitle(R.string.dialog_item_title)
                .setCancelable(false)
                .setNegativeButton(getResources().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Utils.toggleKeyboard(getActivity());
                    }
                })
                .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                        Dialog dialog = (Dialog) dialogInterface;

                        TextInputLayout inputName = dialog.findViewById(R.id.input_item_name);
                        TextInputLayout inputDesc = dialog.findViewById(R.id.input_item_description);
                        TextInputLayout inputPrice = dialog.findViewById(R.id.input_item_price);
                        int price = 0;
                        try {
                            double priceDouble = Double.parseDouble(inputPrice.getEditText().getText().toString());
                            price = (int) (priceDouble * 100);
                        } catch (NumberFormatException e) {
                        }
                        CheckBox inputUrgent = dialog.findViewById(R.id.checkbox_item_urgent);
                        listener.onDialogAddItem(
                                inputName.getEditText().getText().toString(),
                                inputDesc.getEditText().getText().toString(),
                                price,
                                inputUrgent.isChecked()
                        );
                        Utils.toggleKeyboard(getActivity());
                    }
                });

        Dialog d = builder.create();

        d.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialogInterface) {
                Utils.toggleKeyboard(getActivity());
            }
        });

        return d;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        listener = (ItemDialogListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        listener = null;
    }

    public interface ItemDialogListener {
        void onDialogAddItem(String inputName, String inputDescription, int inputPrice, boolean inputUrgent);
    }

}
