package ong.myapp.studentmanagement.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import ong.myapp.studentmanagement.R;
import ong.myapp.studentmanagement.model.UserActivityHistory;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    private List<UserActivityHistory> historyList;

    public HistoryAdapter(List<UserActivityHistory> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        UserActivityHistory history = historyList.get(position);
        holder.tvUserId.setText(history.getUserId());
        holder.tvTime.setText(history.getTimestamp().toString());
        holder.name.setText(history.getName());
        if (history.getAvatar() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(history.getAvatar())
                    .into(holder.ivAvatar);
        }

    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserId, tvTime,name;
        ImageView ivAvatar;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserId = itemView.findViewById(R.id.tv_userId);
            tvTime = itemView.findViewById(R.id.tv_time);
            name=itemView.findViewById(R.id.tv_name);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
        }
    }
}
