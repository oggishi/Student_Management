package ong.myapp.studentmanagement.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import ong.myapp.studentmanagement.R;
import ong.myapp.studentmanagement.model.Student;

public class StudentAdapter extends BaseAdapter {

    private final Context context;
    private final List<Student> students;
    private final LayoutInflater inflater;

    public StudentAdapter(Context context, List<Student> students) {
        this.context = context;
        this.students = new ArrayList<>(students);
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return students.size();
    }

    @Override
    public Student getItem(int position) {
        return students.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.item_student, parent, false);
        }

        ImageView studentImage = view.findViewById(R.id.imgStudent);
        TextView studentName = view.findViewById(R.id.tvFullName);

        Student student = students.get(position);
        String imageURL = student.getImageURL();
        studentName.setText(student.getName());


        Glide.with(context)
                .load(imageURL.isEmpty() ? null : imageURL)
                .apply(new RequestOptions().placeholder(R.drawable.avatar).error(R.drawable.avatar))
                .into(studentImage);

        return view;
    }

    public void updateList(List<Student> newList) {
        students.clear();
        students.addAll(newList);
        notifyDataSetChanged();
    }
}
