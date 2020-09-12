package oversecured.ovaa.network;

import oversecured.ovaa.objects.LoginData;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface LoginService {
    @POST
    Call<Void> login(@Url String url, @Body LoginData loginData);
}
