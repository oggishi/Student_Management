package ong.myapp.studentmanagement.adapter;

import android.content.Context;
import android.util.Log;
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
import ong.myapp.studentmanagement.model.User;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.TaiKhoanViewHolder> {

    private List<User> userList;
    private Context context;
    private User selectedUser;
    private OnItemClickListener listener;

    public UserAdapter(Context context, List<User> userList) {
        this.userList = userList;
        this.context = context;
    }


    public interface OnItemClickListener {
        void onItemClick(User user);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaiKhoanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_taikhoan, parent, false);
        return new TaiKhoanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaiKhoanViewHolder holder, int position) {
        User user = userList.get(position);

        holder.tv_name.setText(user.getName());
        holder.tv_userId.setText(user.getUserId());
        holder.tv_status.setText("Trạng thái: "+user.getStatus());
        holder.tv_email.setText("Email: " + user.getEmail());
        holder.tv_age.setText("Tuổi: "+user.getAge());
        holder.tv_phone.setText("Phone: " + user.getPhoneNumber());
        holder.tv_password.setText("Password: " + user.getPassword());
        holder.tv_role.setText("Role: " + user.getRole());
        Log.d("GlideImageURL", "URL: " + user.getAvatar());
        Glide.with(holder.itemView.getContext())
                .load(user.getAvatar())
                .into(holder.iv_avatar);

        // Kiểm tra nếu chứng chỉ này được chọn
        if (selectedUser != null && selectedUser.equals(user)) {
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.gray));
        } else {
            holder.itemView.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
        }

        holder.itemView.setOnClickListener(v -> {
            selectedUser = user;
            notifyDataSetChanged();
            if (listener != null) {
                listener.onItemClick(user);
            }
        });
    }


    @Override
    public int getItemCount() {
        return userList.size();
    }

    public User getSelectedUser() {
        return selectedUser;
    }

    public void updateData(List<User> newList) {
        this.userList.clear();
        this.userList.addAll(newList);
        notifyDataSetChanged();
    }



    public static class TaiKhoanViewHolder extends RecyclerView.ViewHolder {
        TextView tv_name, tv_userId, tv_status, tv_email, tv_phone, tv_password, tv_role,tv_age;
        ImageView iv_avatar;
        public TaiKhoanViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_userId = itemView.findViewById(R.id.tv_userId);
            tv_status = itemView.findViewById(R.id.tv_status);
            tv_email = itemView.findViewById(R.id.tv_email);
            tv_phone = itemView.findViewById(R.id.tv_phone);
            tv_password = itemView.findViewById(R.id.tv_password);
            tv_role = itemView.findViewById(R.id.tv_role);
            iv_avatar=itemView.findViewById(R.id.iv_avatar);
            tv_age=itemView.findViewById(R.id.tv_age);

        }
    }


}
