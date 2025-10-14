package ong.myapp.studentmanagement;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import ong.myapp.studentmanagement.adapter.HistoryAdapter;
import ong.myapp.studentmanagement.adapter.UserAdapter;
import ong.myapp.studentmanagement.model.User;
import ong.myapp.studentmanagement.model.UserActivityHistory;

public class QuanLyTaiKhoanActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    ImageView btnBack, btnSearch;
    EditText editSearch;
    RecyclerView recyclerViewUser;
    UserAdapter userAdapter;
    List<User> userList;
    FirebaseFirestore db;
    ProgressBar progressBar;
    private ImageView dialogAvatarImageView;
    private  DialogViewHolderUser dialogViewHolderUser;
    User selectedUser;
    private Uri imageUri;
    private String avatarUrl ="";


    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_quan_ly_tai_khoan);
        anhXa();
        db = FirebaseFirestore.getInstance();
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(this, userList);

        recyclerViewUser.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewUser.setAdapter(userAdapter);

        loadUserData();

        userAdapter.setOnItemClickListener(user -> {
            selectedUser = user;
        });

        btnBack.setOnClickListener(view -> finish());
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchText = s.toString().trim();
                if (!searchText.isEmpty()) {
                    searchUser(searchText);
                } else {
                    loadUserData();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void searchUser(String searchText) {
        String query = searchText.toLowerCase();
        List<User> filteredList = new ArrayList<>();

        for (User user : userList) {
            if (user.getUserId().toLowerCase().contains(query) ||
                    user.getAge().toLowerCase().contains(query) ||
                    user.getName().toLowerCase().contains(query) ||
                    user.getEmail().toLowerCase().contains(query) ||
                    user.getPassword().toLowerCase().contains(query) ||
                    user.getStatus().toLowerCase().contains(query) ||
                    user.getRole().toLowerCase().contains(query) ||
                    user.getPhoneNumber().toLowerCase().contains(query)) {
                filteredList.add(user);
            }
        }

        userAdapter.updateData(filteredList);

        TextInputLayout layoutSearch = findViewById(R.id.layoutSearch);
        if (filteredList.isEmpty()) {

            layoutSearch.setError("Không tìm thấy user");
            loadUserData();
        } else {
            layoutSearch.setError(null);
        }
    }


    private void anhXa() {
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        btnSearch = findViewById(R.id.btnSearch);
        editSearch = findViewById(R.id.editSearch);
        recyclerViewUser = findViewById(R.id.recyclerViewUser);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu_user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.menu_add_new) {
            showAddUserDialog();
            return true;
        } else if (itemId == R.id.menu_update) {
            if (selectedUser != null) {
                showUpdateUserDialog(selectedUser);
            } else {
                Toast.makeText(this, "Vui lòng chọn tài khoản cần cập nhật", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (itemId == R.id.menu_delete) {
            if (selectedUser != null) {
                showDeleteUserDialog(selectedUser);
            } else {
                Toast.makeText(this, "Vui lòng chọn tài khoản cần xóa", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (itemId == R.id.menu_history) {
            showHistoryDialog();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }


    private void loadUserData() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("User").get().addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                List<User> newList = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    User user = document.toObject(User.class);
                    newList.add(user);
                }

                if (newList.isEmpty()) {
                    Log.d("LoadDebug", "No data available.");
                }
                userList.clear();
                userList.addAll(newList);
                userAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            } else {
                Toast.makeText(QuanLyTaiKhoanActivity.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    //them
    private void showAddUserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thêm tài khoản mới");

        DialogViewHolderUser viewHolder = DialogViewHolderUser.setupDialogView(this, null, false);
        builder.setView(viewHolder.tv_userId.getRootView());

        String generatedUserId = "U" + String.format("%03d", (int) (Math.random() * 999 + 1));
        viewHolder.tv_userId.setText(generatedUserId);

        viewHolder.tv_userId.setFocusable(false);
        viewHolder.tv_userId.setClickable(false);

        builder.setPositiveButton("Thêm", null);
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        Button addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        addButton.setOnClickListener(v -> {
            viewHolder.layoutUserId.setError(null);
            viewHolder.layoutName.setError(null);
            viewHolder.layoutAge.setError(null);
            viewHolder.layoutEmail.setError(null);
            viewHolder.layoutPhone.setError(null);
            viewHolder.layoutPassword.setError(null);
            viewHolder.layoutRole.setError(null);
            viewHolder.layoutStatus.setError(null);
            if (validateInputs(viewHolder.tv_userId, viewHolder.tv_name, viewHolder.tv_age, viewHolder.tv_email, viewHolder.tv_phone, viewHolder.tv_password, viewHolder.tv_role, viewHolder.tv_status,
                    viewHolder.layoutUserId, viewHolder.layoutName, viewHolder.layoutAge, viewHolder.layoutEmail, viewHolder.layoutPhone, viewHolder.layoutPassword, viewHolder.layoutRole, viewHolder.layoutStatus)) {
                if (avatarUrl == null || avatarUrl.isEmpty()) {
                    Toast.makeText(this, "Bạn chưa tải ảnh hoặc Đang tải ảnh lên, vui lòng chờ...", Toast.LENGTH_SHORT).show();
                } else {
                    createUser(viewHolder, dialog);
                }
            }
        });

        viewHolder.iv_camera.setOnClickListener(v -> openFileChooser(viewHolder.iv_avatar));
        viewHolder.iv_avatar.setOnClickListener(v -> openFileChooser(viewHolder.iv_avatar));
        this.dialogViewHolderUser = viewHolder;
    }



    private void createUser(DialogViewHolderUser viewHolder, AlertDialog dialog) {
        String userId = viewHolder.tv_userId.getText().toString().trim();
        String name = viewHolder.tv_name.getText().toString().trim();
        String age = viewHolder.tv_age.getText().toString().trim();
        String email = viewHolder.tv_email.getText().toString().trim();
        String phone = viewHolder.tv_phone.getText().toString().trim();
        String password = viewHolder.tv_password.getText().toString().trim();
        String role = viewHolder.tv_role.getText().toString().trim();
        String status = viewHolder.tv_status.getText().toString().trim();

        User user = new User(userId, name, age, phone, role, status, password, email, avatarUrl);
        user.saveToFirebase();
        userList.add(user);
        userAdapter.notifyDataSetChanged();
        dialog.dismiss();
        Toast.makeText(this, "Thêm tài khoản thành công", Toast.LENGTH_SHORT).show();
        avatarUrl="";
        imageUri=null;
    }

    //cap nhat

    private void showUpdateUserDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cập nhật thông tin tài khoản");
        avatarUrl = user.getAvatar();

        DialogViewHolderUser viewHolder = DialogViewHolderUser.setupDialogView(this, user, true);
        builder.setView(viewHolder.tv_userId.getRootView());

        builder.setPositiveButton("Cập nhật", null);
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        Button updateButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        updateButton.setOnClickListener(v -> {
            viewHolder.layoutUserId.setError(null);
            viewHolder.layoutName.setError(null);
            viewHolder.layoutAge.setError(null);
            viewHolder.layoutEmail.setError(null);
            viewHolder.layoutPhone.setError(null);
            viewHolder.layoutPassword.setError(null);
            viewHolder.layoutRole.setError(null);
            viewHolder.layoutStatus.setError(null);
            if (validateInputs(viewHolder.tv_userId, viewHolder.tv_name, viewHolder.tv_age, viewHolder.tv_email, viewHolder.tv_phone, viewHolder.tv_password, viewHolder.tv_role, viewHolder.tv_status,
                    viewHolder.layoutUserId, viewHolder.layoutName, viewHolder.layoutAge, viewHolder.layoutEmail, viewHolder.layoutPhone, viewHolder.layoutPassword, viewHolder.layoutRole, viewHolder.layoutStatus)) {

                if (imageUri != null) {
                    if(!avatarUrl.equals(user.getAvatar())){
                        updateUser(viewHolder, user, dialog);
                    }else{
                        Toast.makeText(this, "Đang tải ảnh lên, vui lòng chờ...", Toast.LENGTH_SHORT).show();
                        updateButton.setEnabled(false);
                        uploadImageToFirebase(dialogViewHolderUser.progressBar2,() -> updateUser(viewHolder, user, dialog));
                    }
                    imageUri=null;
                } else {
                    updateUser(viewHolder, user, dialog);
                }
            }
        });

        viewHolder.iv_camera.setOnClickListener(v -> openFileChooser(viewHolder.iv_avatar));
        this.dialogViewHolderUser = viewHolder;
    }

    private void uploadImageToFirebase(ProgressBar progressBar2,Runnable onSuccessCallback) {
        if (imageUri != null) {
            progressBar2.setVisibility(View.VISIBLE);
            StorageReference fileReference = FirebaseStorage.getInstance()
                    .getReference("uploads/" + System.currentTimeMillis() + ".jpg");

            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        avatarUrl = uri.toString();
                        progressBar2.setVisibility(View.GONE);

                        Log.d("Upload", "Tải ảnh thành công, URL: " + avatarUrl);
                        onSuccessCallback.run();
                    }))
                    .addOnFailureListener(e -> Toast.makeText(this, "Upload thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            onSuccessCallback.run();
        }
    }



    private void updateUser(DialogViewHolderUser viewHolder, User user, AlertDialog dialog) {
        String userId = viewHolder.tv_userId.getText().toString().trim();
        String name = viewHolder.tv_name.getText().toString().trim();
        String age = viewHolder.tv_age.getText().toString().trim();
        String email = viewHolder.tv_email.getText().toString().trim();
        String phone = viewHolder.tv_phone.getText().toString().trim();
        String password = viewHolder.tv_password.getText().toString().trim();
        String role = viewHolder.tv_role.getText().toString().trim();
        String status = viewHolder.tv_status.getText().toString().trim();

        user.setUserId(userId);
        user.setName(name);
        user.setAge(age);
        user.setEmail(email);
        user.setPhoneNumber(phone);
        user.setPassword(password);
        user.setRole(role);
        user.setStatus(status);
        user.setAvatar(avatarUrl);


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("User").document(userId).set(user)
                .addOnSuccessListener(aVoid -> {
                    for (int i = 0; i < userList.size(); i++) {
                        if (userList.get(i).getUserId().equals(userId)) {
                            userList.set(i, user);
                            break;
                        }
                    }
                    dialog.dismiss();
                    userAdapter.notifyDataSetChanged();


                    Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        avatarUrl="";
    }


    //xoa
    private void showDeleteUserDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xóa tài khoản");
        builder.setMessage("Bạn có chắc chắn muốn xóa tài khoản này không?");

        builder.setPositiveButton("Xóa", (dialog, which) -> {
            deleteUser(user);
            dialog.dismiss();
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteUser(User user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Xóa User khỏi Firestore bằng userId
        db.collection("User").document(user.getUserId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    userList.remove(user);
                    userAdapter.notifyDataSetChanged();

                    Toast.makeText(this, "Xóa tài khoản thành công", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Xóa thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    private void openFileChooser(ImageView iv_avatar) {
        dialogAvatarImageView = iv_avatar;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            if (dialogAvatarImageView != null) {
                dialogAvatarImageView.setImageURI(imageUri);
                uploadImageToFirebase(dialogViewHolderUser.progressBar2,() -> {
                });
            }
        }
    }



    private boolean validateInputs(EditText tv_userId, EditText tv_name, EditText tv_age, EditText tv_email, EditText tv_phone, EditText tv_password, EditText tv_role, EditText tv_status,
                                   TextInputLayout layoutUserId, TextInputLayout layoutName, TextInputLayout layoutAge, TextInputLayout layoutEmail, TextInputLayout layoutPhone, TextInputLayout layoutPassword, TextInputLayout layoutRole, TextInputLayout layoutStatus) {

        if (tv_userId.getText().toString().trim().isEmpty()) {
            layoutUserId.setError("Mã tài khoản không được để trống");
            tv_userId.requestFocus();
            return false;
        }

        if (tv_name.getText().toString().trim().isEmpty()) {
            layoutName.setError("Họ và tên không được để trống");
            tv_name.requestFocus();
            return false;
        }

        if (tv_age.getText().toString().trim().isEmpty()) {
            layoutAge.setError("Tuổi không được để trống");
            tv_age.requestFocus();
            return false;
        }

        String email = tv_email.getText().toString().trim();
        if (email.isEmpty()) {
            layoutEmail.setError("Email không được để trống");
            tv_email.requestFocus();
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            layoutEmail.setError("Email không đúng định dạng");
            tv_email.requestFocus();
            return false;
        }

        if (tv_phone.getText().toString().trim().isEmpty()) {
            layoutPhone.setError("Số điện thoại không được để trống");
            tv_phone.requestFocus();
            return false;
        }

        if (tv_password.getText().toString().trim().isEmpty()) {
            layoutPassword.setError("Mật khẩu không được để trống");
            tv_password.requestFocus();
            return false;
        }

        String role = tv_role.getText().toString().trim().toLowerCase();
        if (role.isEmpty()) {
            layoutRole.setError("Vai trò không được để trống");
            tv_role.requestFocus();
            return false;
        }
        if (!role.equalsIgnoreCase("manager") && !role.equalsIgnoreCase("employee")) {
            layoutRole.setError("Vai trò chỉ có thể là 'manager' hoặc 'employee'");
            tv_role.requestFocus();
            return false;
        }

        String status = tv_status.getText().toString().trim().toLowerCase();
        if (status.isEmpty()) {
            layoutStatus.setError("Trạng thái không được để trống");
            tv_status.requestFocus();
            return false;
        } else if (!status.equalsIgnoreCase("normal") &&!status.equalsIgnoreCase("block")) {
            layoutStatus.setError("Trạng thái chỉ có thể là 'normal' hoặc 'block'");
            tv_status.requestFocus();
            return false;
        }

        return true;
    }


    private void showHistoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_history, null);
        builder.setView(dialogView);

        RecyclerView recyclerViewHistory = dialogView.findViewById(R.id.recyclerViewHistory);
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));

        List<UserActivityHistory> historyList = new ArrayList<>();
        HistoryAdapter historyAdapter = new HistoryAdapter(historyList);
        recyclerViewHistory.setAdapter(historyAdapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("UserActivityHistory")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    historyList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String userId = document.getString("userId");
                        String name = document.getString("name");
                        String avatar = document.getString("avatar");
                        Timestamp timestamp = document.getTimestamp("timestamp");


                        String formattedTimestamp = "";
                        if (timestamp != null) {
                            formattedTimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                    .format(timestamp.toDate());
                        }

                        historyList.add(new UserActivityHistory(avatar, name, userId, formattedTimestamp));
                    }
                    historyAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi tải lịch sử", Toast.LENGTH_SHORT).show());

        builder.setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }


}