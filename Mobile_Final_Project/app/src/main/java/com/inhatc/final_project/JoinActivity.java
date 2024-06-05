package com.inhatc.final_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class JoinActivity extends AppCompatActivity implements View.OnClickListener {

    private Spinner spinner;
    private Button btnJoinAct;
    private EditText edtJoinId, edtJoinPw, edtJoinPwChk, edtJoinName, edtJoinAnswer;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        btnJoinAct = (Button)findViewById(R.id.btnJoinAct);
        edtJoinId = (EditText)findViewById(R.id.edtJoinId);
        edtJoinPw = (EditText)findViewById(R.id.edtJoinPw);
        edtJoinPwChk = (EditText)findViewById(R.id.edtJoinPwChk);
        edtJoinName = (EditText)findViewById(R.id.edtJoinName);
        edtJoinAnswer = (EditText)findViewById(R.id.edtJoinAnswer);
        spinner = (Spinner)findViewById(R.id.spnJoinQuestion);

        String[] items = new String[]{"비밀번호 찾기 질문을 선택해주세요", "나의 제 보물 1호는?", "나의 초등학교는?", "좋아하는 연예인은?"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinner.setAdapter(adapter);

        btnJoinAct.setOnClickListener(this);

        databaseRef = FirebaseDatabase.getInstance().getReference("Inhatc_Android_FinalProject");

    }

    @Override
    public void onClick(View v) {
        if (v == btnJoinAct) {
            if (edtJoinId.getText().toString().trim().equals("")) {
                Toast.makeText(this, "아이디를 입력해 주세요.", Toast.LENGTH_SHORT).show();
                edtJoinId.requestFocus();

            } else if (edtJoinPw.getText().toString().trim().equals("")) {
                Toast.makeText(this, "비밀번호를 입력해 주세요.", Toast.LENGTH_SHORT).show();
                edtJoinPw.requestFocus();

            } else if (edtJoinPwChk.getText().toString().trim().equals("")) {
                Toast.makeText(this, "비밀번호 확인을 입력해 주세요.", Toast.LENGTH_SHORT).show();
                edtJoinPwChk.requestFocus();
                
            } else if (edtJoinName.getText().toString().trim().equals("")) {
                Toast.makeText(this, "이름을 입력해 주세요.", Toast.LENGTH_SHORT).show();
                edtJoinName.requestFocus();

            } else if (spinner.getSelectedItemPosition() == 0) {
                Toast.makeText(this, "질문을 선택해 주세요.", Toast.LENGTH_SHORT).show();
                spinner.setFocusable(true);
                spinner.setFocusableInTouchMode(true);
                spinner.requestFocus();

            } else if (edtJoinAnswer.getText().toString().trim().equals("")) {
                Toast.makeText(this, "답변을 입력해 주세요.", Toast.LENGTH_SHORT).show();
                edtJoinAnswer.requestFocus();

            } else if (!edtJoinPw.getText().toString().trim().equals(edtJoinPwChk.getText().toString().trim())) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                edtJoinPw.setText("");
                edtJoinPwChk.setText("");
                edtJoinPw.requestFocus();

            } else if (edtJoinId.getText().toString().trim().length() < 6) {
                Toast.makeText(this, "아이디를 6자 이상 입력해 주세요.", Toast.LENGTH_SHORT).show();
                edtJoinId.setText("");
                edtJoinId.requestFocus();

            } else if (edtJoinPw.getText().toString().trim().length() < 8 || edtJoinPw.getText().toString().trim().length() > 20) {
                Toast.makeText(this, "비밀번호를 8 ~ 20자 입력해 주세요.", Toast.LENGTH_SHORT).show();
                edtJoinPw.setText("");
                edtJoinPwChk.setText("");
                edtJoinPw.requestFocus();

            } else {
                UserVO userVO = new UserVO();
                userVO.setUserId(edtJoinId.getText().toString().trim());
                userVO.setUserPw(edtJoinPw.getText().toString().trim());
                userVO.setUserName(edtJoinName.getText().toString().trim());
                userVO.setUserQuestion(spinner.getSelectedItem().toString());
                userVO.setUserAnswer(edtJoinAnswer.getText().toString().trim());

                // 매크로 방지 이후 회원가입
                reCAPTCHA(userVO);

            }
        }
    }

    private void regist_user(UserVO userVO) {
        databaseRef.child("User").child(userVO.getUserId()).child("userId").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() == null) {
                    databaseRef.child("User").child(userVO.getUserId()).setValue(userVO);
                    setResult(RESULT_OK, getIntent().putExtra("msg", "회원가입 완료"));
                    finish();

                } else {
                    Toast.makeText(JoinActivity.this, "이미 아이디가 존재합니다.", Toast.LENGTH_SHORT).show();
                    edtJoinId.setText("");
                    edtJoinId.requestFocus();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(JoinActivity.this, "회원가입 중 에러가 발생했습니다.\nerror : " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void reCAPTCHA(UserVO userVO) { // reCAPTCHA 사용
        SafetyNet.getClient(JoinActivity.this).verifyWithRecaptcha("6LddimEgAAAAAIr6h8ostCoZaqBzCFieAl_bPCW8")
                .addOnSuccessListener(new OnSuccessListener<SafetyNetApi.RecaptchaTokenResponse>() {
                    @Override
                    public void onSuccess(SafetyNetApi.RecaptchaTokenResponse recaptchaTokenResponse) {
                        String captchaToken = recaptchaTokenResponse.getTokenResult();

                        if (captchaToken != null) {
                            if (!captchaToken.isEmpty()) {
                                // 보안문자 입력 성공
                                regist_user(userVO);

                            } else {
                                Toast.makeText(JoinActivity.this, "보안문자 입력에 실패하셨습니다.", Toast.LENGTH_SHORT).show();

                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(JoinActivity.this, "Captcha 연결에 실패하셨습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}