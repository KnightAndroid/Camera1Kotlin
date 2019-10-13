package com.knight.cameraone.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View

/**
 *
 * @ProjectName:    Camera1Kotlin
 * @Package:        com.knight.cameraone.view
 * @ClassName:      FaceDeteView
 * @Description:    java类作用描述
 * @Author:         knight
 * @CreateDate:     2019-10-11 22:08
 * @UpdateUser:     更新者
 * @UpdateDate:     2019-10-11 22:08
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */

class FaceDeteView : View {
    var mPaint: Paint? = null
    var mColor:String = "#42ed45"
    var mFaces:ArrayList<RectF>?=null


    constructor(context: Context):super(context){
        init()
    }
    constructor(context: Context,attributes: AttributeSet):super(context,attributes){
        init()
    }
    constructor(context: Context,attributes: AttributeSet,defStyleAttr:Int):super(context,attributes,defStyleAttr){
        init()
    }



    fun init(){
        mPaint = Paint()
        mPaint?.color = Color.parseColor(mColor)
        mPaint?.style = Paint.Style.STROKE
        mPaint?.strokeWidth =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, context.resources.displayMetrics)
        mPaint?.isAntiAlias = true


    }
    override fun onDraw(canvas: Canvas){
//        super.onDraw(canvas)
//        for(index in 0 until mFaces?.size!!){
//            canvas.drawRect(mFaces!![index],mPaint)
//        }

        super.onDraw(canvas)
        if (mFaces != null) {
            for (face in mFaces!!) {

                canvas.drawRect(face, mPaint!!)
            }

        }
    }



    fun setFace(mFaces:ArrayList<RectF>){
        this.mFaces = mFaces
        invalidate()

    }
}