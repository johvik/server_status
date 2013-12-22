package server.status.ui.checker;

import server.status.R;
import server.status.check.Https;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

public class HttpsEditDialog extends CheckerEditDialog {
	private EditText editText1;
	private EditText editText2;
	private CheckBox checkBox1;

	@Override
	public int getResource() {
		return R.layout.checker_https;
	}

	@Override
	protected void updateChecker() {
		if (editText1 != null && editText2 != null && checkBox1 != null) {
			try {
				Https https = (Https) checker;
				int port = Integer.parseInt(editText1.getText().toString());
				int responseCode = Integer.parseInt(editText2.getText()
						.toString());
				boolean allCertificates = checkBox1.isChecked();
				https.setPort(port);
				https.setResponseCode(responseCode);
				https.setAllCertificates(allCertificates);
			} catch (NumberFormatException e) {
			} catch (ClassCastException e) {
			}
		}
	}

	@Override
	protected void updateEditView(View view) {
		editText1 = (EditText) view.findViewById(R.id.editText1);
		editText2 = (EditText) view.findViewById(R.id.editText2);
		checkBox1 = (CheckBox) view.findViewById(R.id.checkBox1);
		try {
			Https https = (Https) checker;
			editText1.setText(Integer.toString(https.getPort()));
			editText2.setText(Integer.toString(https.getResponseCode()));
			checkBox1.setChecked(https.getAllCertificates());
		} catch (ClassCastException e) {
		}
	}
}
