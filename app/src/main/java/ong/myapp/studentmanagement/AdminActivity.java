package ong.myapp.studentmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AdminActivity extends AppCompatActivity {
    ImageView btnBack,btnTinChi,btnTaiKhoan,btnStudent,btnThongtin;
    String userRole,userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);
        anhXa();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
         userRole = getIntent().getStringExtra("role");
         userId=getIntent().getStringExtra("id");
        if (userRole == null || !userRole.equalsIgnoreCase("admin")) {
            btnTaiKhoan.setEnabled(false);
            btnTaiKhoan.setAlpha(0.5f);
        }
        btnBack.setOnClickListener(view -> finish());
        btnTinChi.setOnClickListener(view->openTinChiActivity());
        btnTaiKhoan.setOnClickListener(view -> openTaiKhoanActivity());
        btnStudent.setOnClickListener(view -> openStudentActivity());
        btnThongtin.setOnClickListener(view -> openThongTinActivity());
    }
    private void openTinChiActivity(){
        Intent intent=new Intent(this,QuanLyTinChiActivity.class);
        intent.putExtra("role",userRole);
        startActivity(intent);
    }
    private void openTaiKhoanActivity(){
        Intent intent=new Intent(this,QuanLyTaiKhoanActivity.class);
        startActivity(intent);
    }
    private void openStudentActivity(){

        Intent intent=new Intent(AdminActivity.this, StudentActivity.class);
        intent.putExtra("role",userRole);
        startActivity(intent);
    }
    private void openThongTinActivity(){
        Intent intent=new Intent(this,ThongTinCaNhanActivity.class);
        intent.putExtra("id",userId);
        intent.putExtra("role",userRole);
        startActivity(intent);
    }
    private void anhXa(){
        btnBack=findViewById(R.id.btnExit);
        btnTinChi=findViewById(R.id.imageViewqltinchi);
        btnTaiKhoan=findViewById(R.id.imageViewqluser);
        btnStudent=findViewById(R.id.imageViewqlstudent);
        btnThongtin=findViewById(R.id.imageViewprofile);
    }
}