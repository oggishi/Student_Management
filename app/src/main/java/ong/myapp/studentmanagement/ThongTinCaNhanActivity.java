package ong.myapp.studentmanagement;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.InputStream;

import ong.myapp.studentmanagement.model.User;

public class ThongTinCaNhanActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private Button btnEdit, btnSave;
    ImageView iv_Off, iv_Camera, iv_Avatar, btnExit;
    EditText tv_id, tv_name, tv_email, tv_phone, tv_age, tv_role, tv_password, tv_status;
    String id, role;
    Uri imageUri;
    private String avatarUrl = "";
    User user;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_thong_tin_ca_nhan);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        id = getIntent().getStringExtra("id");
        role = getIntent().getStringExtra("role");
        anhXa();

        Glide.with(this)
                .asGif()
                .load(R.raw.off)
                .into(iv_Off);

    }

    private void anhXa() {
        btnExit = findViewById(R.id.btnExit);
        btnEdit = findViewById(R.id.btnEdit);
        btnSave = findViewById(R.id.btnSave);
        iv_Off = findViewById(R.id.iv_Off);
        iv_Camera = findViewById(R.id.ivCamera);
        iv_Avatar = findViewById(R.id.ivAvatar);
        tv_id = findViewById(R.id.tviD);
        tv_name = findViewById(R.id.tvName);
        tv_email = findViewById(R.id.tvEmail);
        tv_phone = findViewById(R.id.tvPhone);
        tv_age = findViewById(R.id.tvAge);
        tv_role = findViewById(R.id.tvRole);
        tv_password = findViewById(R.id.tvPassword);
        tv_status = findViewById(R.id.tvStatus);
        progressBar = findViewById(R.id.progress);

        tv_name.setEnabled(false);
        tv_email.setEnabled(false);
        tv_phone.setEnabled(false);
        tv_age.setEnabled(false);
        tv_password.setEnabled(false);
        tv_status.setEnabled(false);
        tv_role.setEnabled(false);
        tv_id.setEnabled(false);
        iv_Camera.setEnabled(false);
        btnSave.setVisibility(View.GONE);
        btnEdit.setOnClickListener(view -> editProfile());
        btnSave.setOnClickListener(view -> saveProfile());
        iv_Camera.setOnClickListener(view -> editAvatar());
        iv_Avatar.setOnClickListener(view -> editAvatar());
        iv_Off.setOnClickListener(view -> dangxuat());
        btnExit.setOnClickListener(view -> finish());
        loadData();


    }

    private void loadData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("User")
                .whereEqualTo("userId", id)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {

                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);

                        String userId = document.getString("userId");
                        String name = document.getString("name");
                        String avatar = document.getString("avatar");
                        String email = document.getString("email");
                        String age = document.getString("age");
                        String phone = document.getString("phoneNumber");
                        String password = document.getString("password");
                        String status = document.getString("status");
                        String role = document.getString("role");
                        progressBar.setVisibility(View.VISIBLE);
                        Glide.with(this)
                                .load(avatar)
                                .apply(new RequestOptions().placeholder(R.drawable.ic_launcher_background)
                                        .error(R.drawable.ic_launcher_background))
                                .into(iv_Avatar);
                        progressBar.setVisibility(View.GONE);
                        avatarUrl = avatar;
                        Log.d("avatar", avatarUrl);
                        tv_id.setText(userId);
                        tv_name.setText(name);
                        tv_email.setText(email);
                        tv_phone.setText(phone);
                        tv_age.setText(age);
                        tv_password.setText(password);
                        tv_status.setText(status);
                        tv_role.setText(role);
                    }
                });
    }

    private void editProfile() {
        if (role.equalsIgnoreCase("employee")) {
            iv_Camera.setEnabled(true);
        } else {
            tv_name.setEnabled(true);
            tv_email.setEnabled(true);
            tv_phone.setEnabled(true);
            tv_age.setEnabled(true);
            tv_password.setEnabled(true);
            iv_Camera.setEnabled(true);
        }

        btnSave.setVisibility(View.VISIBLE);
        btnEdit.setVisibility(View.GONE);
    }

    private void saveProfile() {
        tv_name.setEnabled(false);
        tv_email.setEnabled(false);
        tv_phone.setEnabled(false);
        tv_age.setEnabled(false);
        tv_password.setEnabled(false);
        iv_Camera.setEnabled(false);
        btnSave.setVisibility(View.GONE);
        btnEdit.setVisibility(View.VISIBLE);

        user = new User(id, tv_name.getText().toString(), tv_age.getText().toString(), tv_phone.getText().toString(), tv_role.getText().toString(), tv_status.getText().toString(), tv_password.getText().toString(), tv_email.getText().toString(), avatarUrl);
        Log.d("avatar", avatarUrl);
        updateUser(user);
    }

    private void editAvatar() {
        openFileChooser(iv_Avatar);

    }

    private void openFileChooser(ImageView iv_avatar) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        btnSave.setEnabled(false);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            if (iv_Avatar != null) {
                iv_Avatar.setImageURI(imageUri);
                uploadImageToFirebase();
            }
        }
    }

    private void uploadImageToFirebase() {
        if (imageUri != null) {
            progressBar.setVisibility(View.VISIBLE);
            StorageReference fileReference = FirebaseStorage.getInstance()
                    .getReference("uploads/" + System.currentTimeMillis() + ".jpg");

            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        avatarUrl = uri.toString();
                        Log.d("avatar", avatarUrl);
                        progressBar.setVisibility(View.GONE);
                        btnSave.setEnabled(true);
                    }))
                    .addOnFailureListener(e -> {
                        btnSave.setEnabled(true);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Upload thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateUser(User user) {
        tv_name.setText(user.getName());
        tv_email.setText(user.getEmail());
        tv_phone.setText(user.getPhoneNumber());
        tv_age.setText(user.getAge());
        tv_password.setText(user.getPassword());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("User").document(user.getUserId()).set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void dangxuat() {
        Intent intent = new Intent(ThongTinCaNhanActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}