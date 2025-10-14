package ong.myapp.studentmanagement;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;


public class SendEmailTask extends AsyncTask<String, Void, Boolean> {
    private final Context context;
    private final String senderPassword;

    public SendEmailTask(Context context, String senderPassword) {
        this.context = context;
        this.senderPassword = senderPassword;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        String email = params[0];
        String password = params[1];
        MailSender mailSender = new MailSender();
        String subject = "[Quên mật khẩu] QUẢN LÝ THÔNG TIN SINH VIÊN";
        String body = "<h3>Xin chào, " + email + "!</h3>" +
                "<p>Chúng tôi đã nhận được yêu cầu gửi lại mật khẩu từ bạn.</p>" +
                "<p>Mật khẩu của bạn là: <b>" + password + "</b></p>" +
                "<p>Chân thành cảm ơn bạn đã sử dụng dịch vụ!</p>" +
                "<p>Trân trọng,</p>" +
                "<p>Quản lý thông tin sinh viên</p>" +
                "<img src='https://firebasestorage.googleapis.com/v0/b/studentmanagement-1deb2.appspot.com/o/logo.webp?alt=media&token=f66f0dda-861d-4cf2-bdc1-7f080d5daf66'alt='Quản lý thông tin sinh viên Logo' width='100' height='100'>";

        try {
            mailSender.sendEmail(email, subject, body, senderPassword);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            Toast.makeText(context, "Đã gửi mật khẩu qua email!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Gửi email thất bại!", Toast.LENGTH_SHORT).show();
        }
    }
}
