package server.status;

import server.status.ui.SelectCheckerDialog;
import server.status.ui.SelectCheckerDialog.SelectCheckerListener;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class EditActivity extends FragmentActivity implements
		SelectCheckerListener {
	public static final String INTENT_ID = "id";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit);

		Button buttonSave = (Button) findViewById(R.id.buttonSave);
		buttonSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			}
		});
		Button buttonCancel = (Button) findViewById(R.id.buttonCancel);
		buttonCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.edit, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_add_checker:
			SelectCheckerDialog dialog = new SelectCheckerDialog();
			dialog.show(getSupportFragmentManager(), "SelectCheckerDialog");
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onSelectChecker(int index) {
		// TODO Auto-generated method stub
	}
}
