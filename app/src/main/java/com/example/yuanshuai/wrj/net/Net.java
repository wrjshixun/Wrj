package com.example.yuanshuai.wrj.net;



import com.amap.api.maps.offlinemap.City;
import com.example.yuanshuai.wrj.model.CityZone;
import com.example.yuanshuai.wrj.model.Drone;
import com.example.yuanshuai.wrj.model.ListModel;
import com.example.yuanshuai.wrj.model.Output;
import com.example.yuanshuai.wrj.model.UserInfoOutput;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;

/**
 * Created by 12917 on 2017/7/15.
 */

public class Net {
    private static Net net;
    private String url="http://211.87.235.127:8080/";
    private Retrofit retrofit;
    private UserInfoOutput userInfoOutput=null;
    private OkHttpClient.Builder okHttpClient;
    private NetApi ret;
    private String cookie;
    public static synchronized Net getNet(){
        if(net==null)
            net=new Net();
        return net;
    }
    private Net(){
        GsonBuilder builder = new GsonBuilder();

        // Register an adapter to manage the date types as long values
        builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return new Date(json.getAsJsonPrimitive().getAsLong());
            }
        });

        Gson gson = builder.create();
        okHttpClient=new OkHttpClient.Builder();
        okHttpClient.cookieJar(new CookieJar() {
            private final HashMap<String, List<Cookie>> cookieStore = new HashMap<String, List<Cookie>>();
            @Override
            public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                cookieStore.put(url.host(), cookies);
                cookie=cookies.get(0).toString();
            }

            @Override
            public List<Cookie> loadForRequest(HttpUrl url) {
                List<Cookie> cookies = cookieStore.get(url.host());
                return cookies != null ? cookies : new ArrayList<Cookie>();
            }
        });

        retrofit=new Retrofit.Builder()
                .baseUrl(url)
                .client(okHttpClient.build())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        ret=retrofit.create(NetApi.class);

    }

    public void setUserInfoOutput(UserInfoOutput userInfoOutput) {
        this.userInfoOutput = userInfoOutput;
    }

    public UserInfoOutput getUserInfoOutput() {
        return userInfoOutput;
    }

    //    注册
    public Observable<Output<UserInfoOutput>> register(String username,String passwod){
        return ret.register(username,passwod);
    }
//    登陆
    public Observable<Output<UserInfoOutput>> login(String username,String password){
        return ret.login(username,password,false);
    }
//    注销
    Observable<Output> logout(){
        return ret.logout();
    }

//    修改密码
    Observable<Output> updatePassword(String password){
        return ret.updatePassword(password);
    }

//    获取用户信息
    Observable<Output<UserInfoOutput>> getUserInfo(String id){
        return ret.getUserInfo(id);
    }

//    获取无人机列表
    Observable<Output<ListModel<Drone>>> droneModelList(){
        return ret.droneModelList();
    }
//    获取区域列表
    Observable<Output<ListModel<CityZone>>> cityZoneList(int type,int parentid){
        return ret.cityZoneList(0,10000,type,parentid);
    }

}
