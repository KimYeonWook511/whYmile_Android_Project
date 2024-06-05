package com.inhatc.final_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnLogin, btnFindPw, btnJoin, btnLoginExit;
    private EditText edtId, edtPw;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 바 없애기 -> themes.xml에서 설정함
        setContentView(R.layout.activity_login);

        btnLogin = (Button)findViewById(R.id.btnLogin);
        btnFindPw = (Button)findViewById(R.id.btnFindPw);
        btnJoin = (Button)findViewById(R.id.btnJoin);
        btnLoginExit = (Button)findViewById(R.id.btnLoginExit);
        edtId = (EditText)findViewById(R.id.edtId);
        edtPw = (EditText)findViewById(R.id.edtPw);

        btnLogin.setOnClickListener(this);
        btnFindPw.setOnClickListener(this);
        btnJoin.setOnClickListener(this);
        btnLoginExit.setOnClickListener(this);

        databaseRef = FirebaseDatabase.getInstance().getReference("Inhatc_Android_FinalProject");
    }

    @Override
    public void onClick(View v) {
        if (v == btnLogin) {
            if (edtId.getText().toString().trim().equals("")) {
                Toast.makeText(this, "아이디를 입력해 주세요.", Toast.LENGTH_SHORT).show();
                edtId.requestFocus();

            } else if (edtPw.getText().toString().trim().equals("")) {
                Toast.makeText(this, "비밀번호를 입력해 주세요.", Toast.LENGTH_SHORT).show();
                edtPw.requestFocus();

            } else {
                // 로그인 처리
                login_user();

            }

        } else if (v == btnJoin) {
            // 회원가입 창으로 이동
            startActivityForResult(new Intent(LoginActivity.this, JoinActivity.class), 1);

        } else if (v == btnFindPw) {
            // 비밀번호 찾기 창으로 이동
            startActivityForResult(new Intent(LoginActivity.this, FindPwActivity.class), 1);

        } else if (v == btnLoginExit) {
            // 앱 종료
            finishAffinity(); // 루트 액티비티 종료
            System.runFinalization(); // 작업중인 쓰레드가 다 종료시 종료
            System.exit(0); // 현재 액티비티 종료

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            // 사용자가 뒤로가기 눌렀을 때를 위한 방지

        } else if (requestCode == 1) {
            // 결과 메세지 출력
            Toast.makeText(this, data.getStringExtra("msg"), Toast.LENGTH_SHORT).show();
        }
    }

    private void login_user() {
        databaseRef.child("User").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(edtId.getText().toString()).child("userId").getValue() == null) {
                    // 입력된 아이디가 데이터 베이스에 존재하지 않을 경우
                    Toast.makeText(LoginActivity.this, "아이디가 존재하지 않습니다.", Toast.LENGTH_SHORT).show();

                } else if (!snapshot.child(edtId.getText().toString()).child("userPw").getValue().toString().equals(edtPw.getText().toString())) {
                    // 입력된 비밀번호가 데이터 베이스에 저장되어 있는 아이디의 비밀번호와 일치하지 않을 경우
                    Toast.makeText(LoginActivity.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();

                } else {
                    // 로그인 성공
                    startActivity(new Intent(LoginActivity.this, MenuActivity.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "로그인 중 에러가 발생했습니다.\nerror : " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}