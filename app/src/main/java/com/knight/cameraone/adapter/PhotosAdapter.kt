package com.knight.cameraone.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.knight.cameraone.R

/**
 *
 * @ProjectName:    Camera1Kotlin
 * @Package:        com.knight.cameraone.adapter
 * @ClassName:      PhotosAdapter
 * @Description:    java类作用描述
 * @Author:         knight
 * @CreateDate:     2019-10-11 22:29
 * @UpdateUser:     更新者
 * @UpdateDate:     2019-10-11 22:29
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */

class PhotosAdapter(private var photoLists:List<String>): RecyclerView.Adapter<PhotosAdapter.ViewHolder>(), View.OnClickListener{



    //点击回调
    var onItemClickListener:OnItemClickListener?=null

    interface OnItemClickListener{
        fun onItemClick(v:View,path:String)
    }

    fun setItemClickListener(onItemClickListener:OnItemClickListener){
        this.onItemClickListener = onItemClickListener

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.item_photo,parent,false)
        var holder = ViewHolder(view)
        //绑定事件
        view.setOnClickListener(this)
        return holder
    }

    override fun getItemCount(): Int = photoLists.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //设置Image图片
        Glide.with(holder.itemView.context).load(photoLists[position]).apply(RequestOptions.bitmapTransform(CircleCrop()))
            .override(holder.mImageView.width,holder.mImageView.height)
            .error(R.drawable.default_person_icon)
            .into(holder.mImageView)
        holder.itemView.tag = position
    }

    override fun onClick(v: View?) {

    }

    inner class ViewHolder(itemView:View): RecyclerView.ViewHolder(itemView){
       var mImageView = itemView.findViewById(R.id.iv_photo) as ImageView
    }
}