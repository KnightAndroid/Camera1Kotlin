package com.knight.cameraone

import android.annotation.SuppressLint
import android.graphics.*
import android.hardware.Camera
import android.media.MediaRecorder
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.util.DisplayMetrics
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.knight.cameraone.utils.ImageUtil
import com.knight.cameraone.utils.SystemUtil
import com.knight.cameraone.utils.ThreadPoolUtil
import com.knight.cameraone.utils.ToastUtil
import com.knight.cameraone.view.FaceDeteView
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

/**
 *
 * @ProjectName:    Camera1Kotlin
 * @Package:        com.knight.cameraone
 * @ClassName:      CameraPresenter
 * @Description:    java类作用描述
 * @Author:         knight
 * @CreateDate:     2019-10-07 18:17
 * @UpdateUser:     更新者
 * @UpdateDate:     2019-10-07 18:17
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */

class CameraPresenter(mAppCompatActivity: AppCompatActivity, mSurfaceView: SurfaceView) : Camera.PreviewCallback {


    //相机对象
    private var mCamera: Camera? = null
    //相机对象参数设置
    private var mParameters: Camera.Parameters? = null
    //自定义相机页面
    private var mAppCompatActivity: AppCompatActivity = mAppCompatActivity
    //SurfaceView 用于预览对象
    private var mSurfaceView: SurfaceView = mSurfaceView
    //SurfaceHolder对象
    private var mSurfaceHolder: SurfaceHolder
    //摄像头Id 默认后置 0，前置的值是1
    private var mCameraId: Int = Camera.CameraInfo.CAMERA_FACING_BACK
    //预览旋转的角度
    private var orientation: Int = 0

    //自定义回调
    private var mCameraCallBack: CameraCallBack? = null
    //手机的像素宽和高
    private var screenWidth: Int
    private var screenHeight: Int
    //拍照数量
    private var photoNum: Int = 0
    //拍照存放的文件
    private var photosFile: File? = null
    //当前缩放具体值
    private var mZoom: Int = 0
    //视频录制
    private var mediaRecorder: MediaRecorder? = null
    //录制视频的videoSize
    private var height: Int = 0
    private var width: Int = 0

    //检测头像的FaceView
    private var mFaceView:FaceDeteView?= null

    private var isFull = false


    fun setFull(full : Boolean){
        isFull = full

    }

    //自定义回调
    interface CameraCallBack {
        //预览帧回调
        fun onPreviewFrame(data: ByteArray?, camera: Camera?)

        //拍照回调
        fun onTakePicture(data: ByteArray?, camera: Camera?)

        //人脸检测回调
        fun onFaceDetect(rectFArrayList: ArrayList<RectF>?, camera: Camera?)

        //拍照路径返回
        fun getPhotoFile(imagePath: String?)
    }

    fun setCameraCallBack(mCameraCallBack: CameraCallBack) {
        this.mCameraCallBack = mCameraCallBack
    }


    override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
        mCameraCallBack?.onPreviewFrame(data, camera)
    }


    var mHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            when (msg?.what) {
                1 -> mCameraCallBack?.getPhotoFile(msg?.obj.toString())

            }
        }
    }


    init {
        mSurfaceView.holder.setKeepScreenOn(true)
        mSurfaceHolder = mSurfaceView.holder
        var dm: DisplayMetrics = DisplayMetrics()
        mAppCompatActivity.windowManager.defaultDisplay.getMetrics(dm)
        //获取宽高像素
        screenHeight = dm.heightPixels
        screenWidth = dm.widthPixels
        setUpFile()
        init()
    }


    /**
     * 创建拍照文件夹
     *
     */
    fun setUpFile() {
        //方式1 这里是app的内部存储 这里要注意 不是外部私有目录 详情请看 Configuration这个类
        photosFile = File(Configuration.insidePath)
        //方式2 这里改为app的外部存储私有存储目录(虽然是外部存储 但只能是本app自己存储) /storage/emulated/0/Android/data/com.knight.cameraone/Pictures
        photosFile =  mAppCompatActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (!photosFile!!.exists() || !photosFile!!.isDirectory) {
            var isSuccess: Boolean? = false

            try {
                isSuccess = photosFile?.mkdirs()
            } catch (e: Exception) {
                ToastUtil.showShortToast(mAppCompatActivity, "创建存放目录失败,请检查磁盘空间")
            } finally {
                when (isSuccess) {
                    false -> {
                        ToastUtil.showShortToast(mAppCompatActivity, "创建存放目录失败,请检查磁盘空间")
                        mAppCompatActivity.finish()
                    }

                }
            }

        }

    }


    /**
     * 初始化回调
     *
     */
    fun init() {
        mSurfaceHolder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
                //surface绘制是执行
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                openCamera(mCameraId)
                //并设置预览
                startPreview()
                //新增获取系统支持视频尺寸
                getVideoSize()
                mediaRecorder = MediaRecorder()

            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                releaseCamera()
            }
        })
    }


    /**
     * 拍照
     *
     */
    fun takePicture(takePhotoOrientation:Int) {
        mCamera?.let {
            //拍照回调 点击拍照时回调
            it.takePicture(object : Camera.ShutterCallback {
                override fun onShutter() {

                }
            }, object : Camera.PictureCallback {
                //回调没压缩的原始数据
                override fun onPictureTaken(data: ByteArray?, camera: Camera?) {

                }
            }, object : Camera.PictureCallback {
                //回调图片数据 点击拍照后相机返回的照片byte数组，照片数据
                override fun onPictureTaken(data: ByteArray?, camera: Camera?) {
                    //拍照后记得调用预览方法，不然会停在拍照图像的界面
                    mCamera?.startPreview()
                    //回调
                    mCameraCallBack?.onTakePicture(data, camera)
                    //保存图片
                    if (data != null) {
                        getPhotoPath(data,takePhotoOrientation)
                    }
                }
            })

        }
    }

    /**
     *
     * 设置预览
     */
    fun startPreview() {
        try {
            //根据所传入的SurfaceHolder对象来设置实时预览
            mCamera?.setPreviewDisplay(mSurfaceHolder)
            //调整预览角度
            setCameraDisplayOrientation(mAppCompatActivity, mCameraId, mCamera)
            mCamera?.startPreview()
            //开启人脸检测
            startFaceDetect()

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    /**
     *
     * 开始人脸检测
     */
    fun startFaceDetect() {
        //开始人脸识别，这个要调用startPreview之后调用
        mCamera?.startFaceDetection()
        //添加回调
        mCamera?.setFaceDetectionListener(object : Camera.FaceDetectionListener {
            override fun onFaceDetection(faces: Array<out Camera.Face>?, camera: Camera?) {
                //  mCameraCallBack?.onFaceDetect(transForm(faces as Array<Camera.Face>), camera)
                Log.d("sssd", "检测到" + faces?.size + "人脸")
                mFaceView?.setFace(transForm((faces as Array<Camera.Face>)))
                for(index in 0 until faces!!.size){
                    Log.d("""第${index + 1}张人脸""","分数"+faces[index].score + "左眼"+faces[index].leftEye+"右眼"+faces[index].rightEye+"嘴巴"+faces[index].mouth)
                }

            }
        })
    }

    //将相机中用于表示人脸矩形的坐标转换成UI页面的坐标
    fun transForm(faces: Array<Camera.Face>): ArrayList<RectF> {
        val matrix = Matrix()
        //前置摄像机需要镜面翻转
        val mirror = (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT)
        matrix.setScale(if (mirror) -1f else 1f, 1f)
        //设置camera的setDisplayOrientation值
        matrix.postRotate(orientation.toFloat())
        //camera driver坐标范围从（-1000，-1000）到（1000，1000）。
        //ui坐标范围从（0，0）到（宽度，高度）
        matrix.postScale(mSurfaceView.width / 2000f, mSurfaceView.height / 2000f)
        matrix.postTranslate(mSurfaceView.width / 2f, mSurfaceView.height / 2f)

        val rectList = ArrayList<RectF>()
        for (face in faces) {
            val srcRect = RectF(face.rect)
            val dstRect = RectF(0f, 0f, 0f, 0f)
            matrix.mapRect(dstRect, srcRect)
            rectList.add(dstRect)
        }
        return rectList
    }


    /**
     *
     * 设置前置还是后置
     *
     */
    fun setFrontOrBack(mCameraId:Int){
        this.mCameraId = mCameraId

    }

    /**
     *
     * 人脸检测设置检测的View 矩形框
     *
     */
    fun setFaceView(mFaceView:FaceDeteView){
         this.mFaceView = mFaceView
    }


    /**
     *
     * 前后摄像切换
     *
     */
    fun switchCamera(){
       //先释放资源
       releaseCamera()
       //在Android P之前 Android设备仍然最多只有前后两个摄像头，在Android p后支持多个摄像头 用户想打开哪个就打开哪个
       mCameraId = (mCameraId + 1) % Camera.getNumberOfCameras()
        Log.d("ssd",mCameraId.toString())
       //打开摄像头
       openCamera(mCameraId)
       //切换摄像头之后开启预览
       startPreview()
    }

    /**
     * 闪光灯
     * @param turnSwitch true 为开启 false 为关闭
     *
     */
    fun turnLight(turnSwitch:Boolean){
        var parameters = mCamera?.parameters
        parameters?.flashMode = if(turnSwitch) Camera.Parameters.FLASH_MODE_TORCH else Camera.Parameters.FLASH_MODE_OFF
        mCamera?.parameters = parameters
    }

    /**
     *
     * 开启人脸检测
     *
     */
    fun turnFaceDetect(isDetect:Boolean){
         mFaceView?.visibility = if(isDetect) View.VISIBLE else View.GONE
    }


    /**
     * 打开相机，并且判断是否支持该摄像头
     *
     * @param FaceOrBack 前置还是后置
     * @return
     *
     */
    fun openCamera(FaceOrBack: Int): Boolean {
        //是否支持前后摄像头
        var isSupportCamera: Boolean = isSupport(FaceOrBack)
        //如果支持
        if (isSupportCamera) {
            try {
                mCamera = Camera.open(FaceOrBack)
                initParameters(mCamera)
                //设置预览回调
                mCamera?.setPreviewCallback(this)

            } catch (e: Exception) {
                e.printStackTrace()
                ToastUtil.showShortToast(mAppCompatActivity, "打开相机失败~")
                return false
            }
        }
        return isSupportCamera

    }

    /**
     * 判断是否支持某个相机
     * @param faceOrBack 前置还是后置
     * @return
     *
     */
    fun isSupport(faceOrBack: Int): Boolean {
        var cameraInfo: Camera.CameraInfo = Camera.CameraInfo()
        for (index in 0 until Camera.getNumberOfCameras()) {
            //返回相机信息
            Camera.getCameraInfo(index, cameraInfo)
            if (cameraInfo.facing == faceOrBack) {
                return true
            }
        }
        return false
    }


    /**
     * 判断是否支持对焦模式
     * @return
     *
     */
    fun isSupportFocus(focusMode: String): Boolean {
        var isSupport: Boolean = false
        //获取所支持对焦模式
        var listFocus: List<String>? = mParameters?.supportedFocusModes
        for (index in 0 until listFocus!!.size) {
            //如果存在 返回true
            if (listFocus[index].equals(focusMode)) {
                isSupport = true
            }
        }
        return isSupport
    }

    /**
     * 初始化相机参数
     *
     */
    fun initParameters(camera: Camera?) {
        try {
            //获取Parameters对象
            mParameters = camera?.parameters
            //设置预览格式
            mParameters?.previewFormat = ImageFormat.NV21
            //mParameters?.exposureCompensation = 5
            if(isFull){
                setPreviewSize(screenWidth,screenHeight)
            } else {
                setPreviewSize(mSurfaceView.measuredWidth,mSurfaceView.measuredHeight)
            }
            setPictureSize()
            //连续自动对焦图像
            if (isSupportFocus(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                mParameters?.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
            } else if (isSupportFocus(Camera.Parameters.FOCUS_MODE_AUTO)) {
                //自动对焦(单次)
                mParameters?.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
            }
            mCamera?.parameters = mParameters
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtil.showShortToast(mAppCompatActivity, "初始化相机失败")
        }

    }

    /**
     *
     * 设置预览界面尺寸
     */
    fun setPreviewSize(width:Int,height:Int) {
        //获取系统支持预览大小
        var localSizes: List<Camera.Size> = mParameters?.supportedPreviewSizes!!
        var biggestSize: Camera.Size? = null //最大分辨率
        var fitSize: Camera.Size? = null//优先选屏幕分辨率
        var targetSize: Camera.Size? = null//没有屏幕分辨率就取跟屏幕分辨率相近(大)的size
        var targetSize2: Camera.Size? = null//没有屏幕分辨率就取跟屏幕分辨率相近(小)的size
        var cameraSizeLength: Int = localSizes.size

        if(width.toFloat() / height == 3.0f / 4){

            for(index in 0 until cameraSizeLength){
                var size : Camera.Size = localSizes[index]
                if(size.width.toFloat() / size.height == 4/0f / 3){
                    mParameters?.setPreviewSize(size.width,size.height)
                    break
                }

            }
        } else {
            for (index in 0 until cameraSizeLength) {
                var size: Camera.Size = localSizes[index]
                if (biggestSize == null || (size.width >= biggestSize.width && size.height >= biggestSize.height)) {
                    biggestSize = size
                }

                //如果支持的比例都等于所获取到的宽高
                if (size.width == screenHeight && size.height == screenWidth) {
                    fitSize = size
                    //如果任一宽高等于所支持的尺寸
                } else if (size.width == screenHeight || size.height == screenWidth) {
                    if (targetSize == null) {
                        targetSize = size
                    } else if (size.width < screenHeight || size.height < screenWidth) {
                        targetSize2 = size
                    }
                }
            }

           if(fitSize == null){
               fitSize = targetSize
           }

           if(fitSize == null){
               fitSize = targetSize2
           }

           if(fitSize == null){
               fitSize = biggestSize
           }

            mParameters?.setPreviewSize(fitSize?.width!!, fitSize?.height!!)
        }




    }

    /**
     * 设置最佳保存图片的尺寸
     *
     */
    fun setPictureSize() {
        var localSizes: List<Camera.Size> = mParameters?.supportedPreviewSizes!!
        var biggestSize: Camera.Size? = null
        var fitSize: Camera.Size? = null
        var previewSize: Camera.Size? = mParameters?.previewSize
        var previewSizeScale: Float = 0f
        if (previewSize != null) {
            previewSizeScale = previewSize.width / previewSize.height.toFloat()
        }

        if (localSizes != null) {
            var cameraSizeLength: Int = localSizes.size
            for (index in 0 until cameraSizeLength) {
                var size: Camera.Size = localSizes[index]
                if (biggestSize == null) {
                    biggestSize = size
                } else if (size.width >= biggestSize.width && size.height >= biggestSize.height) {
                    biggestSize = size
                }

                //选出与预览界面等比的最高分辨率
                if (previewSizeScale > 0 && size.width >= previewSize?.width!! && size.height >= previewSize?.height!!) {
                    var sizeScale: Float = size.width / size.height.toFloat()
                    if (sizeScale == previewSizeScale) {
                        if (fitSize == null) {
                            fitSize = size
                        } else if (size.width >= fitSize.width && size.height >= fitSize.height) {
                            fitSize = size
                        }
                    }
                }
            }

            //如果没有选出fitsize，那么最大的Size就是FitSize
            if (fitSize == null) {
                fitSize = biggestSize
            }

            mParameters?.setPictureSize(fitSize?.width!!, fitSize?.height!!)

        }


    }


    /**
     * 保证预览方向正确
     * @param appCompatActivity Activity
     * @param cameraId 相机Id
     * @param camera 相机
     */
    fun setCameraDisplayOrientation(appCompatActivity: AppCompatActivity, cameraId: Int, camera: Camera?) {
        var info: Camera.CameraInfo = Camera.CameraInfo()
        Camera.getCameraInfo(cameraId, info)
        //rotation是预览Window的旋转方向，对于手机而言，当在清单文件设置Activity的screenOrientation="portait"时，
        //rotation=0，这时候没有旋转，当screenOrientation="landScape"时，rotation=1。
        var rotation: Int = appCompatActivity.windowManager.defaultDisplay.rotation
        var degree: Int = 0
        when (rotation) {
            Surface.ROTATION_0 -> degree = 0
            Surface.ROTATION_90 -> degree = 90
            Surface.ROTATION_180 -> degree = 180
            Surface.ROTATION_270 -> degree = 270
        }

        var result: Int = 0
        //计算图像所要旋转的角度
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degree) % 360
            result = (360 - result) % 360

        } else {
            result = (info.orientation - degree + 360) % 360
        }
        orientation = result
        //调整预览图像旋转角度
        camera?.setDisplayOrientation(result)

    }


    /**
     * 获取输出视频的width和height
     *
     */
    fun getVideoSize() {
        var biggest_width: Int = 0
        var biggest_height: Int = 0
        var fitSize_width: Int = 0
        var fitSize_height: Int = 0
        var fitSize_widthBig: Int = 0
        var fitSize_heightBig: Int = 0
        var parameters: Camera.Parameters? = mCamera?.parameters
        //得到系统支持视频尺寸
        var videoSize: List<Camera.Size>? = parameters?.supportedVideoSizes
        for (index in 0 until videoSize?.size!!) {
            var w: Int = videoSize!![index].width
            var h: Int = videoSize!![index].height
            if (biggest_width == 0 && biggest_height == 0 || w >= biggest_height && h >= biggest_width) {
                biggest_width = w
                biggest_height = h
            }

            if (w == screenHeight && h == screenWidth) {
                width = w
                height = h
            } else if (w == screenHeight || h == screenWidth) {
                if (width == 0 || height == 0) {
                    fitSize_width = w
                    fitSize_height = h

                } else if (w < screenHeight || h < screenWidth) {
                    fitSize_widthBig = w
                    fitSize_heightBig = h

                }
            }
        }

        if (width == 0 && height == 0) {
            width = fitSize_width
            height = fitSize_height
        }

        if (width == 0 && height == 0) {
            width = fitSize_widthBig
            height = fitSize_heightBig
        }

        if (width == 0 && height == 0) {
            width = biggest_width
            height = biggest_height

        }


    }

    /**
     *
     * 释放相机资源
     */
    fun releaseCamera() {
        //停止预览
        mCamera?.stopPreview()
        mCamera?.setPreviewCallback(null)
        //释放相机资源
      //  mCamera?.unlock()
        mCamera?.release()
        mCamera = null
        mHandler.removeMessages(1)
        mediaRecorder?.release()
        mediaRecorder = null
    }


    /**
     * 变焦
     * @param zoom 缩放系数
     */
    fun setZoom(zoom: Int) {
        //获取Paramters对象
        var parameters: Camera.Parameters? = mCamera?.parameters
        //如果不支持变焦
        if (!parameters?.isZoomSupported!!) {
            return
        }

        parameters.zoom = zoom
        //Camera对象重新设置Paramters对象参数
        mCamera?.parameters = parameters
        mZoom = zoom

    }

    /**
     *
     * 返回缩放值
     * @return 返回缩放值
     */
    fun getZoom(): Int {
        return mZoom
    }

    /**
     * 获取最大Zoom值
     * @return zoom
     */
    fun getMaxZoom(): Int {
        var parameters: Camera.Parameters? = mCamera?.parameters
        if (!parameters?.isZoomSupported!!) {
            return -1
        }
        return if (parameters.maxZoom > 50) {
            50
        } else {
            parameters.maxZoom
        }


    }


    /**
     *
     * 自动变焦
     *
     */
    fun autoFocus(){
        mCamera?.autoFocus(object : Camera.AutoFocusCallback{
            override fun onAutoFocus(success: Boolean, camera: Camera?) {

            }
        })
    }


    /**
     * @return 返回路径
     *
     *
     */
    fun getPhotoPath(data: ByteArray,takePhotoOrientation:Int) {
        ThreadPoolUtil.execute(object : Runnable {
            override fun run() {
                var timeMillis: Long = System.currentTimeMillis()
                var time: String = SystemUtil.formatTime(timeMillis)
                //拍照数量
                photoNum++
                //图片名字
                var name: String = SystemUtil.formatTime(timeMillis, SystemUtil.formatRandom(photoNum) + ".jpg")
                //创建具体文件
                var file = File(photosFile, name)
                if (!file.exists()) {
                    try {
                        file.createNewFile()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        return
                    }

                }


                try {
                    var fos = FileOutputStream(file)
                    try {
                        //将数据写入文件
                        fos.write(data)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } finally {
                        try {
                            fos.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }

                    //将图片旋转
                    rotateImageView(mCameraId, takePhotoOrientation, file.absolutePath)
                   // rotateImageView(mCameraId, takePhotoOrientation, Configuration.insidePath + file.name)


                    //将图片保存到手机相册
                 //   SystemUtil.saveAlbum(Configuration.insidePath + file.name, file.name, mAppCompatActivity)
                    //将图片复制到外部(target SDK 设置Android10以下)
                //    SystemUtil.copyPicture(Configuration.insidePath + file.name, Configuration.OUTPATH, file.name)

                    //将图片保存到手机相册 方式1
                 //   SystemUtil.saveAlbum(file.absolutePath,file.name,mAppCompatActivity)
                    //将图片保存到手机相册 方式2
                    ImageUtil.saveAlbum(mAppCompatActivity,file)

                    var message = Message()
                    message.what = 1
                //    message.obj = Configuration.insidePath + file.name
                    message.obj = file.absolutePath
                    mHandler.sendMessage(message)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        })
    }


    /**
     * 旋转图片
     * @param cameraId 前置还是后置
     * @param orientation 拍照时传感器方向
     * @param path 图片路径
     */
    fun rotateImageView(cameraId: Int, orientation: Int, path: String) {
        var bitmap = BitmapFactory.decodeFile(path)
        var matrix = Matrix()
        matrix.postRotate(orientation.toFloat())
        //创建新的图片
        var resizedBitmap: Bitmap? = null
        //0是后置
//        if (cameraId == 0) {
//            if (orientation == 90) {
//                matrix.postRotate(90f)
//            }
//        }
//
//        //1是前置
//        if (cameraId == 1) {
//            matrix.postRotate(270f)
//        }

        //1是前置
        if(cameraId == 1){
            if(orientation == 90){
                matrix.postRotate(180f)
            }
        }

        //创建新的图片
        resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        //新增 如果是前置 需要镜面翻转处理
        if (cameraId == 1) {
            var martix1 = Matrix()
            martix1.postScale(-1f, 1f)
            resizedBitmap =
                Bitmap.createBitmap(resizedBitmap, 0, 0, resizedBitmap.width, resizedBitmap.height, martix1, true)
        }

        var file = File(path)
        //重新写入文件

        try {
            //写入文件
            var fos: FileOutputStream? = null
            fos = FileOutputStream(file)
            //默认jpg
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
            resizedBitmap.recycle()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     *
     * 录制方法
     *
     */
    fun startRecord(path: String, name: String) {
        //解锁Camera硬件
        mCamera?.unlock()
        mediaRecorder?: MediaRecorder()
        mediaRecorder?.let {
            it.setCamera(mCamera)
            //音频源 麦克风
            it.setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
            //视频源 camera
            it.setVideoSource(MediaRecorder.VideoSource.CAMERA)
            //输出格式
            it.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            //音频编码
            it.setAudioEncoder(MediaRecorder.VideoEncoder.DEFAULT)
            //视频编码
            it.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            //设置帧频率
            it.setVideoEncodingBitRate(1 * 1024 * 1024 * 100)
            Log.d("sssd视频宽高：", "宽" + width + "高" + height + "")
            it.setVideoSize(width, height)
            //每秒的帧数
            it.setVideoFrameRate(20)

            //调视频旋转角度 如果不设置 后置和前置都会被旋转播放
            if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                if (orientation == 270 || orientation == 90 || orientation == 180) {
                    it.setOrientationHint(180)
                } else {
                    it.setOrientationHint(0)
                }
            } else {
                if (orientation == 90) {
                    it.setOrientationHint(90)
                }
            }

            var file = File(path)
            if (!file.exists()) {
                file.mkdirs()
            }


            //设置输出文件名字
            it.setOutputFile(path + File.separator + name + "mp4")
            var file1 = File(path + File.separator + name + "mp4")
            if (file1.exists()) {
                file1.delete()
            }

            //设置预览
            it.setPreviewDisplay(mSurfaceView.holder.surface)


            try {
                //准备录制
                it.prepare()
                //开始录制
                it.start()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }


    }


    /**
     * 停止录制
     *
     *
     */
    fun stopRecord() {
        mediaRecorder?.release()
        mediaRecorder = null
        mCamera?.release()
        openCamera(mCameraId)
        //并设置预览
        startPreview()
    }


}