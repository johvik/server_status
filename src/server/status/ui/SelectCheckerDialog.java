package server.status.ui;

import server.status.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class SelectCheckerDialog extends DialogFragment {
	public interface SelectCheckerListener {
		public void onSelectChecker(int index);
	}

	private SelectCheckerListener listener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Make sure activity implements listener
		try {
			listener = (SelectCheckerListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement SelectCheckerListener");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(getString(R.string.action_server_add_checker));
		builder.setItems(R.array.checkers_array, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				listener.onSelectChecker(which);
			}
		});
		return builder.create();
	}
}
