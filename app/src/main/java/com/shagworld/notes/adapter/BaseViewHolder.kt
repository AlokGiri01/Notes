package com.shagworld.notes.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Created By Alok G on 09-05-2024.
 */
abstract class BaseViewHolder<T : Any>(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bindData(item: T, position: Int)


}
