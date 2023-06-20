package com.example.simplesocket.server

import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object SocketServer {
    private val TAG = SocketServer::class.java.simpleName

    const val SOCKET_PORT = 8080

    private var socket: Socket? = null
    private var serverSocket: ServerSocket? = null

    private lateinit var mCallback: Callback

    private lateinit var outputStream: OutputStream

    var result = true

    // 服务端线程池
    private var serverThreadPool: ExecutorService? = null

    /**
     * Start server
     */
    fun startServer(callback: Callback): Boolean {
        mCallback = callback
        Thread {
            try {
                serverSocket = ServerSocket(SOCKET_PORT)
                while (result) {
                    socket = serverSocket?.accept()
                    mCallback.otherMsg("${socket?.inetAddress} to connected")
                    ServerThread(socket!!, mCallback).start()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                result = false
            }
        }.start()
        return result
    }

    /**
     * 关闭服务
     */
    fun stopServer() {
        socket?.apply {
            shutdownInput()
            shutdownOutput()
            close()
        }
        serverSocket?.close()

        //关闭线程池
        serverThreadPool?.shutdownNow()
        serverThreadPool = null
    }

    /**
     * 发送到客户端
     */
    fun sendToClient(msg: String) {
        if (serverThreadPool == null) {
            serverThreadPool = Executors.newCachedThreadPool()
        }
        serverThreadPool?.execute {
            if (socket == null) {
                mCallback.otherMsg("客户端还未连接")
                return@execute
            }
            if (socket!!.isClosed) {
                mCallback.otherMsg("Socket已关闭")
                return@execute
            }
            outputStream = socket!!.getOutputStream()
            try {

                outputStream.write(msg.toByteArray())
                outputStream.flush()
            } catch (e: IOException) {
                e.printStackTrace()
                mCallback.otherMsg("向客户端发送消息: $msg 失败")
            }
        }
    }

    /**
     * 回复心跳消息
     */
    fun replyHeartbeat() {
        if (serverThreadPool == null) {
            serverThreadPool = Executors.newCachedThreadPool()
        }
        val msg = "Copy that for Service"
        serverThreadPool?.execute {
            if (socket == null) {
                mCallback.otherMsg("客户端还未连接")
                return@execute
            }
            if (socket!!.isClosed) {
                mCallback.otherMsg("Socket已关闭")
                return@execute
            }
            outputStream = socket!!.getOutputStream()
            try {
                outputStream.write(msg.toByteArray())
                outputStream.flush()
            } catch (e: IOException) {
                e.printStackTrace()
                mCallback.otherMsg("向客户端发送消息: $msg 失败")
            }
        }
    }

    class ServerThread(private val socket: Socket, private val callback: Callback) :
        Thread() {

        override fun run() {
            val inputStream: InputStream?
            try {
                inputStream = socket.getInputStream()
                val buffer = ByteArray(1024)
                var len: Int
                var receiveStr = ""
                if (inputStream.available() == 0) {
                    Log.e(TAG, "inputStream.available() == 0")
                }
                while (inputStream.read(buffer).also { len = it } != -1) {
                    receiveStr += String(buffer, 0, len, Charsets.UTF_8)
                    if (len < 1024) {
                        socket.inetAddress.hostAddress?.let {
                            if (receiveStr == "Call Service for Custom") {//收到客户端发送的心跳消息
                                //准备回复
                                replyHeartbeat()
                            } else {
                                callback.resultMsg(it, receiveStr)
                            }
                        }
                        receiveStr = ""
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                when (e) {
                    is SocketTimeoutException -> {
                        Log.e(TAG, "连接超时，正在重连")
                    }
                    is NoRouteToHostException -> {
                        Log.e(TAG, "该地址不存在，请检查")
                    }
                    is ConnectException -> {
                        Log.e(TAG, "连接异常或被拒绝，请检查")
                    }
                    is SocketException -> {
                        when (e.message) {
                            "Already connected" -> Log.e(TAG, "连接异常或被拒绝，请检查")
                            "Socket closed" -> Log.e(TAG, "连接已关闭")
                        }
                    }
                }
            }
        }
    }
}