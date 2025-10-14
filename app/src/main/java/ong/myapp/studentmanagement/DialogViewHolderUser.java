package ong.myapp.studentmanagement;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.textfield.TextInputLayout;


import ong.myapp.studentmanagement.model.User;

public class DialogViewHolderUser {

    EditText tv_name, tv_userId, tv_status, tv_email, tv_phone, tv_password, tv_role, tv_age;
    ImageView iv_avatar, iv_camera;
    TextInputLayout layoutName, layoutUserId, layoutStatus, layoutEmail, layoutPhone, layoutPassword, layoutRole, layoutAge;
    ProgressBar progressBar2;
    public DialogViewHolderUser(View view) {
        // Gán ID các thành phần từ layout mới
        tv_name = view.findViewById(R.id.tv_names);
        tv_userId = view.findViewById(R.id.tv_userIds);
        tv_status = view.findViewById(R.id.tv_statuss);
        tv_email = view.findViewById(R.id.tv_emails);
        tv_phone = view.findViewById(R.id.tv_phones);
        tv_password = view.findViewById(R.id.tv_passwords);
        tv_role = view.findViewById(R.id.tv_roles);
        tv_age = view.findViewById(R.id.tv_ages);

        iv_avatar = view.findViewById(R.id.iv_avatars);
        iv_camera = view.findViewById(R.id.iv_cameras);
        progressBar2 = view.findViewById(R.id.progressBar2);
        layoutName = view.findViewById(R.id.layoutName);
        layoutUserId = view.findViewById(R.id.layoutUserId);
        layoutStatus = view.findViewById(R.id.layoutStatus);
        layoutEmail = view.findViewById(R.id.layoutEmail);
        layoutPhone = view.findViewById(R.id.layoutPhone);
        layoutPassword = view.findViewById(R.id.layoutPassword);
        layoutRole = view.findViewById(R.id.layoutRole);
        layoutAge = view.findViewById(R.id.layoutAge);

    }

    public static DialogViewHolderUser setupDialogView(Context context, User user, boolean isUpdate) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_add_user, null);
        DialogViewHolderUser viewHolder = new DialogViewHolderUser(view);

        if (user != null) {

            viewHolder.tv_name.setText(user.getName());
            viewHolder.tv_userId.setText(user.getUserId());
            viewHolder.tv_status.setText(user.getStatus());
            viewHolder.tv_email.setText(user.getEmail());
            viewHolder.tv_phone.setText(user.getPhoneNumber());
            viewHolder.tv_password.setText(user.getPassword());
            viewHolder.tv_role.setText(user.getRole());
            viewHolder.tv_age.setText(user.getAge());
            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                Glide.with(context)
                        .load(user.getAvatar())
                        .apply(new RequestOptions().placeholder(R.drawable.ic_launcher_background)
                                .error(R.drawable.ic_launcher_background))
                        .into(viewHolder.iv_avatar);
            }

            if (isUpdate) {
                viewHolder.tv_userId.setEnabled(false);
            }
        }

        return viewHolder;
    }
}
