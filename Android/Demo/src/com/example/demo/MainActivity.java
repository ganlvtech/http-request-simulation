package com.example.demo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ScrollView view = new ScrollView(this);
        final LinearLayout container = new LinearLayout(this);
        final TextView txtUsername = new TextView(this);
        final EditText edtUsername = new EditText(this);
        final TextView txtPassword = new TextView(this);
        final EditText edtPassword = new EditText(this);
        final Button btnLogin = new Button(this);
        final TextView txtOutput = new TextView(this);

        txtUsername.setText("NetID");
        txtPassword.setText("密码");
        btnLogin.setText("开始");
        edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        container.setOrientation(LinearLayout.VERTICAL);

        container.addView(txtUsername);
        container.addView(edtUsername);
        container.addView(txtPassword);
        container.addView(edtPassword);
        container.addView(btnLogin);
        container.addView(txtOutput);
        view.addView(container);
        setContentView(view);

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Bundle data = msg.getData();
                String str = data.getString("output");
                txtOutput.append(str);
                txtOutput.append("\n");
            }
        };

        btnLogin.setOnClickListener(new View.OnClickListener() {
            private void output(String str) {
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putString("output", str);
                msg.setData(data);
                handler.sendMessage(msg);
            }

            @Override
            public void onClick(View v) {
                txtOutput.setText("");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String username = edtUsername.getText().toString();
                        String password = edtPassword.getText().toString();
                        Demo demo = new Demo();
                        try {
                            String loginUrl = demo.loginCas(username, password);
                            output("登录CAS成功");
                            String hello = demo.loginSsfw(loginUrl);
                            output("登录师生服务系统成功");
                            output(hello);
                            List<String[]> courses = demo.listCourse();
                            output("读取课程列表成功");
                            for (String[] course : courses) {
                                try {
                                    output("==============================");
                                    output(course[0]);
                                    if (!course[2].equals("评教")) {
                                        continue;
                                    }
                                    demo.evalCourse(course[1]);
                                    output("评教成功");
                                } catch (Exception e) {
                                    output(e.getMessage());
                                }
                            }
                        } catch (Exception e) {
                            output(e.getMessage());
                        }
                    }
                }).start();
            }
        });
    }
}
