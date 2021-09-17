package com.knight.cameraone.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.RectF
import android.hardware.Camera
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.OrientationHelper
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.knight.cameraone.CameraPresenter
import com.knight.cameraone.CircleButtonView
import com.knight.cameraone.Configuration
import com.knight.cameraone.R
import com.knight.cameraone.adapter.PhotosAdapter
import com.knight.cameraone.utils.SystemUtil
import com.knight.cameraone.utils.ToastUtil
import com.knight.cameraone.view.FaceDeteView
import kotlinx.android.synthetic.main.activity_customcamera.*

/**
 *
 * @ProjectName:    Camera1Kotlin
 * @Package:        com.knight.cameraone.activity
 * @ClassName:      CustomCameraActivity
 * @Description:    java类作用描述
 * @Author:         knight
 * @CreateDate:     2019-10-07 13:21
 * @UpdateUser:     更新者
 * @UpdateDate:     2019-10-07 13:21
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */

class CustomCameraActivity:AppCompatActivity(), View.OnClickListener, CameraPresenter.CameraCallBack,View.OnTouchListener,PhotosAdapter.OnItemClickListener{




    override fun onItemClick(v: View, path: String) {
        startActivity(Intent(this,BigPhotoActivity::class.java).putExtra("imagePhoto",path))
    }


    //逻辑层
    var mCameraPresenter:CameraPresenter?=null
    val MODE_INIT:Int = 0
    //两个触摸点触摸屏幕状态
    val MODE_ZOOM:Int = 1
    //标识模式
    var mode:Int = MODE_ZOOM
    //两点的初始距离
    var startDis:Float = 0f
    //适配器
    var mPhotosAdapter:PhotosAdapter? = null
    //图片List
    var photoList:MutableList<String>?=null



    var isMove:Boolean = false

    //闪光灯开关
    var isTurn:Boolean = true
    //开启人脸监测
    var isFaceDetect:Boolean = true

    //人脸检测框
    var faceView:FaceDeteView?=null

    var isFull :Boolean = false


    //传感器方向监听对象 监听屏幕旋转角度
    var orientationEventListener:OrientationEventListener?=null
    //拍照时方向(传感器角度方向 -> 转变而来)
    var takePhotoOrientation:Int = 90

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_customcamera)
        getScreenBrightness()
        initOrientate()
        initListener()
        //初始化CameraPresenter
        mCameraPresenter = CameraPresenter(this,sf_camera)
        //设置后置摄像头
        mCameraPresenter?.setFrontOrBack(Camera.CameraInfo.CAMERA_FACING_BACK)
        //添加监听
        mCameraPresenter?.setCameraCallBack(this)
        //添加人脸检测
        mCameraPresenter?.setFaceView(faceView!!)
        mCameraPresenter?.turnFaceDetect(false)

        photoList = ArrayList<String>()

        mPhotosAdapter = PhotosAdapter(photoList as ArrayList<String>)
        mPhotosAdapter?.setItemClickListener(this)

        var layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = OrientationHelper.VERTICAL
        cy_photo.layoutManager = layoutManager
        cy_photo.adapter = mPhotosAdapter


    }






    override fun onClick(v: View?) {
        when(v?.id){
            R.id.iv_photo -> cy_photo.visibility = if(cy_photo.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            //改变摄像头
            R.id.tv_change_camera -> mCameraPresenter?.switchCamera()
            //关闭还是开启闪光灯
            R.id.tv_flash ->{
                mCameraPresenter?.turnLight(isTurn)
                tv_flash.setBackgroundResource(if(isTurn)R.drawable.icon_turnon else R.drawable.icon_turnoff )
                Configuration.flaseState = isTurn
                isTurn = !isTurn
            }
            //开启人脸检测
            R.id.tv_facedetect -> {
                mCameraPresenter?.turnFaceDetect(isFaceDetect)
                tv_facedetect.setBackgroundResource(if(isFaceDetect)R.drawable.icon_facedetect_on else R.drawable.icon_facedetect_off)
                isFaceDetect = !isFaceDetect
            }
            //切换全屏还是4：3
            R.id.tv_matchorwrap -> {
                cl_parent.removeView(sf_camera)
                var screen : IntArray= getScreent()
                //定义布局参数
                var layoutParams : ConstraintLayout.LayoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT,ConstraintLayout.LayoutParams.WRAP_CONTENT)
                if(isFull){
                    //是全屏 切换成4：3
                    layoutParams.width = screen[0]
                    layoutParams.height = screen[0] / 9 * 16
                    tv_matchorwrap.text = "全屏模式"
                } else {
                    //不是全屏
                    //是全屏 切换成4：3
                    layoutParams.width = screen[0]
                    layoutParams.height = screen[1]
                    tv_matchorwrap.text = "半屏模式"
                }
                sf_camera.layoutParams = layoutParams
                isFull = !isFull
                mCameraPresenter?.setFull(isFull)
                cl_parent.addView(sf_camera,0,layoutParams)
            }
        }
    }

    /**
     * 预览回调
     * @param data 预览数据
     *
     */
    override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {

    }

    /**
     *
     * 拍照回调
     * @param data 拍照数据
     *
     */
    override fun onTakePicture(data: ByteArray?, camera: Camera?) {

    }

    /**
     * 人脸检测回调
     * @param rectFArrayList
     *
     */
    override fun onFaceDetect(rectFArrayList: ArrayList<RectF>?, camera: Camera?) {

    }

    /**
     *
     * 返回拍照后的照片
     * @param imagePath
     *
     */
    override fun getPhotoFile(imagePath: String?) {
        //设置头像
        Glide.with(this).load(imagePath).apply(RequestOptions.bitmapTransform(CircleCrop()).override(iv_photo.width,iv_photo.height).error(R.drawable.default_person_icon))
            .into(iv_photo)
        photoList?.add(imagePath!!)
        mPhotosAdapter?.notifyDataSetChanged()
    }

    override fun getVideoFile(videoFilePath: String) {
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        //无论多少跟手指加进来，都是MotionEvent.ACTION_DWON MotionEvent.ACTION_POINTER_DOWN
        //MotionEvent.ACTION_MOVE:
        when (event.action and MotionEvent.ACTION_MASK) {
            //手指按下屏幕
            MotionEvent.ACTION_DOWN -> mode = MODE_INIT
            //当屏幕上已经有触摸点按下的状态的时候，再有新的触摸点被按下时会触发
            MotionEvent.ACTION_POINTER_DOWN -> {
                mode = MODE_ZOOM
                //计算两个手指的距离 两点的距离
                startDis = SystemUtil.twoPointDistance(event)
            }
            //移动的时候回调
            MotionEvent.ACTION_MOVE -> {
                isMove = true
                //这里主要判断有两个触摸点的时候才触发
                if (mode == MODE_ZOOM) {
                    //只有两个点同时触屏才执行
                    if (event.pointerCount < 2) {
                        return true
                    }
                    //获取结束的距离
                    val endDis = SystemUtil.twoPointDistance(event)
                    //每变化10f zoom变1
                    val scale = ((endDis - startDis) / 10f).toInt()
                    if (scale >= 1 || scale <= -1) {
                        var zoom = mCameraPresenter!!.getZoom() + scale
                        //判断zoom是否超出变焦距离
                        if (zoom > mCameraPresenter!!.getMaxZoom()) {
                            zoom = mCameraPresenter!!.getMaxZoom()
                        }
                        //如果系数小于0
                        if (zoom < 0) {
                            zoom = 0
                        }
                        //设置焦距
                        mCameraPresenter!!.setZoom(zoom)
                        //将最后一次的距离设为当前距离
                        startDis = endDis
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                //判断是否点击屏幕 如果是自动聚焦
                if (isMove == false) {
                    //自动聚焦
                    mCameraPresenter?.autoFocus()
                    isMove = false
                }
            }
        }
        return true
    }


    /**
     * 添加点击事件，触摸事件
     *
     */
    fun initListener(){
        sf_camera.setOnTouchListener(this)
        iv_photo.setOnClickListener(this)
        tv_change_camera.setOnClickListener(this)
        tv_flash.setOnClickListener(this)
        tv_matchorwrap.setOnClickListener(this)
        //点击事件
        tv_takephoto.setOnClickListener(object : CircleButtonView.OnClickListener{
            override fun onClick() {
                //拍照的调用方法
                mCameraPresenter?.takePicture(takePhotoOrientation)
            }
        })

        //长按事件
        tv_takephoto.setOnLongClickListener(object:CircleButtonView.OnLongClickListener{
            override fun onNoMinRecord(currentTime: Int) {
                ToastUtil.showShortToast(this@CustomCameraActivity, "录制时间太短～")
            }

            override fun onRecordFinishedListener() {
                 mCameraPresenter?.stopRecord()
                 startActivity(Intent(this@CustomCameraActivity,PlayAudioActivity::class.java)
                     .putExtra("videoPath",mCameraPresenter?.getVideoFilePath())

                 )
            }

            override fun onLongClick() {
                //视频文件/storage/emulated/0/Android/data/com.knight.cameraone/files/Movies 下
                mCameraPresenter?.startRecord(getExternalFilesDir(Environment.DIRECTORY_MOVIES)?.path!!,"video")
//                mCameraPresenter?.startRecord(Configuration.OUTPATH,"video")
            }
        })

        tv_facedetect.setOnClickListener(this)
        faceView = findViewById(R.id.faceView)
    }


    /**
     * 加入调整亮度
     *
     */
    fun getScreenBrightness(){
        var lp:WindowManager.LayoutParams = window.attributes
        //screenBrightness的值是0.0-1.0 从0到1.0 亮度逐渐增大 如果是-1，那就是跟随系统亮度
        lp.screenBrightness = 200f * (1f/255f)
        window.attributes = lp
    }

    /**
     *
     * Activity 销毁回调方法 释放各种资源
     *
     */
    override fun onDestroy() {
        super.onDestroy()
        mCameraPresenter?.releaseCamera()
        orientationEventListener?.disable()
    }


    /**
     * 获取屏幕宽高
     *
     */
    fun getScreent():IntArray{
      //  var screens = arrayOf<Int>(2)
        val screens = IntArray(2)
        //获取屏幕宽度
        var metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        var width = metrics.widthPixels
        var height = metrics.heightPixels

        //宽
        screens[0] = width
        //高
        screens[1] = height

        return screens

    }


    /**
     *
     * 初始化传感器方向 转为拍照方向
     *
     */
    fun initOrientate(){
         if(orientationEventListener == null){
             orientationEventListener = object:OrientationEventListener(this){
                 override fun onOrientationChanged(i: Int) {
                     // i的范围是0-359
                     // 屏幕左边在顶部的时候 i = 90;
                     // 屏幕顶部在底部的时候 i = 180;
                     // 屏幕右边在底部的时候 i = 270;
                     // 正常的情况默认i = 0;
                     if(i in 45..134){
                         takePhotoOrientation = 180
                     } else if(i in 135..224){
                         takePhotoOrientation = 270
                     } else if(i in 225..314){
                         takePhotoOrientation = 0
                     } else {
                         takePhotoOrientation = 90
                     }
                 }

             }
         }

        orientationEventListener?.enable()
    }
}