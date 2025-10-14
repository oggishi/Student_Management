package ong.myapp.studentmanagement.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import ong.myapp.studentmanagement.QuanLyTinChiActivity;
import ong.myapp.studentmanagement.R;
import ong.myapp.studentmanagement.model.Certificate;

public class TinChiAdapter extends RecyclerView.Adapter<TinChiAdapter.TinChiViewHolder> {

    private List<Certificate> tinChiList;
    private Context context;
    private Certificate selectedCertificate;
    private OnItemClickListener listener;

    public TinChiAdapter(Context context, List<Certificate> tinChiList) {
        this.tinChiList = tinChiList;
        this.context = context;
    }

    public interface OnItemClickListener {
        void onItemClick(Certificate certificate);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TinChiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_tinchi, parent, false);
        return new TinChiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TinChiViewHolder holder, int position) {
        Certificate tinChi = tinChiList.get(position);

        holder.tvName.setText(tinChi.getName());
        holder.tvDescription.setText(tinChi.getDescription());
        holder.tvStatus.setText(tinChi.getStatus());
        holder.tvId.setText("[" + tinChi.getCerId() + "]");
        holder.tvStudentId.setText("MSSV: " + tinChi.getStudentId());
        holder.tvFaculty.setText("Khoa: " + tinChi.getFaculty());


        if (selectedCertificate != null && selectedCertificate.equals(tinChi)) {
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.gray));
        } else {
            holder.itemView.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
        }

        holder.itemView.setOnClickListener(v -> {
            selectedCertificate = tinChi;
            notifyDataSetChanged();
            if (listener != null) {
                listener.onItemClick(tinChi);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            showDeleteConfirmationDialog(tinChi, position);
            return true;
        });
    }



    @Override
    public int getItemCount() {
        return tinChiList.size();
    }

    public Certificate getSelectedCertificate() {
        return selectedCertificate;
    }

    public void updateData(List<Certificate> newList) {
        this.tinChiList.clear();
        this.tinChiList.addAll(newList);
        notifyDataSetChanged();
    }


    public static class TinChiViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDescription, tvStatus, tvId, tvStudentId, tvFaculty, tv_idDetail;

        public TinChiViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.name);
            tvDescription = itemView.findViewById(R.id.description);
            tvStatus = itemView.findViewById(R.id.status);
            tvId = itemView.findViewById(R.id.cerId);
            tvStudentId = itemView.findViewById(R.id.studentId);
            tvFaculty = itemView.findViewById(R.id.faculty);


        }
    }
    private void showDeleteConfirmationDialog(Certificate certificate, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Xác nhận xóa");
        builder.setMessage("Bạn có chắc chắn muốn xóa chứng chỉ này?");
        builder.setPositiveButton("Xóa", (dialog, which) -> {
            deleteCertificate(certificate, position);
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }
    private void deleteCertificate(Certificate certificate, int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Certificate").document(certificate.getCerId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    tinChiList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Xóa chứng chỉ thành công", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Xóa chứng chỉ thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
