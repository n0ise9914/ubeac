package io.ubeac.app.network;

import android.webkit.URLUtil;
import com.blankj.utilcode.util.NetworkUtils;
import com.google.gson.Gson;
import io.ubeac.app.network.models.Packet;
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

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Api {
    private static Api instance;
    private Gson gson = new Gson();
    private Retrofit retrofit;
    private String url;

    private Api(String url) {
        this.url = url;
        if (!URLUtil.isValidUrl(url))
            return;
        if (!url.endsWith("/"))
            url += "/";
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .readTimeout(6, TimeUnit.SECONDS)
                .build();
        retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .client(client)
                .build();
    }

    public static Api getInstance(String url) {
        if (instance == null || !Objects.equals(instance.url, url)) {
            instance = new Api(url);
        }
        return instance;
    }

    public void sendData(Packet data) {
        if (!NetworkUtils.isConnected())
            return;
        String strBody = "[" + gson.toJson(data) + "]";
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), strBody);
        UbeacService service = retrofit.create(UbeacService.class);
        Call<ResponseBody> call = service.sendData(body);
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
