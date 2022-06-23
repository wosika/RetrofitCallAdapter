package com.wosika.call

import com.wosika.call.NetworkResult
import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

internal class ResponseCallDelegate<T>(private val proxyCall: Call<T>) :
    Call<NetworkResult<T>> {

    override fun enqueue(callback: Callback<NetworkResult<T>>) =


        proxyCall.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                //得到响应，将被代理的Call的响应包装成NetworkResult后返回给代理者
                callback.onResponse(
                    this@ResponseCallDelegate,
                    Response.success(
                        //将Retrofit中的Response转成NetworkResult
                        response.toNetworkResult()
                    )
                )
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                //失败了，将被代理的Call的异常包装成NetworkResult后返回给代理者
                callback.onResponse(
                    this@ResponseCallDelegate,
                    //将Exception转成NetworkResult
                    Response.success(t.toExceptionResult())
                )
            }

        })

    //下面都是不变的代理方法
    override fun isExecuted(): Boolean = proxyCall.isExecuted

    override fun cancel() = proxyCall.cancel()

    override fun isCanceled(): Boolean = proxyCall.isCanceled

    override fun request(): Request = proxyCall.request()

    override fun timeout(): Timeout = proxyCall.timeout()

    override fun clone(): Call<NetworkResult<T>> = ResponseCallDelegate(proxyCall.clone())

    override fun execute(): Response<NetworkResult<T>> = throw NotImplementedError()
}

//相关的扩展方法如下：
//将Retrofit的Response转成Success
fun <T> Response<T>.toSuccessResult(): NetworkResult.Success<T> {
    return NetworkResult.Success(this)
}

//将Retrofit的Response转成ServerError
fun <T> Response<T>.toServerErrorResult(): NetworkResult.Failure.ServerError<T> {
    return NetworkResult.Failure.ServerError(this)
}

//将Exception转成NetworkResult.Failure.Exception
fun <T> Throwable.toExceptionResult(): NetworkResult.Failure.Exception<T> {
    return NetworkResult.Failure.Exception(this)
}

//将一个Retrofit的Response转成NetworkResult
fun <T> Response<T>.toNetworkResult(): NetworkResult<T> = (try {
    //http code为成功，即200-300
    if (isSuccessful) {
        toSuccessResult()
    } else if (this.body() == null) {
        //自定义的异常
        Exception("response body is null").toExceptionResult()
    }
    //http code为失败
    else {
        toServerErrorResult()
    }
} catch (t: Throwable) {
    t.toExceptionResult()
})
