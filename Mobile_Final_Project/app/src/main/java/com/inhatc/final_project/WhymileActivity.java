package com.inhatc.final_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class WhymileActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnSubmit, btnAgain, btnExit;
    private EditText edtInput;
    private TextView txtWhymile2, txtWhymile3, txtWhymile4, txtWhymile5, txtWhymile6, txtUser1, txtUser2;
    private int inputCnt = 0; // 사용자의 입력 횟수
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whymile);

        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        btnAgain = (Button) findViewById(R.id.btnAgain);
        btnExit = (Button) findViewById(R.id.btnExit);
        edtInput = (EditText) findViewById(R.id.edtInput);
        txtWhymile2 = (TextView) findViewById(R.id.txtWhymile2);
        txtWhymile3 = (TextView) findViewById(R.id.txtWhymile3);
        txtWhymile4 = (TextView) findViewById(R.id.txtWhymile4);
        txtWhymile5 = (TextView) findViewById(R.id.txtWhymile5);
        txtWhymile6 = (TextView) findViewById(R.id.txtWhymile6);
        txtUser1 = (TextView) findViewById(R.id.txtUser1);
        txtUser2 = (TextView) findViewById(R.id.txtUser2);

        // 텍스트 뷰 안보이게 하기
        txtUser1.setVisibility(View.INVISIBLE);
        txtUser2.setVisibility(View.INVISIBLE);
        txtWhymile2.setVisibility(View.INVISIBLE);
        txtWhymile3.setVisibility(View.INVISIBLE);
        txtWhymile4.setVisibility(View.INVISIBLE);
        txtWhymile5.setVisibility(View.INVISIBLE);
        txtWhymile6.setVisibility(View.INVISIBLE);

        databaseRef = FirebaseDatabase.getInstance().getReference("Inhatc_Android_FinalProject");

        btnSubmit.setOnClickListener(this);
        btnAgain.setOnClickListener(this);
        btnExit.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == btnSubmit) {
            if (inputCnt == 0) {
                // 첫 번째 메시지 입력 (사용자가 질문하기)
                inputAct(edtInput.getText().toString().trim()); // 입력에 대한 처리 실행

            } else if (inputCnt == 1) {
                // 두 번째 메시지 입력 (질문에대한 답변하기)
                inputAct(edtInput.getText().toString().trim()); // 입력에 대한 처리 실행

            }

        } else if (v == btnAgain) {
            startActivity(new Intent(WhymileActivity.this, WhymileActivity.class));

        } else if (v == btnExit) {
            finishAffinity(); // 루트 액티비티 종료
            System.runFinalization(); // 작업중인 쓰레드가 다 종료시 종료
            System.exit(0); // 현재 액티비티 종료

        }
    }

    private void inputAct(String input) {
        if (inputCnt == 0) {
            // 사용자의 질문 내용 출력
            txtUser1.setText(input);

            // 1. 사용자의 질문 어절로 나누어서 관련된 답변 있는지 확인
            databaseRef.child("Question").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long dataCnt = snapshot.getChildrenCount(); // PK 번호로 생각
                    for (int i = 0; i < dataCnt; i++) {
                        if (snapshot.child(Integer.toString(i + 1)).child("question").getValue().toString()
                                .equals(input)) {
                            // 사용자의 질문이 이미 존재할 경우

                            // 질문에 대한 답변 출력
                            printAnswer(input);

                            // 사용자에게 질문할 내용 출력
                            printQuestion();

                            return;
                        }
                    }
                    // 사용자의 질문이 존재하지 않을 경우
                    // 질문 저장
                    QuestionVO questionVO = new QuestionVO();
                    questionVO.setNo((int) dataCnt + 1);
                    questionVO.setQuestion(input);
                    databaseRef.child("Question").child(Integer.toString(questionVO.getNo())).setValue(questionVO);

                    // 질문에 대한 답변 출력
                    printAnswer(input);

                    // 사용자에게 질문할 내용 출력
                    printQuestion();

                    return;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(WhymileActivity.this, "사용자의 질문 처리 중 에러가 발생했습니다.\nerror : " + error, Toast.LENGTH_SHORT).show();
                }
            });

        } else if (inputCnt == 1) {
            // 사용자의 질문 내용 출력
            txtUser2.setText(input);

            // 1. 질문에 대한 답변 저장
            databaseRef.child("Answer").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    AnswerVO answerVO = new AnswerVO();
                    answerVO.setNo((int) snapshot.getChildrenCount() + 1);
                    answerVO.setQuestion(txtWhymile4.getText().toString());
                    answerVO.setAnswer(input);
                    databaseRef.child("Answer").child(Integer.toString(answerVO.getNo())).setValue(answerVO);

                    visibleTextView(); // 데이터가 다 입력 되면 TextView 보여주기

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(WhymileActivity.this, "사용자의 답변 처리 중 에러가 발생했습니다.\nerror : " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void printAnswer(String inputQuestion) { // 질문에 대한 답변을 찾는 메소드
        databaseRef.child("Answer").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long dataCnt = snapshot.getChildrenCount();
                String[] inputWords = inputQuestion.split(" "); // 띄어쓰기 기준으로 스플릿
                ArrayList<String> answerList = new ArrayList<String>(); // 답변을 저장할 리스트

                for (int i = 0; i < dataCnt; i++) {
                    int containCnt = 0;

                    for (int k = 0; k < inputWords.length; k++) {
                        if (snapshot.child(Integer.toString(i + 1)).child("question").getValue().toString()
                                .contains(inputWords[k])) {
                            // 어절이 질문에 포함되어 있을 시 containCnt 추가
                            containCnt++;
                        }
                    }
                    // containCnt가 (어절의 수 - 1) 보다 크거나 같을 경우에 해당 질문의 답변 저장하기
                    if (containCnt >= inputWords.length - 1) {
                        // 해당 dbQuestion의 답변을 저장
                        answerList.add(snapshot.child(Integer.toString(i + 1)).child("answer").getValue().toString());
                    }
                }

                if (answerList.size() == 0) {
                    // 해당 질문에 대한 답변이 없는 경우
                    txtWhymile2.setText("저도 잘 모르겠어요.");

                } else {
                    // 해당 질문에 대한 답변이 있는 경우
                    int randomIndex = (int) (Math.random() * answerList.size()); // 답변 리스트에서 랜덤하게 뽑아낼 인덱스

                    for (int k = 0; k < answerList.size(); k++) {
                        if (k == randomIndex) {
                            txtWhymile2.setText(answerList.get(k)); // 랜덤하게 뽑아낸 답변을 출력
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(WhymileActivity.this, "답변 리스트 생성 중 에러가 발생했습니다.\nerror : " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void printQuestion() { // 사용자에게 할 질문을 출력하는 메소드
        databaseRef.child("Question").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long dataCnt = snapshot.getChildrenCount();
                ArrayList<String> questionList = new ArrayList<String>(); // 질문을 저장할 리스트

                for (int i = 0; i < dataCnt; i++) {
                    System.out.println(snapshot.child(Integer.toString(i + 1)).child("question").getValue().toString());
                    questionList.add(snapshot.child(Integer.toString(i + 1)).child("question").getValue().toString());
                }

                int randomIndex = (int) (Math.random() * questionList.size()); // 질문 리스트에서 랜덤하게 뽑아낼 인덱스

                for (int k = 0; k < questionList.size(); k++) {

                    if (k == randomIndex) {
                        txtWhymile4.setText(questionList.get(k));
                    }
                }

                visibleTextView(); // 데이터가 다 입력 되면 TextView 보여주기
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(WhymileActivity.this, "질문 리스트 생성 중 에러가 발생했습니다.\nerror : " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void visibleTextView() { // TextView를 활성화 시키는 메소드
        if (inputCnt == 0) {
            txtUser1.setVisibility(View.VISIBLE); // 유저의 메시지 텍스트 뷰 활성화
            txtWhymile2.setVisibility(View.VISIBLE); // 와이밀리의 메시지 텍스트 뷰 활성화
            txtWhymile3.setVisibility(View.VISIBLE); // 와이밀리의 메시지 텍스트 뷰 활성화
            txtWhymile4.setVisibility(View.VISIBLE); // 와이밀리의 메시지 텍스트 뷰 활성화

        } else if (inputCnt == 1) {
            txtUser2.setVisibility(View.VISIBLE); // 유저의 메시지 텍스트 뷰 활성화
            txtWhymile5.setVisibility(View.VISIBLE); // 와이밀리의 메시지 텍스트 뷰 활성화
            txtWhymile6.setVisibility(View.VISIBLE); // 와이밀리의 메시지 텍스트 뷰 활성화

            edtInput.setVisibility(View.INVISIBLE); // 사용자 메시지 입력 창 비활성화
            btnSubmit.setVisibility(View.INVISIBLE); // 전송 버튼 비활성화
            btnAgain.setVisibility(View.VISIBLE); // 다시하기 버튼 활성화
            btnExit.setVisibility(View.VISIBLE); // 종료하기 버튼 활성화

            btnAgain.setVisibility(View.VISIBLE); // 다시하기 버튼 활성화
            // 키보드 비활성화
            ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(edtInput.getWindowToken(), 0);
            edtInput.setInputType(0); // 입력 방지

        }

        edtInput.setText(""); // 입력 초기화
        inputCnt++;
    }
}