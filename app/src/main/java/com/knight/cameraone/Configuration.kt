package com.knight.cameraone

import android.os.Environment

/**
 *
 * @ProjectName:    Camera1Kotlin
 * @Package:        com.knight.cameraone
 * @ClassName:      Configuration
 * @Description:    java类作用描述
 * @Author:         knight
 * @CreateDate:     2019-10-05 14:49
 * @UpdateUser:     更新者
 * @UpdateDate:     2019-10-05 14:49
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */

//Kotlin类不支持静态方法和成员，但Kotlin支持全局函数和变量，因此我们可以直接使用全局函数和变量来代替类中静态方法和静态成员变量。不过还有一个问题没解决，如果要使用类中的这些全局函数和变量，该如何处理呢？
//→这就要用到Kotlin推出的一个有趣的语法糖：Objects。那么Objects能取代静态类成员吗？
//→某种程度上，Objects可以解决由于没有static而造成的麻烦
//静态类
object  Configuration {
    //这是app内部存储 格式如下 /data/data/包名/xxx/
    val insidePath : String = "/data/data/com.knight.cameraone/pic/"
    //外部路径
    val OUTPATH :String = Environment.getExternalStorageDirectory().toString() + "/拍照-相册/"
}