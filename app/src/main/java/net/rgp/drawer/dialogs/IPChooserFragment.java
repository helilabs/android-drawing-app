package net.rgp.drawer.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import net.rgp.drawer.R;
import net.rgp.drawer.listeners.OnNewIPSelectedListener;

public class IPChooserFragment extends DialogFragment {

    private String IP_ADDRESS;
    private OnNewIPSelectedListener mListener;
    private EditText ipField;

    public static IPChooserFragment newInstance(String IP) {
        IPChooserFragment fragment = new IPChooserFragment();
        Bundle args = new Bundle();
        if(!IP.equals("")){
            args.putString("ip_address", IP);
            fragment.setArguments(args);
        }
        return fragment;
    }

    public IPChooserFragment() {
        // Required empty public constructor
    }

    public void setOnNewIPChooserSelectedListener(OnNewIPSelectedListener listener){
           mListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null && args.containsKey("ip_address")){
            String ip_address = args.getString("ip_address","");
            if(!ip_address.equals("")){
                IP_ADDRESS = ip_address;
            }
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Begin building a new dialog.
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final LayoutInflater inflater = getActivity().getLayoutInflater();

        final View dialogView = inflater.inflate(R.layout.fragment_ipchooser, null);
        if (dialogView != null) {
            ipField = (EditText) dialogView.findViewById(R.id.ip_field);
            ipField.setText(IP_ADDRESS);
        }

        builder.setTitle("Choose new Brush Size")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onNewIPSelected(ipField.getText().toString());
                        dialog.dismiss();
                    }
                })
                .setView(dialogView);

        return builder.create();
    }

}
