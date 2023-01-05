package com.app.aiosbusroute.retrofit;

import com.app.aiosbusroute.LoginModel;
import com.app.aiosbusroute.common.SuccessModel;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Query;

public interface ApiService {

    @GET("BusInsertUpdate")
    Call<LoginModel> BusInsertUpdate(@HeaderMap Map<String, String> headers,
                                     @Query("passkey") String passkey,
                                     @Query("route") Integer route,
                                     @Query("busnumber") String busnumber);


    @GET("BusInserLocation")
    Call<SuccessModel> BusInserLocation(@HeaderMap Map<String, String> headers,
                                        @Query("id") Integer id,
                                        @Query("lat") String lat,
                                        @Query("longi") String longi);

}
