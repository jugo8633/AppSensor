package app.sensor;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.os.Handler;
import android.os.Message;

public class AppSensor
{
	public static final String	TYPE_VIEW					= "0";	// 瀏覽商品
	public static final String	TYPE_SHOPPING_CART_ADD		= "1";	// 購物車商品新增
	public static final String	TYPE_SHOPPING_CART_CANCEL	= "2";	// 購物車商品取消
	public static final String	TYPE_PROD_ORDER				= "3";	// 商品訂購
	public static final String	TYPE_PROD_CANCEL			= "4";	// 商品取消訂購
	public static final String	TYPE_PROD_PREORDER			= "5";	// 商品預購
	public static final String	TYPE_PROD_SEARCH			= "6";	// 商品搜尋
	public static final String	TYPE_MEMBER_LOGIN			= "7";	// 會員登入
	public static final String	TYPE_MEMBER_SIGNON			= "8";	// 會員註冊
	public static final String	TYPE_PROD_SELECT			= "9";	// 點選商品
	public static final String	TYPE_BONUS_SELECT			= "10"; // 點選紅利
	public static final String	TYPE_GIFT_SELECT			= "11"; // 點選贈品
	public static final String	TYPE_VALUE_ADD_SELECT		= "12"; // 點選加價購
	public static final String	TYPE_OUTLET_SELECT			= "13"; // 點選出清品
	public static final String	TYPE_WELFARE_SELECT			= "14"; // 點選福利品
	public static final String	TYPE_PUSH_MESSAGE_VIEW		= "15"; // 推播網頁瀏覽
	public static final String	TYPE_SERIAL_GET				= "16"; // 取序號
	public static final String	TYPE_LOGIN_QQ				= "17"; // 騰訊QQ會員登入
	public static final String	TYPE_INVITE					= "18"; // 邀請
	public static final String	TYPE_FIELD					= "19"; // 商場/場域
	public static final String	TYPE_MISSION				= "20"; // 任務
	public static final String	TYPE_QRCODE_SCAN			= "21"; // QR Code掃描
	public static final String	TYPE_EXCHANGE				= "22"; // 兌換

	private final int			TAG_APPSENSOR_INIT			= 1025;
	public static final int		MSG_RESPONSE				= 1026;
	private Handler				parentHandler				= null;
	private Handler				theHandler					= new Handler()
															{
																@Override
																public void handleMessage(Message msg)
																{
																	if (MSG_RESPONSE == msg.what)
																	{
																		switch (msg.arg2)
																		{
																			case TAG_APPSENSOR_INIT:
																				initHandle(msg.arg1, (String) msg.obj);
																				break;
																		}

																		if (null != parentHandler)
																		{
																			Message pMsg = new Message();
																			pMsg.what = msg.what;
																			pMsg.arg1 = msg.arg1;
																			pMsg.arg2 = msg.arg2;
																			pMsg.obj = msg.obj;
																			parentHandler.sendMessage(pMsg);
																		}
																	}
																}
															};

	public AppSensor()
	{
		super();
	}

	public AppSensor(Handler pHandler)
	{
		parentHandler = pHandler;
	}

	@Override
	protected void finalize() throws Throwable
	{
		super.finalize();
	}

	public String getVersion()
	{
		return Common.Version;
	}

	public void init()
	{
		Thread t = new Thread(new sendGetRunnable(Common.URL_APPSENSOR_INIT, null, TAG_APPSENSOR_INIT));
		t.start();
	}

	private void initHandle(final int nReturnCode, final String strContent)
	{
		if (HttpURLConnection.HTTP_OK == nReturnCode)
		{
			Common.URL_APPSENSOR_SERVER = strContent;
			Logs.showTrace("Set App Sensor Server URL:" + Common.URL_APPSENSOR_SERVER);
		}
		else
		{
			Common.URL_APPSENSOR_SERVER = null;
			Logs.showTrace("Get App Sensor Server URL Fail, Return Code:" + String.valueOf(nReturnCode) + " Error:"
					+ strContent);
		}
	}

	public void sendEvent(HashMap<String, String> postParams)
	{
		if (null == Common.URL_APPSENSOR_SERVER)
		{
			Logs.showTrace("App Sensor Not Init");
			return;
		}
		Thread t = new Thread(new sendPostRunnable(postParams));
		t.start();
	}

	private int sendPostData(Map<String, String> parm, HttpClient.Response response)
	{
		if (null == parm || null == response)
		{
			return -1;
		}

		try
		{
			HttpClient.sendPostData(Common.URL_APPSENSOR_SERVER, parm, response);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Logs.showTrace("Exception:" + e.getMessage());
		}
		return response.mnCode;
	}

	class sendPostRunnable implements Runnable
	{
		Map<String, String>	parm;

		@Override
		public void run()
		{
			HttpClient.Response response = new HttpClient.Response();
			sendPostData(parm, response);
			Message msg = new Message();
			msg.what = MSG_RESPONSE;
			msg.arg1 = response.mnCode;
			msg.obj = response.mstrContent;
			theHandler.sendMessage(msg);
			msg = null;
			parm.clear();
			parm = null;
		}

		public sendPostRunnable(HashMap<String, String> PostParm)
		{
			parm = new HashMap<String, String>();

			if (null != PostParm && 0 < PostParm.size())
			{
				for (Entry<String, String> item : PostParm.entrySet())
				{
					parm.put(item.getKey(), item.getValue());
				}
			}
		}
	}

	private int sendGetData(final String strURL, Map<String, String> parm, HttpClient.Response response)
	{
		if (null == strURL || null == response)
		{
			return -1;
		}

		try
		{
			HttpClient.sendGetData(strURL, parm, response);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Logs.showTrace("Exception:" + e.getMessage());
		}
		return response.mnCode;
	}

	class sendGetRunnable implements Runnable
	{
		private String					mstrURL	= null;
		private HashMap<String, String>	parm	= null;
		private int						mnTag	= 0;

		@Override
		public void run()
		{
			HttpClient.Response response = new HttpClient.Response();
			sendGetData(mstrURL, parm, response);
			Message msg = new Message();
			msg.what = MSG_RESPONSE;
			msg.arg1 = response.mnCode;
			msg.arg2 = mnTag;
			msg.obj = response.mstrContent;
			theHandler.sendMessage(msg);
			msg = null;
			parm.clear();
			parm = null;
		}

		public sendGetRunnable(final String strURL, final HashMap<String, String> sendParm, final int nTag)
		{
			if (!StringUtility.isValid(strURL))
				return;

			mstrURL = strURL;
			mnTag = nTag;
			parm = new HashMap<String, String>();

			if (null != sendParm && 0 < sendParm.size())
			{
				for (Entry<String, String> item : sendParm.entrySet())
				{
					parm.put(item.getKey(), item.getValue());
				}
			}
		}
	}
}
