package kr.co.hoon.a180524htmlread;

import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    EditText editText;
    TextView textView;

    ProgressDialog progressDialog;

    // 다운로드 받은 데이터를 textView에 출력하는 Handler
    // 메인스레드의 메시지큐에 명령을 전달해서 수행하도록해주는 클래스 - UI 변경
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            progressDialog.dismiss();
            // 숫자데이터는 what arg1 arg2로 전달
            // 그외 데이터는 obj에 전달
            textView.setText(msg.obj.toString());
        }
    };

    // 데이터를 다운로드 받아서 Handler에게 전송하는 클래스
    class ThreadEx extends Thread{
        @Override
        public void run() {
            try{
                String addr = editText.getText().toString();
                // URL객체
                URL url = new URL(addr);
                // 연결객체
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                // 옵션 설정
                conn.setConnectTimeout(30000);
                conn.setUseCaches(false);

                // 인코딩 타입을 확인하기 위한 코드
                String header = conn.getContentType();
                Log.e("헤더", header);

                // 문자열을 읽기 위한 스트림
                // finance.naver.com 은 euc-kr로 인코딩돼있기 때문에 인코딩설정을 해줘야 안깨짐
                // m.naver.com 은 UTF-8로 인코딩돼있기 때문에 인코딩설정을 특별히 하지 않아도 기본이 UTF-8이라서 안깨짐(euc-kr로 하면 깨짐)
                // 헤더에 인코딩타입을 확인해서 해당 인코딩타입으로 설정
                BufferedReader br = null;
                if(header.toUpperCase().indexOf("UTF-8") >= 0){
                    br = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
                }else if(header.toUpperCase().indexOf("ISO-8859-1") >= 0){
                    br = new BufferedReader(new InputStreamReader(conn.getInputStream(),"ISO-8859-1"));
                }else{
                    br = new BufferedReader(new InputStreamReader(conn.getInputStream(),"EUC-KR"));
                }

                // 읽은 문자열을 계속해서 저장하기 위해 StringBuilder 생성
                StringBuilder sb = new StringBuilder();
                // 줄단위로 읽어서 읽은 줄이 있으면 sb에 추가
                while(true) {
                    String line = br.readLine();
                    if(line==null) break;
                    sb.append(line+"\n");
                }
                // 다 읽었으면 연결 해제
                br.close();
                conn.disconnect();
                // 핸들러에게 받은 문자열 전송
                Message msg = new Message();
                msg.obj = sb;
                handler.sendMessage(msg);

            }catch (Exception e){
                Log.e("예외",e.getMessage());
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText)findViewById(R.id.editText);
        textView = (TextView)findViewById(R.id.textView);
        Button btn = (Button)findViewById(R.id.btn);

        btn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {

                progressDialog = ProgressDialog.show(MainActivity.this, "기다리세요.", "다운로드중");

                ThreadEx th = new ThreadEx();
                th.start();
            }
        });

    }
}
