package com.knight.cameraone.utils

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.*


/**
 * @author created by luguian
 * @organize 车童网
 * @Date 2020/9/4 16:09
 * @descript:照片处理
 */
object ImageUtil {

    fun saveAlbum(context: Context,targetFile: File){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            val contentValues = ContentValues()
            //设置文件名
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME,targetFile.name)
            //设置文件类型
            contentValues.put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg")
            //方式1 会在Pictures / Camera 文件夹下生成图片
            //contentValues.put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/Camera")
            //方式2 直接在Pictures生成
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH,Environment.DIRECTORY_PICTURES)
            var insertUri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            var inputStream : BufferedInputStream
            var outputStream : OutputStream?=null
            try {
                inputStream = BufferedInputStream(FileInputStream(targetFile))
                if (insertUri != null) {
                    outputStream = context.getContentResolver().openOutputStream(insertUri)
                }
                if (outputStream != null) {
                    val buffer = ByteArray(1024 * 4)
                    var len: Int
                    while (((inputStream.read(buffer)).also { len = it }) != -1) {
                        outputStream.write(buffer, 0, len)
                    }
                    outputStream.close()
                    inputStream.close()
                }
            } catch (e:IOException){
                return
            }
        } else {
            SystemUtil.saveAlbum(targetFile.absolutePath, targetFile.name, context)
        }


    }



}