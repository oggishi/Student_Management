package ong.myapp.studentmanagement.model;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Student {
    private String studentId;
    private String name;
    private String birthday;
    private String facultyId;
    private String imageURL;
    private  String classname;
    private String teacher;


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

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(String facultyId) {
        this.facultyId = facultyId;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getClassname() {
        return classname;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    Student(){

    }

    public Student(String studentId, String name, String birthday, String classname, String faculty, String avatarUrl, String teacher) {
        this.studentId = studentId;
        this.name = name;
        this.birthday = birthday;
        this.facultyId = faculty;
        this.imageURL = avatarUrl;
        this.classname = classname;
        this.teacher = teacher;
    }

    public void saveToFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> studentData = new HashMap<>();
        studentData.put("studentId", studentId);
        studentData.put("name", name);
        studentData.put("birthday", birthday);
        studentData.put("classname", classname);
        studentData.put("facultyId", facultyId);
        studentData.put("imageURL", imageURL);
        studentData.put("teacher", teacher);

        db.collection("Student").document(studentId)
                .set(studentData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Student data successfully saved!"))
                .addOnFailureListener(e -> Log.w("Firestore", "Error saving student data", e));
    }
}
