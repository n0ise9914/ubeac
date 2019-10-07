package io.ubeac.app.network;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface UbeacService {

    @POST("{token}/")
    Call<ResponseBody> sendData(@Path("token") String token, @Body RequestBody body);

}
