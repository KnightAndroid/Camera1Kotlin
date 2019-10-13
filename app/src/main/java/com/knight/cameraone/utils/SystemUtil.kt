package com.knight.cameraone.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import java.io.File
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.*

/**
 *
 * @ProjectName:    Camera1Kotlin
 * @Package:        com.knight.cameraone.utils
 * @ClassName:      SystemUtil
 * @Description:    java类作用描述
 * @Author:         knight
 * @CreateDate:     2019-10-06 13:42
 * @UpdateUser:     更新者
 * @UpdateDate:     2019-10-06 13:42
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */

class SystemUtil {

    //静态方法 引入kotlin一个语法糖 陪伴对象(Companion Objects) 通过陪伴对象的方式在kotlin里实现静态方法
    companion object {


        /**
         * 获取包名
         * @param context
         * @return
         *
         */
        fun getPackageName(context: Context): String {

            try {
                var packageManager: PackageManager = context.packageManager
                var packageInfo: PackageInfo = packageManager.getPackageInfo(context.packageName, 0)
                return packageInfo.packageName
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return ""
        }


        /**
         * 格式化时间
         * @param time
         * @return "yyyy-MM--dd HH:mm:ss"
         *
         */
        fun formatTime(time: Long): String {
            var formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            var curDate = Date(time)
            return formatter.format(curDate)
        }


        /**
         * 格式化时间
         * @param time 时间
         * @param file 文件命名
         * @return
         */
        fun formatTime(time: Long, file: String): String {
            var formatter = SimpleDateFormat("yyyyMMddHHmmss")
            var curDate = Date(time)
            return formatter.format(curDate) + file
        }


        /**
         * 格式化三位随机数
         * @param i 传入的数字
         * @return
         */
        fun formatRandom(i: Int): String {
            var s: String = i.toString() + ""
            if (s.length == 1) {
                return "000$s"
            } else if (s.length == 2) {
                return "00$s"
            } else if (s.length == 3) {
                return "0$s"
            } else {
                return s
            }


        }

        /**
         * 将突破保存到手机相册
         * @param path 路径
         * @param name 文件名字
         * @param context 上下文
         *
         */
        fun saveAlbum(path: String, name: String, context: Context) {
            //把文件插入到系统图库
            try {
                MediaStore.Images.Media.insertImage(context.contentResolver, path, name, null)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }

            //通知图库更新
            context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://$path")))
        }

        fun copyPicture(srcPath: String, dstPath: String, name: String) {
            val oldFile = File(srcPath)
            val newFile = File(dstPath + name)
            Log.d("ssd",newFile.name)
            newFile.deleteOnExit()
            newFile.createNewFile()
            var c = -1
            val buffer = ByteArray(1024 * 1000)
            val inputStream = oldFile.inputStream()
            val now = System.currentTimeMillis()
            while ({ c = inputStream.read(buffer);c }() > 0) {
                newFile.appendBytes(buffer.copyOfRange(0, c))
            }
//            Log.d("ssd", "目标$dstPath")
            //源文件流
//            var fileInputStream: FileInputStream? = null
//            //结果流
//            var fileOutputStream: FileOutputStream? = null
//            var dir = File(dstPath)
//            if (!dir.exists() || !dir.isDirectory) {
//                dir.mkdir()
//            }
//            try {
//                fileInputStream = FileInputStream(srcPath)
//                fileOutputStream = FileOutputStream(dstPath + name)
//                var bytes = byteArrayOf(1024.toByte())
//                var by: Int = 0
//
//
//                do {
//                    by = fileInputStream.read(bytes)
//                    fileOutputStream.write(bytes, 0, by)
//                    if(by == -1){
//                        break
//                    }
//                }while (true)
////                while (((fileInputStream.read(bytes)).also { by = it }) != -1) {
////                    Log.d("ssd","进入")
////                    fileOutputStream.write(bytes, 0, by)
////                }
//            } catch (e: FileNotFoundException) {
//                e.printStackTrace()
//            } catch (e: IOException) {
//                e.printStackTrace()
//            } finally {
//                try {
//                    fileInputStream?.close()
//                    fileOutputStream?.close()
//                } catch (e: Exception) {
//                    e.printStackTrace()
//
//                }
//            }

        }


        /**
         * 两点的距离
         * @param event 事件
         *
         */
        fun twoPointDistance(event: MotionEvent?): Float {
            if (event == null) {
                return 0f
            }

            var x = event.getX(0) - event.getX(1)
            var y = event.getY(0) - event.getY(1)

            return Math.sqrt((x * x + y * y).toDouble()).toFloat()


        }


    }


}