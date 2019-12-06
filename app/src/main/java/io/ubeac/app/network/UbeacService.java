package io.ubeac.app.network;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UbeacService {

    @POST(".")
    Call<ResponseBody> sendData(@Body RequestBody body);

}
