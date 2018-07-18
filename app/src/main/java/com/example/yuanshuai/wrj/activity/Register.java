package com.example.yuanshuai.wrj.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.example.yuanshuai.wrj.R;
import com.example.yuanshuai.wrj.model.Output;
import com.example.yuanshuai.wrj.model.UserInfoOutput;
import com.example.yuanshuai.wrj.net.Net;

import dji.thirdparty.rx.android.schedulers.AndroidSchedulers;
import rx.Scheduler;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class Register extends AppCompatActivity {

    private EditText name;
    private EditText password;
    private EditText confirmPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_register);
        init();
    }
    private void init(){
        name=(EditText)findViewById(R.id.name);
        password=(EditText)findViewById(R.id.password);
        confirmPassword=(EditText)findViewById(R.id.confirmPassword);
    }
    private void register(){
        String username=name.getText().toString();
        String pwd=password.getText().toString();
        String cpwd=confirmPassword.getText().toString();
        if("".equals(username)){
            name.setError("用户名为空");
        }
        else if("".equals(pwd)){
            password.setError("密码为空");
        }
        else if("".equals(cpwd)){
            confirmPassword.setError("确认密码不能为空");
        }
        else if(!pwd.equals(cpwd)){
            confirmPassword.setError("密码不一致");
        }
        else{
            Net.getNet().register(username,pwd)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.immediate())
                    .subscribe(new Action1<Output<UserInfoOutput>>() {
                        @Override
                        public void call(Output<UserInfoOutput> userInfoOutputOutput) {
                            if (userInfoOutputOutput.getCode()==0){
                                showSnackBar("注册成功");
                                Net.getNet().setUserInfoOutput(userInfoOutputOutput.getData());

                            }
                            else {
                                showSnackBar("注册失败");
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
        switch (view.getId()){
            case R.id.back:
                finish();
                break;
            case R.id.login:
                Intent intent=new Intent(this,Login.class);
                startActivity(intent);
                finish();
                break;
            case R.id.register:
                register();
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
