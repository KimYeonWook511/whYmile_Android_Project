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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FindPwActivity extends AppCompatActivity implements View.OnClickListener {

    private Spinner spinner;
    private Button btnFindPwAct;
    private EditText edtFindPwId, edtFindPwAnswer;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_pw);

        btnFindPwAct = (Button)findViewById(R.id.btnFindPwAct);
        edtFindPwId = (EditText)findViewById(R.id.edtFindPwId);
        edtFindPwAnswer = (EditText)findViewById(R.id.edtFindPwAnswer);
        spinner = (Spinner)findViewById(R.id.spnFindPwQuestion);

        String[] items = new String[]{"비밀번호 찾기 질문을 선택해주세요", "나의 제 보물 1호는?", "나의 초등학교는?", "좋아하는 연예인은?"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinner.setAdapter(adapter);

        btnFindPwAct.setOnClickListener(this);

        databaseRef = FirebaseDatabase.getInstance().getReference("Inhatc_Android_FinalProject");
    }

    @Override
    public void onClick(View v) {
        if (v == btnFindPwAct) {
            if (edtFindPwId.getText().toString().trim().equals("")) {
                Toast.makeText(this, "아이디를 입력해 주세요.", Toast.LENGTH_SHORT).show();
                edtFindPwId.requestFocus();

            } else if (spinner.getSelectedItemPosition() == 0) {
                Toast.makeText(this, "질문을 선택해 주세요.", Toast.LENGTH_SHORT).show();
                spinner.setFocusable(true);
                spinner.setFocusableInTouchMode(true);
                spinner.requestFocus();

            } else if (edtFindPwAnswer.getText().toString().trim().equals("")) {
                Toast.makeText(this, "답변을 입력해 주세요.", Toast.LENGTH_SHORT).show();
                edtFindPwAnswer.requestFocus();

            } else {
                // 비밀번호 찾기 처리
                find_pw();
            }
        }
    }

    private void find_pw() {
        databaseRef.child("User").child(edtFindPwId.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() == null)  {
                    // 입력한 아이디가 존재하지 않을 경우
                    Toast.makeText(FindPwActivity.this, "입력된 정보가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                    edtFindPwId.setText("");
                    spinner.setSelection(0);
                    edtFindPwAnswer.setText("");

                } else if (snapshot.child("userQuestion").getValue().toString().equals(spinner.getSelectedItem().toString())
                            && snapshot.child("userAnswer").getValue().toString().equals(edtFindPwAnswer.getText().toString())) {
                    // 매크로 방지 후
                    // 입력한 아이디에 대한 질문과 답변이 일치하는 경우 비밀번호 변경
                    reCAPTCHA();

                } else {
                    // 입력한 아이디에 대한 질문이나 답변이 일치하지 않을 경우
                    Toast.makeText(FindPwActivity.this, "입력된 정보가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                    edtFindPwId.setText("");
                    spinner.setSelection(0);
                    edtFindPwAnswer.setText("");

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FindPwActivity.this, "비밀번호 찾기 중 에러가 발생했습니다.\nerror : " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            // 성공적으로 비밀번호 변경 완료시
            change_pw(data.getStringExtra("changePw"));
            setResult(RESULT_OK, getIntent().putExtra("msg", "비밀번호 변경 완료"));
            finish();
        }
    }

    private void change_pw(String changePw) {
        databaseRef.child("User").child(edtFindPwId.getText().toString()).child("userPw").setValue(changePw);
    }

    private void reCAPTCHA() { // reCAPTCHA 사용
        SafetyNet.getClient(FindPwActivity.this).verifyWithRecaptcha("6LddimEgAAAAAIr6h8ostCoZaqBzCFieAl_bPCW8")
                .addOnSuccessListener(new OnSuccessListener<SafetyNetApi.RecaptchaTokenResponse>() {
                    @Override
                    public void onSuccess(SafetyNetApi.RecaptchaTokenResponse recaptchaTokenResponse) {
                        String captchaToken = recaptchaTokenResponse.getTokenResult();

                        if (captchaToken != null) {
                            if (!captchaToken.isEmpty()) {
                                // 보안문자 입력 성공
                                startActivityForResult(new Intent(FindPwActivity.this, ChangePwActivity.class), 1);
                                
                            } else {
                                Toast.makeText(FindPwActivity.this, "보안문자 입력에 실패하셨습니다.", Toast.LENGTH_SHORT).show();

                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(FindPwActivity.this, "Captcha 연결에 실패하셨습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}