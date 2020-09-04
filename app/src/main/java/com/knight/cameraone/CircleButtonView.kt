package com.knight.cameraone

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View


/**
 *
 * @ProjectName:    Camera1Kotlin
 * @Package:        com.knight.cameraone
 * @ClassName:      CircleButtonView
 * @Description:    java类作用描述
 * @Author:         knight
 * @CreateDate:     2019-10-07 14:51
 * @UpdateUser:     更新者
 * @UpdateDate:     2019-10-07 14:51
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */

class CircleButtonView : View {

    private val WHAT_LONG_CLICK:Int = 1
    private lateinit var mBigCirclePaint:Paint
    private lateinit var mSmallCirclePaint:Paint
    private lateinit var mProgressCirclePaint:Paint
    //当前View的高
    private var mHeight:Int = 0
    //当前View的宽
    private var mWidth:Int = 0
    private var mInitBitRadius:Float = 0f
    private var mInitSmallRadius:Float = 0f
    private var mBigRadius:Float = 0f
    private var mSmallRadius:Float = 0f
    private var mStartTime:Long = 0
    private var mEndTime:Long = 0
    //录制状态
    private var isRecording : Boolean = false
    //达到最大录制时间
    private var isMaxTime:Boolean = false
    //长按最短时间(毫秒)
    private var mLongClickTime:Long = 1000
    //最大录制时间
    private var mTime : Int = 5
    private var mCurrentProgress:Float = 0f
    //录制最短时间
    private var mMinTime:Int = 3
    //进度条颜色
    private var mProgressColor:Int = 0XFFFFFF
    //圆环宽度
    private var mProgressW:Float = 18f
    //当前手指处于按压状态
    private var isPresseds:Boolean = false
    //圆弧进度变化
    private var mProgressAni:ValueAnimator?=null







    constructor(context : Context) :super(context){
        init(context,null)
    }


    constructor(context: Context,attributes: AttributeSet):super(context,attributes){
        init(context,attributes)
    }

    constructor(context: Context,attributes: AttributeSet,defStyleAttr:Int):super(context,attributes,defStyleAttr){
        init(context,attributes)

    }


    /**
     * 初始化属性
     *
     */
    fun init(context: Context,attrs:AttributeSet?){
        var a = context.obtainStyledAttributes(attrs,R.styleable.CircleButtonView)
        mMinTime = a.getInt(R.styleable.CircleButtonView_minTime,0)
        mTime = a.getInt(R.styleable.CircleButtonView_maxTime,10)
        mProgressW = a.getDimension(R.styleable.CircleButtonView_progressWidth,12f)
        mProgressColor = a.getColor(R.styleable.CircleButtonView_progressColor, Color.parseColor("#6ABF66"))
        a.recycle()
        //初始化画笔抗锯齿、颜色
        mBigCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mBigCirclePaint?.color = Color.parseColor("#DDDDDD")
        mSmallCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mSmallCirclePaint?.color = Color.parseColor("#FFFFFF")
        mProgressCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mProgressCirclePaint?.color = mProgressColor

        mProgressAni = ValueAnimator.ofFloat(0f,360f)
        mProgressAni?.duration = (mTime * 1000).toLong()

    }


    /**
     * 测量方法
     *
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mWidth = MeasureSpec.getSize(widthMeasureSpec)
        mHeight = MeasureSpec.getSize(heightMeasureSpec)
        mBigRadius = mWidth/2*0.75f
        mInitBitRadius = mBigRadius
        mSmallRadius = mBigRadius * 0.75f
        mInitSmallRadius = mSmallRadius
    }

    /**
     *
     * 绘制方法
     */
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        //绘制外圆
        canvas?.drawCircle(mWidth / 2f,mHeight / 2f,mBigRadius,mBigCirclePaint)

        //绘制内圆
        canvas?.drawCircle(mWidth / 2f,mHeight / 2f,mSmallRadius,mSmallCirclePaint)
        //录制的过程中绘制进度条
        when(isRecording){
            true -> drawProgress(canvas)
        }
    }

    /**
     * 绘制圆形进度
     * @param canvas
     *
     */
    fun drawProgress(canvas: Canvas?){
        mProgressCirclePaint?.strokeWidth = mProgressW
        mProgressCirclePaint?.style = Paint.Style.STROKE
        //用于定于的圆弧的形状和大小界限
        var oval = RectF(
            mWidth / 2 - (mBigRadius - mProgressW / 2),
            mHeight / 2 - (mBigRadius - mProgressW / 2),
            mWidth / 2 + (mBigRadius - mProgressW / 2),
            mHeight / 2 + (mBigRadius - mProgressW / 2))
        //根据进度画圆弧
        canvas?.drawArc(oval,-90f,mCurrentProgress,false,mProgressCirclePaint)


    }

    var mHandler = @SuppressLint("HandlerLeak")
    object: Handler(){
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            when(msg?.what){
                WHAT_LONG_CLICK -> {
                    onLongClickListener?.onLongClick()
                    //内外圆动画，内圆缩小，外圆放大
                    startAnimation(mBigRadius,mBigRadius * 1.33f,mSmallRadius,mSmallRadius * 0.7f)
                }


            }
        }
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when(event?.action){
            MotionEvent.ACTION_DOWN ->{
                isPresseds = true
                mStartTime = System.currentTimeMillis()
                var mMessage = Message.obtain()
                mMessage.what = WHAT_LONG_CLICK
                mHandler.sendMessageDelayed(mMessage,mLongClickTime)
            }
            MotionEvent.ACTION_UP ->{
                isPresseds = false
                isRecording = false
                mEndTime = System.currentTimeMillis()
                if(mEndTime - mStartTime < mLongClickTime){
                    mHandler.removeMessages(WHAT_LONG_CLICK)
                    onClickListener?.onClick()

                }else{
                    startAnimation(mBigRadius, mInitBitRadius, mSmallRadius, mInitSmallRadius)//手指离开时动画复原
                    if(mProgressAni != null && mProgressAni?.currentPlayTime!! / 1000 < mMinTime && !isMaxTime){
                         onLongClickListener?.onNoMinRecord(mMinTime)
                        mProgressAni?.cancel()
                    }else{
                        //录制完成
                        if(!isMaxTime){
                            onLongClickListener?.onRecordFinishedListener()

                        }
                    }

                }
            }

        }
        return true
    }


    /**
     *
     * 动画开始
     */
    fun startAnimation(bigStart:Float,bigEnd:Float,smallStart:Float,smallEnd:Float){
        var bigObjAni:ValueAnimator = ValueAnimator.ofFloat(bigStart,bigEnd)
        bigObjAni.duration = 150
//        bigObjAni.addUpdateListener(object :ValueAnimator.AnimatorUpdateListener{
//            override fun onAnimationUpdate(animation: ValueAnimator?) {
//                 mBigRadius = animation?.animatedValue as Float
//                 invalidate()
//            }
//        })



        bigObjAni.addUpdateListener {
             mBigRadius = it.animatedValue as Float
            invalidate()
        }

        var smallObjAni:ValueAnimator = ValueAnimator.ofFloat(smallStart,smallEnd)
        smallObjAni.duration = 150
        smallObjAni.addUpdateListener {
            mSmallRadius = it.animatedValue as Float
            invalidate()
        }

        bigObjAni.start()
        smallObjAni.start()

        smallObjAni.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                //开始绘制圆形进度
                when(isPresseds){
                    true -> {
                        isRecording = true
                        isMaxTime = false
                        startProgressAnimation()
                    }

                }
            }

            override fun onAnimationStart(animation: Animator?) {
               isRecording = false
            }

            override fun onAnimationCancel(animation: Animator?) {

            }
        })


    }


    /**
     * 圆形进度变化动画
     *
     */
    fun startProgressAnimation(){
        mProgressAni?.start()
        mProgressAni?.addUpdateListener {
            mCurrentProgress = it.animatedValue as Float
            invalidate()
        }
        mProgressAni?.addListener(object : Animator.AnimatorListener{
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                //录制动画结束时，即为录制全部完成
                when(isPresseds){
                    true -> {
                        isPresseds=false
                        isMaxTime = true
                        onLongClickListener?.onRecordFinishedListener()
                        startAnimation(mBigRadius,mInitBitRadius,mSmallRadius,mInitSmallRadius)
                        //隐藏进度条
                        mCurrentProgress = 0f
                        invalidate()
                    }

                }
            }

            override fun onAnimationCancel(animation: Animator?) {

            }

            override fun onAnimationStart(animation: Animator?) {

            }

        })
    }


    interface OnLongClickListener{
        fun onLongClick()

        //没达到最小录制时间
        fun onNoMinRecord(currentTime:Int)

        //录制完成
        fun onRecordFinishedListener()
    }

    private var onLongClickListener:OnLongClickListener?= null

    interface OnClickListener{

        fun onClick()
    }


    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener  = onClickListener

    }


    private var onClickListener : OnClickListener? = null

    fun setOnLongClickListener(onLongClickListener:OnLongClickListener){
        this.onLongClickListener = onLongClickListener

    }









}