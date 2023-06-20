package com.example.simplesocket.server

/**
 * 服务端回调
 */
interface Callback {
    //接收客户端的消息
    fun resultMsg(ipAddress: String, msg: String)

    //其他消息
    fun otherMsg(msg: String)
}