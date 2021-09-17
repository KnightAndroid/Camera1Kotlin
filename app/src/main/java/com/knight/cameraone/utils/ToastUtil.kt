package com.knight.cameraone.utils

import android.content.Context
import android.widget.Toast

/**
 *
 * @ProjectName:    Camera1Kotlin
 * @Package:        com.knight.cameraone.utils
 * @ClassName:      ToastUtil
 * @Description:    java类作用描述
 * @Author:         knight
 * @CreateDate:     2019-10-07 14:13
 * @UpdateUser:     更新者
 * @UpdateDate:     2019-10-07 14:13
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */

class ToastUtil {


    companion object{
         var mToast: Toast ?= null

        /**
         * 弹出短提示
         * @param context 上下文
         * @param message 文本提示
         *
         */
        fun showShortToast(context : Context,message:String){
            showToastMessage(context,message,Toast.LENGTH_SHORT)
         }


        /**
         * 弹出Toast提示
         * @param context 上下文
         * @param message 要显示的message
         * @param duration 时间长短
         *
         */
        fun showToastMessage(context:Context,message: String,duration:Int){
            if (mToast == null) {
                mToast = Toast.makeText(context, message, duration)
            }

            mToast?.let{
                //?.表示如果为空就走右边
                mToast?:Toast.makeText(context,message,duration)
                mToast?.setText(message)
                mToast?.duration = duration
                mToast?.show()
            }

        }


    }
}