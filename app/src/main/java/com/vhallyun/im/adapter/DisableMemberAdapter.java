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
public class DisableMemberAdapter extends RecyclerView.Adapter {

    private List<UserInfo> dataList = new ArrayList<>();
    private OnCancelClickListener onCancelClick;

    public DisableMemberAdapter(List<UserInfo> dataList) {
        this.dataList = dataList;
    }

    public void setDataList(List<UserInfo> list) {
        dataList = list;
        notifyDataSetChanged();
    }

    public void setOnCancelClick(OnCancelClickListener listener) {
        onCancelClick = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.member_disable_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        ViewHolder holder = (ViewHolder) viewHolder;
        holder.tvNickName.setText(dataList.get(i).getUserId());
        holder.ivAvatar.setSelected(dataList.get(i).isOnline());

    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivAvatar;
        private TextView tvNickName;
        private Button btnOperation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_member_disable_avatar);
            tvNickName = itemView.findViewById(R.id.tv_member_disable_nick_name);
            btnOperation = itemView.findViewById(R.id.btn_cancel);
            btnOperation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onCancelClick != null) {
                        onCancelClick.onClick(getAdapterPosition());
                    }
                }
            });
        }
    }

    public interface OnCancelClickListener {
        void onClick(int position);
    }
}
