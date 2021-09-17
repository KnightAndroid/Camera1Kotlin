package com.knight.cameraone.activity

import android.content.pm.ActivityInfo
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.view.SurfaceHolder
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.knight.cameraone.R
import kotlinx.android.synthetic.main.activity_playaudio.*

/**
 *
 * @ProjectName:    Camera1Kotlin
 * @Package:        com.knight.cameraone.activity
 * @ClassName:      PlayAudioActivity
 * @Description:    java类作用描述
 * @Author:         knight
 * @CreateDate:     2019-10-07 13:22
 * @UpdateUser:     更新者
 * @UpdateDate:     2019-10-07 13:22
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */

class PlayAudioActivity : AppCompatActivity(), MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {



    var player:MediaPlayer?=null
    var surfaceWidth:Int = 0
    var surfaceHeight:Int = 0

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playaudio)

        //实例化MediaPlayer对象
        player = MediaPlayer()
        player?.setOnCompletionListener(this)
        player?.setOnPreparedListener(this)
        sf_play.post {
            surfaceWidth = sf_play.width
            surfaceHeight = sf_play.height
        }
        //设置数据源，也就是播放文件地址，可以是网络地址
        //var dataPath = Configuration.OUTPATH + "/videomp4"
        var dataPath = intent.getStringExtra("videoPath")

        try {
            player?.setDataSource(dataPath)
        }catch (e:Exception){
            e.printStackTrace()
        }

        sf_play.holder.addCallback(object: SurfaceHolder.Callback{
            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {

            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                //将播放器和SurfaceView关联起来
                player?.setDisplay(holder)
                //异步缓冲当前视频文件，也有一个同步接口
                player?.prepareAsync()
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {

            }

        })

        player?.setOnVideoSizeChangedListener { mp, width, height ->
            changeVideoSize()
        }

    }

    /**
     *
     * 计算播放视频的宽高
     */
    private fun changeVideoSize() {
        var videoWidth = player?.videoWidth
        var videoHeight = player?.videoHeight
        //根据视频尺寸去计算->视频可以在sufaceView中放大的最大倍数。
        var max:Float
        if (resources.configuration.orientation === ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            //竖屏模式下按视频宽度计算放大倍数值
            max = Math.max(
                videoWidth?.toFloat()!! / surfaceWidth.toFloat(),
                videoHeight?.toFloat()!! / surfaceHeight.toFloat()
            )
        } else {
            //横屏模式下按视频高度计算放大倍数值
            max = Math.max(
                videoWidth?.toFloat()!! / surfaceHeight.toFloat(),
                videoHeight?.toFloat()!! / surfaceWidth.toFloat()
            )
        }
        //视频宽高分别/最大倍数值 计算出放大后的视频尺寸
        videoWidth = Math.ceil(videoWidth?.toFloat() / max.toDouble()).toInt()
        videoHeight = Math.ceil(videoHeight?.toFloat() / max.toDouble()).toInt()
        //无法直接设置视频尺寸，将计算出的视频尺寸设置到surfaceView 让视频自动填充。
        val sfPlayLayoutParams: ConstraintLayout.LayoutParams =
            sf_play.layoutParams as ConstraintLayout.LayoutParams
        sfPlayLayoutParams.height = videoHeight
        sfPlayLayoutParams.width = videoWidth
        sf_play.layoutParams = sfPlayLayoutParams
    }


    /**
     * 设置循环播放
     * @param mp
     *
     *
     */
    override fun onCompletion(mp: MediaPlayer?) {
        player?.start()
        player?.isLooping = true
    }

    /**
     * 准备播放
     * @param mp
     *
     */
    override fun onPrepared(mp: MediaPlayer?) {
       player?.start()
    }


    /**
     * 释放资源
     *
     *
     */
    override fun onDestroy(){
        super.onDestroy()
        player?.let {
            it.reset()
            it.release()
        }
    }
}