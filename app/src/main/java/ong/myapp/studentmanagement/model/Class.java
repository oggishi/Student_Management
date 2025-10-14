package ong.myapp.studentmanagement.model;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class Class {
    private String classId;
    private String facultyId;
    private int numberStudent;
    private String teacherMana;
Class(){

}
    public Class(String classId, String facultyId, int numberStudent, String teacherMana) {
        this.classId = classId;
        this.facultyId = facultyId;
        this.numberStudent = numberStudent;
        this.teacherMana = teacherMana;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public String getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(String facultyId) {
        this.facultyId = facultyId;
    }

    public int getNumberStudent() {
        return numberStudent;
    }

    public void setNumberStudent(int numberStudent) {
        this.numberStudent = numberStudent;
    }

    public String getTeacherMana() {
        return teacherMana;
    }

    public void setTeacherMana(String teacherMana) {
        this.teacherMana = teacherMana;
    }

    public void saveToFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> classData = new HashMap<>();
        classData.put("classId", classId);
        classData.put("facultyId", facultyId);
        classData.put("numberStudent", numberStudent);
        classData.put("teacherMana", teacherMana);

        db.collection("Class").document(classId)
                .set(classData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Class data successfully saved!"))
                .addOnFailureListener(e -> Log.w("Firestore", "Error saving class data", e));
    }
}