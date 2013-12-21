package server.status.ui.checker;

import server.status.R;
import server.status.check.Http;
import android.view.View;
import android.widget.EditText;

public class HttpEditDialog extends CheckerEditDialog {
	private EditText editText1;
	private EditText editText2;

	@Override
	public int getResource() {
		return R.layout.checker_http;
	}

	@Override
	protected void updateChecker() {
		if (editText1 != null && editText2 != null) {
			try {
				Http http = (Http) checker;
				int port = Integer.parseInt(editText1.getText().toString());
				int responseCode = Integer.parseInt(editText2.getText()
						.toString());
				http.setPort(port);
				http.setResponseCode(responseCode);
			} catch (NumberFormatException e) {
			} catch (ClassCastException e) {
			}
		}
	}

	@Override
	protected void updateEditView(View view) {
		editText1 = (EditText) view.findViewById(R.id.editText1);
		editText2 = (EditText) view.findViewById(R.id.editText2);
		try {
			Http http = (Http) checker;
			editText1.setText(Integer.toString(http.getPort()));
			editText2.setText(Integer.toString(http.getResponseCode()));
		} catch (ClassCastException e) {
		}
	}
}
