package ong.myapp.studentmanagement;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputLayout;

import ong.myapp.studentmanagement.model.Student;

public class DialogViewHolderStudent {
    public EditText tv_fullName, tv_birthday, tv_faculty, tv_class, tv_teacher,tv_studentId;
    public TextInputLayout layoutFullName, layoutBirthday, layoutFaculty, layoutClass, layoutTeacher,layoutStudentId;
    public ImageView iv_student;
    public ProgressBar progressBar1;



    // Constructor
    public DialogViewHolderStudent(View view) {
        tv_fullName = view.findViewById(R.id.edt_FullName);
        tv_birthday = view.findViewById(R.id.edt_birthday);
        tv_faculty = view.findViewById(R.id.edt_factualy);
        tv_class = view.findViewById(R.id.edt_class);
        tv_teacher = view.findViewById(R.id.edt_teacher);
        tv_studentId=view.findViewById(R.id.edt_studentID);
        progressBar1=view.findViewById(R.id.progressBar1);
        layoutFullName = view.findViewById(R.id.layoutFullName);
        layoutBirthday = view.findViewById(R.id.layoutBirthday);
        layoutClass = view.findViewById(R.id.layoutClass);
        layoutFaculty = view.findViewById(R.id.layoutFaculty);
        layoutTeacher = view.findViewById(R.id.layoutTeacher);
        layoutStudentId=view.findViewById(R.id.layoutStudentId);

        iv_student = view.findViewById(R.id.img_AddStudent);

    }

    public static DialogViewHolderStudent setupDialogView(Context context, Student student, boolean isUpdate) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_add_student, null);
        DialogViewHolderStudent viewHolder = new DialogViewHolderStudent(view);

        if (student != null) {
            viewHolder.tv_fullName.setText(student.getName());
            viewHolder.tv_birthday.setText(student.getBirthday());
            viewHolder.tv_faculty.setText(student.getFacultyId());
            viewHolder.tv_class.setText(student.getClassname());
            viewHolder.tv_teacher.setText(student.getTeacher());
            viewHolder.tv_studentId.setText(student.getStudentId());
            viewHolder.tv_studentId.setEnabled(false);
            Glide.with(context)
                    .load(student.getImageURL())
                    .into(viewHolder.iv_student);
        }

        return viewHolder;
    }



}




