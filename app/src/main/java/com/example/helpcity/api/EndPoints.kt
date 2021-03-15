package com.example.helpcity.api

import retrofit2.Call
import retrofit2.http.*

interface EndPoints {
    @GET("/users/")
    fun getUsers(): Call<List<User>>

    @GET("/users/{id}")
    fun getUserById(@Path("id") id: Int): Call<User>

    @FormUrlEncoded
    @POST("login")
    fun userLogin(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<User>
}