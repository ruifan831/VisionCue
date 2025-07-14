package com.zrgenesiscloud.visioncue.network

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Add authentication headers here if needed
        val newRequest = originalRequest.newBuilder()
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            // Add your authentication token here if needed
            // .addHeader("Authorization", "Bearer $token")
            .build()
        
        return chain.proceed(newRequest)
    }
} 