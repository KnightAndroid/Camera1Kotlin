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
    //这是app内部存储 格式如下 /data/data/包名/xxx/ 内部存储在Android系统对应的根目录是 /data/data/，这个目录普通用户是无权访问的，用户需要root权限才可以查看
    val insidePath : String = "/data/data/com.knight.cameraone/pic/"


    //这是app外部存储的私有存储目录 沙盒模式(Android 10)
    //context.getExternalFilesDir(String type)
    /**
     * 
     * 1.如果type为""，那么获取到的目录是 /storage/emulated/0/Android/data/package_name/files
     * 2.如果type不为空，则会在/storage/emulated/0/Android/data/package_name/files目录下创建一个以传入的type值为名称的目录，例如你将type设为了test，那么就会创建/storage/emulated/0/Android/data/package_name/files/test目录，这个其实有点类似于内部存储getDir方法传入的name参数。但是android官方推荐使用以下的type类型
     *  public static String DIRECTORY_MUSIC = "Music";
     *  public static String DIRECTORY_PODCASTS = "Podcasts";
     *  public static String DIRECTORY_RINGTONES = "Ringtones";
     *  public static String DIRECTORY_ALARMS = "Alarms";
     *  public static String DIRECTORY_NOTIFICATIONS = "Notifications";
     *  public static String DIRECTORY_PICTURES = "Pictures";
     *  public static String DIRECTORY_MOVIES = "Movies";
     *  public static String DIRECTORY_DOWNLOADS = "Download";
     *  public static String DIRECTORY_DCIM = "DCIM";
     *  public static String DIRECTORY_DOCUMENTS = "Documents";
     *
     *
     */

    //例子: context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) 就会在/storage/emulated/0/Android/data/com.knight.cameraone/files
    //外部路径 Android10以上Environment.getExternalStorageDirectory() 不能用
    //外部路径
    val OUTPATH :String = Environment.getExternalStorageDirectory().toString() + "/拍照-相册/"


    //问题梳理：
    //基本类型-double
    const val testDouble = 3.00
    //基本类型-Float
    const val testFloat = 3.0F
    //基本类型-Long
    const val testLong = 3L
    //基本类型-Int
    const val testInt = 3
    //基本类型-Boolean
    const val testBoolean = true

    //String
    const val testString = "ssss"




    //常量数组
    val testArray:Array<String> = arrayOf("java","kotlin") //爆红const val has type “Array<String>” only primitives and String are allowed
    //原因：
    //const 来修饰它
    //只适用于所有的 基础数据类型 的属性，以及 String类型
    //其他类型的数据比如Array<String>使用时会报 Const ‘val’ has type ‘XXXX’. Only primitives and String are allowed 错误）


}