package app.sensor.tester;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import app.sensor.AppSensor;
import app.sensor.Logs;
import app.sensor.SensorMsg;

public class MainActivity extends Activity
{

	TextView	txtHttpResp	= null;

	Handler		theHandler	= new Handler()
							{
								@Override
								public void handleMessage(Message msg)
								{
									switch (msg.what)
									{
										case SensorMsg.RESPONSE_HTTP:
											if (msg.obj instanceof String && null != msg.obj)
											{
												String strContent = (String) msg.obj;
												txtHttpResp.setText(strContent);
											}
											break;
									}
								}
							};

	AppSensor	appSensor	= new AppSensor(theHandler);

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		TextView txVersion = (TextView) this.findViewById(R.id.textViewVersion);
		txVersion.setText("Version:" + appSensor.getVersion());

		Button btnRestTest = (Button) this.findViewById(R.id.buttonRestTest);
		txtHttpResp = (TextView) this.findViewById(R.id.textViewHttpResp);
		btnRestTest.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				Map<String, String> parm = new HashMap<String, String>();
				parm.put("SENSOR1aa", "is1aa");
				parm.put("SENSOR2aa", "is2aa");
				appSensor.sendEvent(parm);
				parm.clear();
				parm = null;

			}
		});

	}
}
