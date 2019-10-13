package com.knight.cameraone.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.knight.cameraone.R
import kotlinx.android.synthetic.main.activity_bigphoto.*

/**
 *
 * @ProjectName:    Camera1Kotlin
 * @Package:        com.knight.cameraone.activity
 * @ClassName:      BigPhotoActivity
 * @Description:    java类作用描述
 * @Author:         knight
 * @CreateDate:     2019-10-07 13:20
 * @UpdateUser:     更新者
 * @UpdateDate:     2019-10-07 13:20
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */

class BigPhotoActivity : AppCompatActivity() {


    var path:String?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bigphoto)

        path = intent.getStringExtra("imagePhoto")
        Glide.with(this).load(path).apply(RequestOptions.noTransformation()
            .override(iv_photo.width,iv_photo.height))
            .error(R.drawable.default_person_icon)
            .into(iv_photo)
    }
}