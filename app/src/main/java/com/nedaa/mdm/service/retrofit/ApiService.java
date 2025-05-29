package com.nedaa.mdm.service.retrofit;


import com.nedaa.mdm.model.DeviceRegisterRequest;
import com.nedaa.mdm.model.DeviceRegisterResponse;
import com.nedaa.mdm.model.DeviceStatusResponse;

import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    @POST("register")
    Single<DeviceRegisterResponse> registerDevice(@Body DeviceRegisterRequest request);

    @GET("status/{deviceId}")
    Single<DeviceStatusResponse> getDeviceStatus(@Path("deviceId") String deviceId);

}
