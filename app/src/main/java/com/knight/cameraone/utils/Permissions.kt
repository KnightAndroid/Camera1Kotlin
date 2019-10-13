package com.knight.cameraone.utils

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

/**
 *
 * @ProjectName:    Camera1Kotlin
 * @Package:        com.knight.cameraone.utils
 * @ClassName:      Permissions
 * @Description:    java类作用描述
 * @Author:         knight
 * @CreateDate:     2019-10-07 13:55
 * @UpdateUser:     更新者
 * @UpdateDate:     2019-10-07 13:55
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */

class Permissions {
     companion object{

         /**
          *
          * 弹出权限提示框
          */
         fun showPermissionSettingDialog(context : Context,permissions:String){
             var msg:String = "本App需要"+permissions+"权限才能正常运行,请点击确定,进入设置界面进行授权处理~"
             var builder = AlertDialog.Builder(context)
             builder.setMessage(msg)
                 .setPositiveButton("确定"
                 ) { dialog, which -> showSettings(context) }
                 .setNegativeButton("取消"){dialog, which ->  }
                 .show()
         }


         /**
          * 如果授权失败，就要进入App权限设置界面
          * @param context 上下文
          *
          */
         fun showSettings(context:Context){
             var intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
             var uri: Uri = Uri.fromParts("package",context.packageName,null)
             intent.data = uri
             intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
             context.startActivity(intent)

         }

     }


}