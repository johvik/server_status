package server.status.ui;

import server.status.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.EditText;

public class ServerHostDialog extends DialogFragment {
	public static final String INTENT_HOST = "ho";
	private static final String BUNDLE_HOST = "host";
	private EditText editText;

	public interface ServerHostListener {
		public void onHostChange(String host);
	}

	private ServerHostListener listener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Make sure activity implements listener
		try {
			listener = (ServerHostListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement ServerHostListener");
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(BUNDLE_HOST, editText.getText().toString());
		super.onSaveInstanceState(outState);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Activity activity = getActivity();
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle(getString(R.string.server_host_change));
		editText = new EditText(activity);
		editText.setHint(R.string.hint_host_change);
		String host = null;
		// Get saved host or just arguments
		if (savedInstanceState != null) {
			host = savedInstanceState.getString(BUNDLE_HOST);
		} else {
			Bundle args = getArguments();
			if (args != null) {
				host = args.getString(INTENT_HOST);
			}
		}
		if (host != null) {
			editText.setText(host);
		}
		builder.setView(editText);
		builder.setPositiveButton(R.string.button_ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				listener.onHostChange(editText.getText().toString());
			}
		});
		builder.setNegativeButton(R.string.button_cancel, null);
		return builder.create();
	}
}
