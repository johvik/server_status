package server.status.ui;

import server.status.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ConfirmDialog extends DialogFragment {
	public static final String INTENT_TITLE = "title";
	public static final String INTENT_MESSAGE = "msg";

	public interface ConfirmDialogListener {
		public void onConfirm();
	}

	private ConfirmDialogListener listener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Make sure activity implements listener
		try {
			listener = (ConfirmDialogListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement ConfirmDialogListener");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Activity activity = getActivity();
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		String title = null;
		String message = null;
		Bundle args = getArguments();
		if (args != null) {
			title = args.getString(INTENT_TITLE);
			message = args.getString(INTENT_MESSAGE);
		}
		if (title != null) {
			builder.setTitle(title);
		}
		if (message != null) {
			builder.setMessage(message);
		}
		builder.setPositiveButton(R.string.button_ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				listener.onConfirm();
			}
		});
		builder.setNegativeButton(R.string.button_cancel, null);
		return builder.create();
	}
}
