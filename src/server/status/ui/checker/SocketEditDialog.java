package server.status.ui.checker;

import server.status.R;
import server.status.check.Socket;
import android.view.View;
import android.widget.EditText;

public class SocketEditDialog extends CheckerEditDialog {
	private EditText editText1;

	@Override
	public int getResource() {
		return R.layout.checker_socket;
	}

	@Override
	protected void updateChecker() {
		if (editText1 != null) {
			try {
				Socket socket = (Socket) checker;
				int port = Integer.parseInt(editText1.getText().toString());
				socket.setPort(port);
			} catch (NumberFormatException e) {
			} catch (ClassCastException e) {
			}
		}
	}

	@Override
	protected void updateEditView(View view) {
		editText1 = (EditText) view.findViewById(R.id.editText1);
		try {
			Socket socket = (Socket) checker;
			editText1.setText(Integer.toString(socket.getPort()));
		} catch (ClassCastException e) {
		}
	}
}
