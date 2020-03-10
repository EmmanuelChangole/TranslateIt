package com.translateit.translateit.fragments;



import com.translateit.translateit.notifictions.MyResponse;
import com.translateit.translateit.notifictions.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAeRENU1w:APA91bH2RNLRoovMkvwPtTKRcYEPhfWS7ra4SZgWpY419xem5SnNr1wasNAhY9i5xWBLLTz3DLSAK0vUJq84C97XAJJuXOTYsJJWoe9vgzsiCe1rYxD-O_k0zppvio88q_aS85zTh4WS"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
