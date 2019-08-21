package com.vhallyun.im;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.vhall.framework.VhallSDK;

/**
 * Created by Hank on 2017/12/8.
 */
public class MainActivity extends Activity {

    TextView tv_appid;
    private static final String TAG = "VHLivePusher";
    private static final String KEY_CHAT_ID = "channelId";
    private static final String KEY_TOKEN = "token";
    SharedPreferences sp;
    EditText edtChennelId, edtToken;
    private String token;
    private String roomid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        token = sp.getString(KEY_TOKEN, "");
        roomid = sp.getString(KEY_CHAT_ID, "");
        tv_appid = this.findViewById(R.id.tv_appid);
        tv_appid.setText(VhallSDK.getInstance().getAPP_ID());
        edtChennelId = findViewById(R.id.edt_channel_id);
        edtToken = findViewById(R.id.edt_token);
        edtToken.setText(token);
        edtChennelId.setText(roomid);

    }


    public void showIM(View view) {
        token = edtToken.getText().toString().trim();
        roomid = edtChennelId.getText().toString().trim();
        if (TextUtils.isEmpty(roomid) || TextUtils.isEmpty(token)) {
            return;
        }
        sp.edit().putString(KEY_CHAT_ID, roomid).putString(KEY_TOKEN, token).commit();
        Intent intent = new Intent(this, IMActivity.class);
        intent.putExtra("channelid", roomid);
        intent.putExtra("token", token);
        startActivity(intent);
    }
}
