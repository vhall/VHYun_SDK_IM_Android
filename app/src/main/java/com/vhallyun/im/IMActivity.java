package com.vhallyun.im;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.vhall.framework.connect.VhallConnectService;
import com.vhall.ims.VHIM;
import com.vhall.message.ConnectServer;
import com.vhallyun.im.model.ChannelDataModel;
import com.vhallyun.im.widget.ChangeUserInfoDialog;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by Hank on 2017/12/20.
 */
public class IMActivity extends Activity {

    private static final String TAG = "IMActivity";
    private String mChannelId = "";
    private String mAccessToken = "";
    private LinearLayout ll_content;
    private EditText et;
    private Switch swSet;
    private MemberPop memberPop;
    private Gson gson = new Gson();
    private ChannelDataModel channelData;
    VHIM im;

    private ChangeUserInfoDialog dialog;



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
        swSet = findViewById(R.id.sw_set_channel);


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

        swSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (swSet.isChecked()) {
                    im.setChannelMsg(VHIM.TYPE_DISABLE_ALL, "", sendCallback);
                } else {
                    im.setChannelMsg(VHIM.TYPE_PERMIT_ALL, "", sendCallback);
                }
            }
        });
    }

    private VHIM.Callback sendCallback = new VHIM.Callback() {
        @Override
        public void onSuccess() {

        }

        @Override
        public void onFailure(int errorCode, String errorMsg) {
            Toast.makeText(IMActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
        }
    };

    public void updateUserInfo(View view) {
        if(dialog == null){
            dialog = new ChangeUserInfoDialog(this);
        }
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        im.leave();
        im = null;
        super.onDestroy();
    }

    public void showMember(final View view) {
        im.getUserList(1, 20, new VHIM.ResultCallback() {
            @Override
            public void onFailure(int errorCode, String errorMsg) {
                Toast.makeText(IMActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(String data) {
                Log.e(TAG, "onSuccess: " + data);
                channelData = gson.fromJson(data, ChannelDataModel.class);
                channelData.resetList();
                if (memberPop == null) {
                    memberPop = new MemberPop(IMActivity.this, channelData, im);
                } else {
                    memberPop.updateData(channelData);
                }
                memberPop.showAtLocation(view, Gravity.CENTER, 0, 0);
            }
        });
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
                        Toast.makeText(IMActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
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
                String dataStr = text.optString("data");
                if (TextUtils.isEmpty(service_type)) {
                    Log.e(TAG, "onMessage: " + msg);
                } else if (service_type.equals(VHIM.TYPE_CUSTOM)) {//自定义消息处理
                    addView(service_type, "", dataStr, time, "");
                } else {
                    JSONObject data = new JSONObject(dataStr);
                    String context = text.optString("context");
                    String nickName, avatar = "";
                    if (!TextUtils.isEmpty(context)) {
                        JSONObject contextObj = new JSONObject(context);
                        nickName = contextObj.optString("nick_name");
                        avatar = contextObj.optString("avatar");
                        if (TextUtils.isEmpty(nickName)) {
                            nickName = sender_id;
                        }
                    } else {
                        nickName = sender_id;
                    }
                    String textContent = data.optString("text_content");
                    String type = data.optString("type");
                    String targetId = data.optString("target_id");
                    if (!TextUtils.isEmpty(targetId)) {
                        nickName = targetId;
                    }
                    int onlineNum = text.optInt("uv");
                    if (type.equals(VHIM.TYPE_JOIN)) {
                        //上线消息
                        if (memberPop != null) {
                            memberPop.userJoin(sender_id);
                        }
                    } else if (type.equals(VHIM.TYPE_LEAVE)) {
                        //下线消息
                        if (memberPop != null) {
                            memberPop.userLeave(sender_id);
                        }
                    } else if (type.equals(VHIM.TYPE_DISABLE)) {
                        //禁言某个用户消息
                        if (memberPop != null) {
                            memberPop.userDisable(targetId);
                        }
                    } else if (type.equals(VHIM.TYPE_DISABLE_ALL)) {
                        //全员禁言消息
                        swSet.setChecked(true);
                    } else if (type.equals(VHIM.TYPE_PERMIT)) {
                        //取消某用户禁言
                        if (memberPop != null) {
                            memberPop.userPermit(targetId);
                        }
                    } else if (type.equals(VHIM.TYPE_PERMIT_ALL)) {
                        //取消全员禁言
                        swSet.setChecked(false);
                    }
                    addView(type, nickName, textContent, time, avatar);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onChannelStatus(String msg) {
            try {
                JSONObject obj = new JSONObject(msg);
                int channelDisable = obj.optJSONObject("data").optInt("channel_disable");
                if (channelDisable == 1) {
                    swSet.setChecked(true);
                } else {
                    swSet.setChecked(false);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private void addView(String event, String nick_name, String data, String time, String avatar) {
        View view = View.inflate(IMActivity.this, R.layout.im_item_layout, null);
        if (ll_content.getChildCount() >= 10) {
            View removeView = ll_content.getChildAt(0);
            ll_content.removeView(removeView);
        }
        TextView c = view.findViewById(R.id.content);
        TextView t = view.findViewById(R.id.time);
        if (event.equals(VHIM.TYPE_CUSTOM)) {
            c.setText("接收的自定义消息" + nick_name + ": " + data);
        } else if (event.equals(VHIM.TYPE_JOIN)) {
            c.setText(nick_name + ": 上线了");
        } else if (event.equals(VHIM.TYPE_LEAVE)) {
            c.setText(nick_name + ": 下线了");
        } else if (event.equals(VHIM.TYPE_DISABLE_ALL)) {
            c.setText("全体禁言");
        } else if (event.equals(VHIM.TYPE_PERMIT_ALL)) {
            c.setText("取消全体禁言");
        } else if (event.equals(VHIM.TYPE_DISABLE)) {
            c.setText(nick_name + ": 被禁言");
        } else if (event.equals(VHIM.TYPE_PERMIT)) {
            c.setText(nick_name + ": 已取消禁言");
        } else {
            c.setText(nick_name + ": " + data);
            ImageView v = view.findViewById(R.id.avatar);
            if (!TextUtils.isEmpty(avatar) && !avatar.equals("null")) {
                Glide.with(this)
                        .load(avatar)
                        .into(v);
            }
            v.setVisibility(View.VISIBLE);
        }
        t.setText(time);
        ll_content.addView(view);
    }
}
