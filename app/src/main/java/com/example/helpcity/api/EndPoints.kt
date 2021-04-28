package com.example.helpcity.api

import retrofit2.Call
import retrofit2.http.*

interface EndPoints {

    @GET("occurrences")
    fun getOccurrences(): Call<List<Occurrence>>

    @GET("occurrences-user/{user_id}")
    fun getUserOccurrences(@Path("user_id") userId: String): Call<List<Occurrence>>

    @POST("occurrence/delete-all/user/{user_id}")
    fun deleteAllUserOccurrences(@Path("user_id") userId: String): Call<ServerResponse>

    @POST("occurrence-delete/{id}")
    fun deleteOccurrenceById(@Path("id") occurrenceId: String): Call<ServerResponse>

    @GET("users")
    fun getUsers(): Call<List<User>>

    @GET("occurrence/{id}")
    fun getOccurrenceById(@Path("id") id: Int): Call<Occurrence>

    @GET("users/{id}")
    fun getUserById(@Path("id") id: Int): Call<User>

    @FormUrlEncoded
    @POST("occurrence-new")
    fun newOccurrence(
        @Field("type") type: String,
        @Field("description") description: String,
        @Field("image") image: String,
        @Field("lat") lat: String,
        @Field("lng") lng: String,
        @Field("user_id") user_id: Int
    ): Call<ServerResponse>

    @FormUrlEncoded
    @POST("occurrence-update")
    fun updateOccurrence(
        @Field("id") id: String,
        @Field("type") type: String,
        @Field("description") description: String,
        @Field("image") image: String,
        @Field("lat") lat: String,
        @Field("lng") lng: String,
        @Field("user_id") user_id: Int
        ): Call<ServerResponse>

    @FormUrlEncoded
    @POST("login")
    fun userLogin(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<User>
}