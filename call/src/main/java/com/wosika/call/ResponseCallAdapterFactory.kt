package com.wosika.call

import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class ResponseCallAdapterFactory : CallAdapter.Factory() {
    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): ResponseCallAdapter? = when (getRawType(returnType)) {
        Call::class.java -> {
            //判断方法返回值
            val callType = getParameterUpperBound(0, returnType as ParameterizedType)
            //只有返回值是NetworkResult的才会使用本Adapter
            when (getRawType(callType)) {
                NetworkResult::class.java -> {
                    val resultType = getParameterUpperBound(0, callType as ParameterizedType)
                    ResponseCallAdapter(resultType)
                }
                else -> null
            }
        }
        else -> null
    }
}

class ResponseCallAdapter constructor(
    private val resultType: Type,
) : CallAdapter<Type, Call<NetworkResult<Type>>> {

    override fun responseType() = resultType

    override fun adapt(call: Call<Type>): Call<NetworkResult<Type>> = ResponseCallDelegate(call)

}