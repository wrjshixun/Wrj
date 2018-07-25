package com.example.yuanshuai.wrj.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.yuanshuai.wrj.R;
import com.example.yuanshuai.wrj.model.Output;
import com.example.yuanshuai.wrj.model.UserInfoOutput;
import com.example.yuanshuai.wrj.net.Net;

import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class Login extends AppCompatActivity {
    private EditText username;
    private EditText password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        init();
    }
    private void init(){
        username=(EditText)findViewById(R.id.name);
        password=(EditText)findViewById(R.id.password);

    }
    private void login(){
        String name=username.getText().toString();
        String pwd=password.getText().toString();
        if ("".equals(name)){
            username.setError("用户名不能为空");
        }
        else if("".equals(pwd)){
            password.setError("密码不能为空");
        }
        else{
            Net.getNet().login(name,pwd)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.immediate())
                    .subscribe(new Action1<Output<UserInfoOutput>>() {
                        @Override
                        public void call(Output<UserInfoOutput> userInfoOutputOutput) {
                            if(userInfoOutputOutput.getCode()==0){
                                Net.getNet().setUserInfoOutput(userInfoOutputOutput.getData());
                                showSnackBar("登陆成功");
                            }
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            showSnackBar(throwable.getMessage());
                        }
                    });
        }

    }
    public void click(View view){
        int i=view.getId();
        switch (i){
            case R.id.back:
                finish();
                break;
            case R.id.register:
                Intent intent=new Intent(this,Register.class);
                startActivity(intent);
                finish();
                break;
            case R.id.login:
                login();
                break;
            case R.id.forgetpassword:
                break;
            default:
                break;
        }
    }
    public void showSnackBar(String message){
        final Snackbar snackbar=Snackbar.make(getWindow().getDecorView(),message,Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("知道了",new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

}
