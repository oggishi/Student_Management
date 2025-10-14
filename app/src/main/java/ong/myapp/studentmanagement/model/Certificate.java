package ong.myapp.studentmanagement.model;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Certificate {
    private String cerId;
    private String studentId;
    private String name;

    private String faculty;
    private String status;
    private String description;
    private boolean isChecked; // Thuộc tính để lưu trạng thái checkbox

    // No-argument constructor required for Firebase deserialization
    public Certificate() {
    }

    public Certificate(String cerId, String studentId, String name, String faculty, String status, String description) {
        this.cerId = cerId;
        this.studentId = studentId;
        this.name = name;

        this.faculty = faculty;
        this.status = status;
        this.description = description;
        this.isChecked = false; // Mặc định không được chọn
    }

    // Getter và Setter cho các thuộc tính
    public String getCerId() {
        return cerId;
    }

    public void setCerId(String cerId) {
        this.cerId = cerId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    public String getFaculty() {
        return faculty;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Certificate that = (Certificate) o;
        return cerId.equals(that.cerId); // So sánh bằng ID hoặc các thuộc tính khác mà bạn thấy phù hợp
    }

    @Override
    public int hashCode() {
        return Objects.hash(cerId); // Tạo mã băm dựa trên các thuộc tính duy nhất
    }

    // Phương thức lưu dữ liệu vào Firestore
    public void saveToFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> certificate = new HashMap<>();
        certificate.put("cerId", cerId);
        certificate.put("studentId", studentId);
        certificate.put("name", name);
        certificate.put("faculty", faculty);
        certificate.put("status", status);
        certificate.put("description", description);

        // Không lưu thuộc tính isChecked vào Firestore
        db.collection("Certificate").document(cerId).set(certificate)
                .addOnSuccessListener(aVoid -> {
                    System.out.println("Data saved successfully.");
                    Log.d("Firestore", "Data saved successfully.");
                })
                .addOnFailureListener(e -> {
                    System.err.println("Failed to save data: " + e.getMessage());
                });
    }


}
