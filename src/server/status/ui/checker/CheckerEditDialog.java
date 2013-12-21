package server.status.ui.checker;

import server.status.R;
import server.status.check.Checker;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;

public abstract class CheckerEditDialog extends DialogFragment {
	public static final String INTENT_CHECKER = "ichk";
	private static final String BUNDLE_CHECKER = "bchk";

	public interface CheckerEditDialogListener {
		public void onCheckerEditDone(Checker checker);
	}

	protected Checker checker;
	private CheckerEditDialogListener listener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Make sure activity implements listener
		try {
			listener = (CheckerEditDialogListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement CheckerEditDialogListener");
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		updateChecker();
		outState.putParcelable(BUNDLE_CHECKER, checker);
		super.onSaveInstanceState(outState);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Activity activity = getActivity();
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		if (savedInstanceState != null) {
			checker = savedInstanceState.getParcelable(BUNDLE_CHECKER);
		} else {
			Bundle args = getArguments();
			if (args != null) {
				checker = args.getParcelable(INTENT_CHECKER);
			}
		}
		builder.setTitle(getString(R.string.checker_edit_title,
				checker.getName(activity)));
		View view = activity.getLayoutInflater().inflate(getResource(), null);
		updateEditView(view);
		builder.setView(view);
		builder.setPositiveButton(R.string.button_ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				updateChecker();
				listener.onCheckerEditDone(checker);
			}
		});
		builder.setNegativeButton(R.string.button_cancel, null);
		return builder.create();
	}

	public abstract int getResource();

	protected abstract void updateChecker();

	protected abstract void updateEditView(View view);

	public static CheckerEditDialog getEditDialog(Checker checker) {
		switch (checker.getType()) {
		case HTTP:
			return new HttpEditDialog();
		case HTTPS:
			// TODO
		case PING:
			// TODO
		case SOCKET:
			// TODO
		default:
			// Will cause null pointer exception
			return null;
		}
	}
}
