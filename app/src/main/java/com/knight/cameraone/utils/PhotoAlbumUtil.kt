package com.knight.cameraone.utils

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore

/**
 *
 * @ProjectName:    Camera1Kotlin
 * @Package:        com.knight.cameraone.utils
 * @ClassName:      PhotoAlbumUtil
 * @Description:    java类作用描述
 * @Author:         knight
 * @CreateDate:     2019-10-07 11:51
 * @UpdateUser:     更新者
 * @UpdateDate:     2019-10-07 11:51
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */

class PhotoAlbumUtil {
     companion object{


         /**
          * 返回照片路径
          *
          */
         fun getRealPathFromUri(context : Context, uri: Uri):String? {

             return when (Build.VERSION.SDK_INT >= 19) {
                 true -> getRealPathFromUriUpAPI19(context, uri)
                 false -> getRealPathFromUriDown19(context, uri)
             }
         }


         @SuppressLint("NewApi")
         fun getRealPathFromUriUpAPI19(context:Context,uri:Uri):String?{
             var filePath :String? = null
             //如果是doucment类型的uri，则通过document id来进行处理
             if(DocumentsContract.isDocumentUri(context,uri)){
                var documentId:String = DocumentsContract.getDocumentId(uri)
                if(isMediaDocument(uri)){
                    //使用":"分割
                    var id :String = documentId.split(":")[1]
                    var selection:String = MediaStore.Images.Media._ID + "=?"
                    var selectionArgs :Array<String> = arrayOf(id)
                    filePath = getDataColumn(context,MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection,selectionArgs)
                }else if(isDownloadsDocument(uri)){
                    var contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),documentId.toLong())
                    filePath = getDataColumn(context,contentUri,null,null)
                }
             }else if("content".equals(uri.scheme,true)){
               //如果是content 类型的Uri
               filePath = getDataColumn(context,uri,null,null)
             } else if("file".equals(uri.scheme)){
               //如果是file类型的Uri,直接获取图片对应的路径
               filePath = uri.path
             }
             return filePath
         }


         /**
          * 版本19以下，根据Uri来获取图片的绝对路径
          * @param context 上下文
          * @param uri 图片的uri
          * @return 返回图片的绝对路径
          */
         fun getRealPathFromUriDown19(context :Context,uri:Uri):String?{
             return getDataColumn(context,uri,null,null)
         }


         /**
          * 判断Uri是否是Media类型的
          * @param Uri
          * @return
          */
         fun isMediaDocument(uri :Uri) : Boolean{
             return "com.android.provides.media.documents".equals(uri.authority)
         }


         /**
          * 判断Uri是否是download类型
          * @param uri
          * @return
          */
         fun isDownloadsDocument(uri:Uri) : Boolean{
             return "com.android.providers.downloads.documents".equals(uri.authority)
         }


         /**
          * 根据数据库表中的_data列，返回uri对应的文件路径
          * @param context 上下文
          * @param uri 图片的Uri
          * @param selection 要返回哪些行的筛选器
          * @param selectionArgs 替换selection中的?
          *
          */
         fun getDataColumn(context : Context,uri:Uri,selection:String?,selectionArgs:Array<String>?) : String? {
             var path :String?=null
             var projection : Array<String> = arrayOf(MediaStore.Images.Media.DATA)
             var cursor : Cursor?=null

             try {
                 cursor = context.contentResolver.query(uri,projection,selection,selectionArgs,null)
                 if(cursor.moveToFirst()){
                     var columnIndex : Int = cursor.getColumnIndexOrThrow(projection[0])
                     path = cursor.getString(columnIndex)
                 }
             }catch (e : Exception){
                 cursor?.close()
             }

             return path
         }



     }



}