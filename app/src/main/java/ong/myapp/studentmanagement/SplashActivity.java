package ong.myapp.studentmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import ong.myapp.studentmanagement.MainActivity;

public class SplashActivity extends AppCompatActivity {

    private TextView dotsTextView;
    private Handler handler = new Handler();
    private int dotCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        dotsTextView = findViewById(R.id.dots);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String dots = "";
                for (int i = 0; i <= dotCount; i++) {
                    dots += ".";
                }
                dotsTextView.setText(dots);

                dotCount++;
                if (dotCount > 3) {
                    dotCount = 0;
                }

                handler.postDelayed(this, 500);
            }
        }, 500);

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, 3000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
