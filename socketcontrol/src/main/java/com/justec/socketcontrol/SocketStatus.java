package com.justec.socketcontrol;

public enum  SocketStatus {
    /**
     * 初始状态
     */
    INIT,
    /**
     * 连接中
     */
    CONNECTING,
    /**
     * 已连接
     */
    CONNECTED,
    /**
     * 已断开
     */
    DISCONNECTED,
    /**
     * 连接错误
     */
    ERROR,
    /**
     * 连接超时
     */
    TIMEOUT,

    OPEN,
    /**
     * 建立连接事件
     */

    CLOSE,
    /**
     * 断开连接事件
     */


}
