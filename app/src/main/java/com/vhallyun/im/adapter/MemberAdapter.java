package com.vhallyun.im.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.vhallyun.im.R;
import com.vhallyun.im.model.ChannelDataModel;
import com.vhallyun.im.model.UserInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zwp on 2019-08-27
 */
public class MemberAdapter extends RecyclerView.Adapter {

    private List<UserInfo> dataList = new ArrayList<>();
    private OnBtnClickListener btnClickListener;

    public MemberAdapter(List<UserInfo> list) {
        this.dataList = list;
    }

    public void setDataList(List<UserInfo> list) {
        dataList = list;
        notifyDataSetChanged();
    }

    public void setOnBtnClickListener(OnBtnClickListener listener) {
        btnClickListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.member_item, viewGroup, false);
        return new ViewHold(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        ViewHold hold = (ViewHold) viewHolder;
        hold.tvNickName.setText(dataList.get(i).getUserId());
        hold.ivAvatar.setSelected(dataList.get(i).isOnline());

    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    class ViewHold extends RecyclerView.ViewHolder {
        private ImageView ivAvatar;
        private TextView tvNickName;
        private Button btnOperate;

        public ViewHold(@NonNull final View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_member_avatar);
            ivAvatar.setSelected(true);
            tvNickName = itemView.findViewById(R.id.tv_nick_name);
            btnOperate = itemView.findViewById(R.id.btn_set_status);
            btnOperate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (btnClickListener !=null){
                        btnClickListener.onCLick(getAdapterPosition());
                    }
                }
            });
        }
    }

    public interface OnBtnClickListener {
        void onCLick(int position);
    }
}
