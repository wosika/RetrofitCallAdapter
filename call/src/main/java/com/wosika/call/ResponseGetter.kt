package com.wosika.call

import okhttp3.Headers

interface ResponseGetter {
    //http状态码
    val code: Int
    //响应头
    val headers: Headers
    //请求Url（部分场景用于判断）
    val url: String
}
