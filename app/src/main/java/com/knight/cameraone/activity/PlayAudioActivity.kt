package com.knight.cameraone.activity

import android.media.MediaPlayer
import android.os.Bundle
import android.view.SurfaceHolder
import androidx.appcompat.app.AppCompatActivity
import com.knight.cameraone.Configuration
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playaudio)

        //实例化MediaPlayer对象
        player = MediaPlayer()
        player?.setOnCompletionListener(this)
        player?.setOnPreparedListener(this)
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