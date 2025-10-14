package ong.myapp.studentmanagement.adapter;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ong.myapp.studentmanagement.R;

public class CertificateAdapter extends ArrayAdapter<String> {
    private Context context;
    private List<String> items;
    private SparseBooleanArray checkBoxStates = new SparseBooleanArray();
    public CertificateAdapter(Context context, List<String> items) {
        super(context, 0, items);
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_certificates, parent, false);
        }

        String currentItem = items.get(position);

        TextView tvTitle = convertView.findViewById(R.id.tvTitle);
        CheckBox checkBox = convertView.findViewById(R.id.cb_itemCertificate);

        tvTitle.setText(currentItem);
        checkBox.setOnCheckedChangeListener(null);

        // Đặt trạng thái của CheckBox từ SparseBooleanArray
        checkBox.setChecked(checkBoxStates.get(position, false));

        // Cập nhật trạng thái khi CheckBox được chọn/bỏ chọn
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            checkBoxStates.put(position, isChecked);
        });

        return convertView;
    }

    public List<String> getSelectedItems() {
        List<String> selectedItems = new ArrayList<>();
        for (int i = 0; i < checkBoxStates.size(); i++) {
            if (checkBoxStates.valueAt(i)) {
                selectedItems.add(items.get(checkBoxStates.keyAt(i)));
            }
        }

        return selectedItems;
    }

    public void checkItem(int position) {
        checkBoxStates.put(position, true);
    }

    public void uncheckAll() {
        for (int i = 0; i < checkBoxStates.size(); i++) {
            checkBoxStates.put(checkBoxStates.keyAt(i), false); // Đặt tất cả trạng thái là unchecked
        }
        notifyDataSetChanged(); // Cập nhật giao diện
    }

    public void unCheckItem(int position) {
        checkBoxStates.put(position, false);
    }

}

