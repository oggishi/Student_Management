package ong.myapp.studentmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private Button btnDangNhap;
    private TextView tvQuenMatKhau,editTextEmail,editTextPassword;
    private TextInputLayout layoutEmail, layoutPassword;
    private ProgressBar progress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        anhXa();
        btnDangNhap.setOnClickListener(view ->clickDangNhap() );
        tvQuenMatKhau.setOnClickListener(view -> clickQuenMatKhau());
    }
    private void clickDangNhap() {
        layoutEmail.setError(null);
        layoutPassword.setError(null);
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        layoutEmail.setError(null);
        layoutPassword.setError(null);

        if (email.isEmpty()) {
            layoutEmail.setError("Vui lòng nhập email");

            return;
        }
        if (password.isEmpty()) {
            layoutPassword.setError("Vui lòng nhập mật khẩu");
            return;

        }
        progress.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("User")
                .whereEqualTo("email", email)
                .whereEqualTo("password", password)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);

                        String userId = document.getString("userId");
                        String name = document.getString("name");
                        String avatar=document.getString("avatar");
                        String logId = document.getId();
                        String role = document.getString("role");
                        String status=document.getString("status");

                        if(status.equalsIgnoreCase("block")){
                            progress.setVisibility(View.GONE);
                            Toast.makeText(this, "Tài khoản của bạn đã bị khóa", Toast.LENGTH_SHORT).show();

                            return;
                        }
                        if (userId != null) {
                            progress.setVisibility(View.GONE);
                            Intent intent = new Intent(this, AdminActivity.class);
                            intent.putExtra("role",role);
                            intent.putExtra("id",userId);
                            startActivity(intent);
                            addLoginHistory(userId,name,avatar);
                        } else {
                            layoutEmail.setError("Không tìm thấy userId, kiểm tra lại dữ liệu.");
                        }
                    } else {
                        progress.setVisibility(View.GONE);
                        layoutEmail.setError("Sai email hoặc mật khẩu");
                    }
                })
                .addOnFailureListener(e -> {
                    layoutEmail.setError("Lỗi đăng nhập: " + e.getMessage());
                });



    }

    private void addLoginHistory(String userId,String name,String avatar) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> activityLog = new HashMap<>();
        activityLog.put("userId", userId);
        activityLog.put("name", name);
        activityLog.put("avatar",avatar);
        activityLog.put("timestamp", FieldValue.serverTimestamp());

        db.collection("UserActivityHistory").add(activityLog)
                .addOnSuccessListener(documentReference -> Log.d("ActivityLog", "Lịch sử đăng nhập đã được ghi với logId: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.w("ActivityLog", "Lỗi ghi lịch sử đăng nhập", e));
    }



    private void clickQuenMatKhau(){
        Intent intent=new Intent(this,QuenMatKhauActivity.class);
        startActivity(intent);
    }
    private void anhXa(){
        btnDangNhap=findViewById(R.id.btnDangnhap);
        tvQuenMatKhau=findViewById(R.id.tvQuenMatKhau);
        editTextEmail=findViewById(R.id.editEmail);
        editTextPassword=findViewById(R.id.editPassword);
        layoutEmail = findViewById(R.id.layoutEmail);
        layoutPassword = findViewById(R.id.layoutPassword);
        progress=findViewById(R.id.progress);

    }


}
