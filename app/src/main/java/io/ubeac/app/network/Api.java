package io.ubeac.app.network;

import com.blankj.utilcode.util.NetworkUtils;
import com.google.gson.Gson;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class Api {
    private static Api instance;
    private Gson gson = new Gson();
    private Retrofit retrofit;
    private String protocol;
    private String token;

    private Api(String protocol, String token) {
        this.protocol = protocol;
        this.token = token;
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .readTimeout(6, TimeUnit.SECONDS)
                .build();
        retrofit = new Retrofit.Builder()
                .baseUrl(protocol.toLowerCase() + "://hook.ubeac.io/")
             // .client(client)
                .build();
    }

    public static Api getInstance(String protocol, String token) {
        if (instance == null || !instance.protocol.equals(protocol) || !instance.token.equals(token)) {
            instance = new Api(protocol, token);
        }
        return instance;
    }

    public void sendData(HashMap<String, Serializable> data) {
        if (!NetworkUtils.isConnected())
            return;
        String strBody = gson.toJson(data);
   RequestBody body = RequestBody.create(MediaType.parse("application/json"), strBody);
        UbeacService service = retrofit.create(UbeacService.class);
        Call<ResponseBody> call = service.sendData(token, body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
            }

            @Override
            public void onFailure(@NotNull Call<ResponseBody> call, Throwable t) {
            }
        });
    }
}
