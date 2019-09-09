package com.vhallyun.im;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.vhall.ims.VHIM;
import com.vhallyun.im.adapter.DisableMemberAdapter;
import com.vhallyun.im.adapter.MemberAdapter;
import com.vhallyun.im.model.ChannelDataModel;
import com.vhallyun.im.model.UserInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zwp on 2019-08-26
 */
public class MemberPop extends PopupWindow {
    private static final String TAG = "MemberPop";
    private Context mContext;
    private RecyclerView rvMember, rvDisableMember;
    private LinearLayoutManager memberManger, disableManager;
    private MemberAdapter memberAdapter;
    private DisableMemberAdapter disableMemberAdapter;
    private ImageView ivClose;
    private List<UserInfo> onlineList = new ArrayList<>();
    private List<UserInfo> disableList = new ArrayList<>();
    private VHIM im;


    public MemberPop(Context context, ChannelDataModel data, VHIM im) {
        this.mContext = context;
        this.im = im;
        onlineList = data.getOnlineList();
        disableList = data.getDisableList();

        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        ColorDrawable dw = new ColorDrawable(Color.WHITE);
        setBackgroundDrawable(dw);
        setFocusable(true);
        View root = View.inflate(context, R.layout.pop_member, null);
        setContentView(root);
        ivClose = root.findViewById(R.id.iv_close);

        rvMember = root.findViewById(R.id.rv_member);
        memberManger = new LinearLayoutManager(mContext);
        memberManger.setOrientation(LinearLayoutManager.VERTICAL);
        memberAdapter = new MemberAdapter(onlineList);
        rvMember.setLayoutManager(memberManger);
        rvMember.setAdapter(memberAdapter);


        rvDisableMember = root.findViewById(R.id.rv_disable_member);
        disableManager = new LinearLayoutManager(mContext);
        disableManager.setOrientation(LinearLayoutManager.VERTICAL);
        disableMemberAdapter = new DisableMemberAdapter(disableList);
        rvDisableMember.setLayoutManager(disableManager);
        rvDisableMember.setAdapter(disableMemberAdapter);

        setOnClickListener();

    }

    private void setOnClickListener() {
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        memberAdapter.setOnBtnClickListener(new MemberAdapter.OnBtnClickListener() {
            @Override
            public void onCLick(final int position) {
                im.setChannelMsg(VHIM.TYPE_DISABLE, onlineList.get(position).getUserId(), new VHIM.Callback() {
                    @Override
                    public void onSuccess() {
                        disableList.add(onlineList.get(position));
                        onlineList.remove(position);
                        memberAdapter.notifyDataSetChanged();
                        disableMemberAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg) {
                        Log.e(TAG, "onFailure: errorCode=" + errorCode + "--errorMsg:" + errorMsg);
                    }
                });
            }
        });

        disableMemberAdapter.setOnCancelClick(new DisableMemberAdapter.OnCancelClickListener() {
            @Override
            public void onClick(final int position) {
                im.setChannelMsg(VHIM.TYPE_PERMIT, disableList.get(position).getUserId(), new VHIM.Callback() {
                    @Override
                    public void onSuccess() {
                        if (disableList.get(position).isOnline()) {
                            onlineList.add(disableList.get(position));
                        }
                        disableList.remove(position);
                        disableMemberAdapter.notifyDataSetChanged();
                        memberAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg) {
                        Log.e(TAG, "onFailure: errorCode=" + errorCode + "--errorMsg:" + errorMsg);
                    }
                });
            }
        });
    }


    public void updateData(ChannelDataModel data) {
//        onlineList = data.getOnlineList();
//        disableList = data.getDisableList();
//        memberAdapter.setDataList(onlineList);
//        disableMemberAdapter.setDataList(disableList);
        memberAdapter.notifyDataSetChanged();
        disableMemberAdapter.notifyDataSetChanged();
    }


    public void userJoin(String id) {
        int updateId = -1;
        for (int i = 0; i < disableList.size(); i++) {
            if (disableList.get(i).getUserId().equals(id)) {
                disableList.get(i).setOnline(true);
                updateId = i;
                break;
            }
        }
        if (updateId == -1) {
            onlineList.add(new UserInfo(id, true));
        }
        memberAdapter.notifyDataSetChanged();
        disableMemberAdapter.notifyDataSetChanged();
    }

    public void userLeave(String id) {
        int updateId = -1;
        for (int i = 0; i < onlineList.size(); i++) {
            if (onlineList.get(i).getUserId().equals(id)) {
                updateId = i;
                break;
            }
        }
        if (updateId != -1) {
            onlineList.remove(updateId);
            memberAdapter.notifyDataSetChanged();
            return;
        }
        for (int i = 0; i < disableList.size(); i++) {
            if (disableList.get(i).getUserId().equals(id)) {
                disableList.get(i).setOnline(false);
                disableMemberAdapter.notifyDataSetChanged();
                break;
            }
        }


    }

    public void userDisable(String id) {
        for (int i = 0; i < onlineList.size(); i++) {
            if (onlineList.get(i).getUserId().equals(id)) {
                disableList.add(onlineList.get(i));
                onlineList.remove(i);
                memberAdapter.notifyDataSetChanged();
                disableMemberAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    public void userPermit(String id) {
        for (int i = 0; i < disableList.size(); i++) {
            if (disableList.get(i).getUserId().equals(id)) {
                if (disableList.get(i).isOnline()) {
                    onlineList.add(disableList.get(i));
                    disableList.remove(i);
                    memberAdapter.notifyDataSetChanged();
                    disableMemberAdapter.notifyDataSetChanged();
                    break;
                }
            }
        }
    }

}
