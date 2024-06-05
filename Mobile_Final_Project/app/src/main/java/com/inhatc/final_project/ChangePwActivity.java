package com.inhatc.final_project;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class ChangePwActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnChangePwAct;
    private EditText edtChangePw, edtChangePwChk;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pw);

        btnChangePwAct = (Button)findViewById(R.id.btnChangePwAct);
        edtChangePw = (EditText)findViewById(R.id.edtChangePw);
        edtChangePwChk = (EditText)findViewById(R.id.edtChangePwChk);

        btnChangePwAct.setOnClickListener(this);

        databaseRef = FirebaseDatabase.getInstance().getReference("Inhatc_Android_FinalProject");
    }

    @Override
    public void onClick(View v) {
        if (v == btnChangePwAct) {
            if (edtChangePw.getText().toString().trim().equals("")) {
                Toast.makeText(this, "변경할 비밀번호를 입력해 주세요.", Toast.LENGTH_SHORT).show();
                edtChangePw.requestFocus();

            } else if (edtChangePwChk.getText().toString().trim().equals("")) {
                Toast.makeText(this, "비밀번호 확인을 입력해 주세요.", Toast.LENGTH_SHORT).show();
                edtChangePwChk.requestFocus();

            } else if (!edtChangePw.getText().toString().trim().equals(edtChangePwChk.getText().toString().trim())) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                edtChangePw.setText("");
                edtChangePwChk.setText("");
                edtChangePw.requestFocus();

            } else if (edtChangePw.getText().toString().trim().length() < 8 || edtChangePw.getText().toString().trim().length() > 20) {
                Toast.makeText(this, "변경할 비밀번호를 8 ~ 20자 입력해 주세요.", Toast.LENGTH_SHORT).show();
                edtChangePw.setText("");
                edtChangePwChk.setText("");
                edtChangePw.requestFocus();

            } else {
                // 변경할 비밀번호 반환
                setResult(RESULT_OK, getIntent().putExtra("changePw", edtChangePw.getText().toString()));
                finish();
            }
        }
    }
}