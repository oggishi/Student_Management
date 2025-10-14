package ong.myapp.studentmanagement;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;

import ong.myapp.studentmanagement.model.Certificate;

public class DialogViewHolderTinChi {
    public EditText edtCerId, edtStudentId, edtName, edtFaculty, edtStatus, edtDescription;
    public TextInputLayout layoutCerId, layoutStudentId, layoutName, layoutFaculty, layoutStatus, layoutDescription;

    public DialogViewHolderTinChi(View view) {
        edtCerId = view.findViewById(R.id.edtCerId);
        edtStudentId = view.findViewById(R.id.edtStudentId);
        edtName = view.findViewById(R.id.edtName);
        edtFaculty = view.findViewById(R.id.edtFaculty);
        edtStatus = view.findViewById(R.id.edtStatusTC);
        edtDescription = view.findViewById(R.id.edtDescription);

        layoutCerId = view.findViewById(R.id.layoutCerId);
        layoutStudentId = view.findViewById(R.id.layoutStudentId);
        layoutName = view.findViewById(R.id.layoutName);
        layoutFaculty = view.findViewById(R.id.layoutFaculty);
        layoutStatus = view.findViewById(R.id.layoutStatusTC);
        layoutDescription = view.findViewById(R.id.layoutDescription);
    }

    public static DialogViewHolderTinChi setupDialogView(Context context, Certificate certificate, boolean isUpdate) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_add_tin_chi, null);
        DialogViewHolderTinChi viewHolder = new DialogViewHolderTinChi(view);

        if (certificate != null) {
            viewHolder.edtCerId.setText(certificate.getCerId());
            viewHolder.edtStudentId.setText(certificate.getStudentId());
            viewHolder.edtName.setText(certificate.getName());
            viewHolder.edtFaculty.setText(certificate.getFaculty());
            viewHolder.edtStatus.setText(certificate.getStatus());
            viewHolder.edtDescription.setText(certificate.getDescription());

            if (isUpdate) {
                viewHolder.edtCerId.setEnabled(false);
                viewHolder.edtStudentId.setEnabled(false);
            }
        }

        return viewHolder;
    }
}
