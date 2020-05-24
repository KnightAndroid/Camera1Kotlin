package com.knight.cameraone.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.knight.cameraone.Configuration
import com.knight.cameraone.R
import com.knight.cameraone.utils.Permissions
import com.knight.cameraone.utils.PhotoAlbumUtil
import com.knight.cameraone.utils.SystemUtil
import com.knight.cameraone.utils.ToastUtil
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity(), View.OnClickListener {


    var test1:Int? = null
    var test2:Int? = 0

    //拍照照片的路径
    private var cameraSavePath: File?=null
    //调用系统拍照返回的uri
    private var uri:Uri?=null
    //权限
    private var needPermissions : Array<String> = arrayOf(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO)
    //类型加?表示可为空
    override fun onClick(v: View?) {
        //在做一次非空判断 保证多线程安全
        when(v?.id){
           //跳转系统相机
           R.id.btn_system_camera -> goSystemCamera()
           //跳转系统相册
           R.id.btn_system_photo -> goSystemPhoto()
           //自定义相机界面
           R.id.btn_custom_camera -> startActivity(Intent(this,CustomCameraActivity::class.java))
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initListener()
        checkNeedPermissions()

    }





    fun initListener(){
        btn_system_camera.setOnClickListener(this)
        btn_system_photo.setOnClickListener(this)
        btn_custom_camera.setOnClickListener(this)
    }

    /**
     * 检查权限
     *
     *
     */
    fun checkNeedPermissions(){
        //6.0以上需要动态申请权限 动态权限校验 Android 6.0 的 oppo & vivo 手机时，始终返回 权限已被允许 但是当真正用到该权限时，却又弹出权限申请框。
        when (Build.VERSION.SDK_INT >= 23){
            true -> when(ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) !== PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) !== PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) !== PackageManager.PERMISSION_GRANTED){
               //多个权限一起申请
                true -> ActivityCompat.requestPermissions(this,needPermissions,1)
            }

        }
    }

    /**
     * 动态处理申请权限的结果
     * 用户点击同意或者拒绝后触发
     * @param requestCode 请求码
     * @param permissions 权限
     * @param grantResults 结果码
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            1 -> when(grantResults.size > 1){
                true -> when(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    true -> when(grantResults[1] == PackageManager.PERMISSION_GRANTED){
                        true -> when(grantResults[2] == PackageManager.PERMISSION_GRANTED){

                        }
                        false -> Permissions.showPermissionSettingDialog(this,needPermissions[1])
                    }
                    false -> Permissions.showPermissionSettingDialog(this,needPermissions[0])
                }
                false -> ToastUtil.showShortToast(this,"请重新尝试~")
            }

        }
    }

    /**
     * 调用系统相机
     *
     */
    fun goSystemCamera(){
       //在根目录创建jpg文件
       cameraSavePath = File(Environment.getExternalStorageDirectory().path + "/" + System.currentTimeMillis() + "jpg")
       //指定跳到系统拍照
       var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
       //适配Android 7.0以上版本应用私有目录限制被访问
       if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
           uri = FileProvider.getUriForFile(this,SystemUtil.getPackageName(applicationContext) + ".fileprovider", cameraSavePath!!)
           intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
       }else{
           //7.0以下
           uri = Uri.fromFile(cameraSavePath)
       }
       //指定ACTION为MediaStore.EXTRA_OUTPUT
       intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT,uri)
       //请求码赋值为1
       startActivityForResult(intent,1)

    }

    /**
     *
     * 跳转到系统相册
     *
     */
    fun goSystemPhoto(){
        var intent = Intent()
        //设置Intent.ACTION_PICK
        intent.action = Intent.ACTION_PICK
        //设置Type
        intent.type = "image/*"
        startActivityForResult(intent,2)



        var test = Configuration.testArray[0]
    }



    override fun onActivityResult(requestCode: Int,resultCode:Int,data:Intent?){
        var photoPath : String?
        //处理拍照后返回的图片路径
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                photoPath = cameraSavePath.toString()
            }else{
                photoPath = uri?.encodedPath
            }
            Log.d("拍照返回的图片的路径:",photoPath)
            Glide.with(this).load(photoPath).apply(RequestOptions.noTransformation()
                .override(iv_photo.width,iv_photo.height))
                .error(R.drawable.default_person_icon)
                .into(iv_photo)
        }else if(requestCode == 2 && resultCode == Activity.RESULT_OK){
            //处理调用相册返回的路径
            photoPath = PhotoAlbumUtil.getRealPathFromUri(this, data?.data!!)
            Log.d("sssd",photoPath)
            Glide.with(this).load(photoPath).apply(RequestOptions.noTransformation()
                .override(iv_photo.width,iv_photo.height))
                .error(R.drawable.default_person_icon)
                .into(iv_photo)
        }


        super.onActivityResult(requestCode,resultCode,data)

    }


}
