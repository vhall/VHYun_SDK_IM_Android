package com.vhallyun.im;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vhall.framework.connect.VhallConnectService;
import com.vhall.ims.VHIM;
import com.vhall.message.ConnectServer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Hank on 2017/12/20.
 */
public class IMActivity extends Activity {

    private String mChannelId = "";
    private String mAccessToken = "";
    private LinearLayout ll_content;
    private EditText et;
    VHIM im;
    private OkHttpClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mChannelId = getIntent().getStringExtra("channelid");
        mAccessToken = getIntent().getStringExtra("token");
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.im_layout);
        ll_content = this.findViewById(R.id.ll_content);
        et = this.findViewById(R.id.et);
        et.setOnEditorActionListener(new EditListener());
        im = new VHIM(mChannelId, mAccessToken);
        im.setOnMessageListener(new MsgListener());
        im.setOnConnectChangedListener(new VhallConnectService.OnConnectStateChangedListener() {
            @Override
            public void onStateChanged(ConnectServer.State state, int serverType) {
                if (serverType == VhallConnectService.SERVER_CHAT) {
                    String text = "";
                    switch (state) {
                        case STATE_CONNECTIONG:
                            text = "连接中";
                            break;
                        case STATE_DISCONNECT:
                            text = "连接失败";
                            break;
                        case STATE_CONNECTED:
                            text = "连接成功！";

                            break;
                    }
                    Toast.makeText(IMActivity.this, "网络：" + text, Toast.LENGTH_SHORT).show();
                }
            }
        });
        im.join();
    }

    @Override
    protected void onDestroy() {
        im.leave();
        im = null;
        super.onDestroy();
    }

    class EditListener implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                im.sendMsg(v.getText().toString(), new VHIM.Callback() {
                    @Override
                    public void onSuccess() {
                        Log.i("IMACt", "success");
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg) {
                        Log.e("imact", "errorCode:" + errorCode + "&errorMsg:" + errorMsg);
                        Toast.makeText(IMActivity.this,errorMsg, Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return true;
        }
    }

    class MsgListener implements VHIM.OnMessageListener {

        @Override
        public void onMessage(String msg) {
            try {
                JSONObject text = new JSONObject(msg);
                String service_type = text.optString("service_type");//服务类型
                if (TextUtils.isEmpty(service_type)) return; //
                String sender_id = text.optString("sender_id");
                String time = text.optString("date_time");
                String context = text.optString("context");
                String dataStr = text.optString("data");
                if (service_type.equals(VHIM.TYPE_CUSTOM)) {//自定义消息处理
                    addView(service_type, "", dataStr, time, "");
                } else {
                    JSONObject data = new JSONObject(dataStr);
                    JSONObject contextObj = new JSONObject(context);
                    String nickName = contextObj.optString("nick_name");
                    if(TextUtils.isEmpty(nickName)){
                        nickName = sender_id;
                    }
                    String avatar = contextObj.optString("avatar");
                    String textContent = data.optString("text_content");
                    String type = data.optString("type");
                    int onlineNum = text.optInt("uv");
//                Toast.makeText(IMActivity.this, " 当前在线人数 ： " + onlineNum, Toast.LENGTH_SHORT).show();
                    if (service_type.equals(VHIM.TYPE_ONLINE)) {
                        addView(type, nickName, textContent, time, avatar);
                    } else {
                        addView(service_type, nickName, textContent, time, avatar);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void addView(String event, String nick_name, String data, String time, String avatar) {
        View view = View.inflate(IMActivity.this, R.layout.im_item_layout, null);
        ImageView v = view.findViewById(R.id.avatar);
        if (ll_content.getChildCount() >= 10) {
            View removeView = ll_content.getChildAt(0);
            ll_content.removeView(removeView);
        }
        if (!TextUtils.isEmpty(avatar) && !avatar.equals("null")) {
            requestAvatar(avatar, v);
        }
        TextView c = view.findViewById(R.id.content);
        TextView t = view.findViewById(R.id.time);
        if (event.equals(VHIM.TYPE_CUSTOM)) {
            c.setText("接收的自定义消息" + nick_name + ": " + data);
        } else if (event.equals(VHIM.TYPE_JOIN)) {
            c.setText(nick_name + ": 上线了");
        } else if (event.equals(VHIM.TYPE_LEAVE)) {
            c.setText(nick_name + ": 下线了");
        } else {
            c.setText(nick_name + ": " + data);
        }
        t.setText(time);
        ll_content.addView(view);
    }

    private void requestAvatar(String url, final ImageView view) {
        if (url.startsWith("//")) {
            url = "http:" + url;
        }
        if (!Patterns.WEB_URL.matcher(url).matches()) {
            return;
        }
        Request request = new Request.Builder().url(url).build();
        if (mClient == null)
            mClient = new OkHttpClient();
        Call call = mClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final byte[] picture = response.body().bytes();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        view.setImageBitmap(BitmapFactory.decodeByteArray(picture, 0, picture.length));
                    }
                });
            }
        });
    }
}
