package ong.myapp.studentmanagement.model;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;

public class User {
    private String userId;
    private String name;
    private String age;
    private String phoneNumber;
    private String role;
    private String status;

    public User(String userId, String name, String age, String phoneNumber, String role, String status, String password, String email, String avatar) {
        this.userId = userId;
        this.name = name;
        this.age = age;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.status = status;
        this.password = password;
        this.email = email;
        this.avatar = avatar;
    }

    private String password;
    private String email;

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    private String avatar;

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }



    User() {

    }



    public void saveToFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", userId);
        userData.put("name", name);
        userData.put("age", age);
        userData.put("phoneNumber", phoneNumber);
        userData.put("role", role);
        userData.put("status", status);
        userData.put("password", password);
        userData.put("email", email);

        db.collection("User").document(userId)
                .set(userData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "User data successfully saved!"))
                .addOnFailureListener(e -> Log.w("Firestore", "Error saving user data", e));
    }
}