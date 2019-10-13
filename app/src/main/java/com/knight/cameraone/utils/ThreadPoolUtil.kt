package com.knight.cameraone.utils

import java.util.concurrent.Executors

/**
 *
 * @ProjectName:    Camera1Kotlin
 * @Package:        com.knight.cameraone.utils
 * @ClassName:      ThreadPoolUtil
 * @Description:    java类作用描述
 * @Author:         knight
 * @CreateDate:     2019-10-07 18:13
 * @UpdateUser:     更新者
 * @UpdateDate:     2019-10-07 18:13
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */

class ThreadPoolUtil {

    companion object{
        var threadPool = Executors.newCachedThreadPool()

        fun execute(runnable: Runnable){
            threadPool.execute(runnable)
        }
    }
}