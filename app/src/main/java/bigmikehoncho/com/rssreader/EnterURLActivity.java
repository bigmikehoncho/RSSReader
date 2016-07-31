package bigmikehoncho.com.rssreader;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import bigmikehoncho.com.rssreader.content.Data;

public class EnterURLActivity extends AppCompatActivity {
	
	private EditText mETUrl;
	private Button mBtnGo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_enter_url);
		
		mETUrl = (EditText) findViewById(R.id.edit_url);
		mBtnGo = (Button) findViewById(R.id.btn_launch);
		
		mBtnGo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// Clear previous data if there is any and launch the MainActivity
				Data.getInstance().clear();
				String url = mETUrl.getText().toString();
				Intent intent = new Intent(EnterURLActivity.this, MainActivity.class);
				intent.putExtra(MainActivity.EXTRA_URL, url);
				startActivity(intent);
			}
		});
	}
}
