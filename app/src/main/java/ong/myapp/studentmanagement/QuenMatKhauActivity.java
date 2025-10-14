package ong.myapp.studentmanagement;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class QuenMatKhauActivity extends AppCompatActivity {

    private static final String TAG = "QuenMatKhauActivity";

    private TextInputEditText editEmail;
    private TextInputLayout layoutEmail;
    private Button btnXacNhan;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseRemoteConfig remoteConfig;
    private String senderPassword;
    private ImageButton btnExit;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quen_mat_khau);
        anhXa();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        remoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0)
                .build();
        remoteConfig.setConfigSettingsAsync(configSettings);

        fetchRemoteConfig();

        btnXacNhan.setOnClickListener(view -> {
            layoutEmail.setError(null);
            Log.d(TAG, "Xác nhận gửi email khôi phục mật khẩu");
            validateEmailAndSendResetLink();

        });
        btnExit.setOnClickListener(view -> finish());
    }
    private void anhXa(){
        editEmail = findViewById(R.id.editEmail);
        layoutEmail = findViewById(R.id.layoutEmail);
        btnXacNhan = findViewById(R.id.btnXacnhan);
        btnExit=findViewById(R.id.btnExit);
    }

    private void fetchRemoteConfig() {
        remoteConfig.fetch(0)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        remoteConfig.activate().addOnCompleteListener(activateTask -> {
                            if (activateTask.isSuccessful()) {
                                senderPassword = remoteConfig.getString("senderPassword");
                            }

                        });
                    } else {
                        if (task.getException() != null) {
                            Log.e(TAG, "Fetch không thành công: " + task.getException().getMessage());
                        } else {
                            Log.e(TAG, "Fetch không thành công: Lỗi không xác định");
                        }
                    }
                });

    }


    private void validateEmailAndSendResetLink() {
        String email = editEmail.getText().toString().trim();
        Log.d(TAG, "Email nhập vào: " + email);

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            layoutEmail.setError("Email không hợp lệ");
            editEmail.requestFocus();
            return;
        } else {
            layoutEmail.setError(null);
        }

        db.collection("User")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            String password = task.getResult().getDocuments().get(0).getString("password");

                            if (senderPassword != null) {
                                new SendEmailTask(this, senderPassword).execute(email, password);
                            } else {
                                Toast.makeText(this, "Mật khẩu email chưa được tải, vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            layoutEmail.setError("Email này chưa được đăng ký");
                            editEmail.requestFocus();
                        }
                    } else {
                        Toast.makeText(this, "Lỗi kiểm tra email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi kiểm tra email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
