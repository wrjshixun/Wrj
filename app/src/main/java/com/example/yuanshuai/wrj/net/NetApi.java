package com.example.yuanshuai.wrj.net;



import com.example.yuanshuai.wrj.model.Output;
import com.example.yuanshuai.wrj.model.UserInfoOutput;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by 12917 on 2017/7/18.
 */

public interface NetApi {
    String commonurl="api/account/";
//    注册
    @POST(commonurl+"register")
    Observable<Output<UserInfoOutput>> register(@Query("username") String username,@Query("password") String password);

//    登陆
    @POST("login.html")
    Observable<Output<UserInfoOutput>> login(@Query("username") String username,@Query("password") String password,@Query("remember-me") boolean b);

//    注销
    @POST(commonurl+"logout")
    Observable<Output> logout();

//    修改密码
    @POST(commonurl+"updatePassword")
    Observable<Output> updatePassword(@Query("password") String password);

//    获取用户信息
    @GET(commonurl+"getUserInfo")
    Observable<Output<UserInfoOutput>> getUserInfo(@Query("id")String id);





}
