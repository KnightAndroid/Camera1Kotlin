package com.knight.cameraone.utils

import com.knight.cameraone.Configuration


/**
 * @author created by luguian
 * @organize 车童网
 * @Date 2021/9/17 10:01
 * @descript:
 */
object CameraUtils {


    /**
     *
     * 判断闪光灯状态
     */
    fun flashState():Boolean {
        return Configuration.flaseState
    }
}