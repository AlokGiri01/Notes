package com.shagworld.notes.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.shagworld.notes.R
import com.shagworld.notes.databinding.RowItemNoteBinding
import com.shagworld.notes.model.Note
import com.shagworld.notes.uitls.EncryptionHelper.decrypt
import com.shagworld.notes.uitls.Pref
import com.shagworld.notes.uitls.Utils.getRandomColorWithOpacity
import kotlin.random.Random

class NoteAdapter(
    private var items: ArrayList<Note>
) :
    RecyclerView.Adapter<BaseViewHolder<Note>>() {
    private var mItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<Note> {
        return MainViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.row_item_note,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: BaseViewHolder<Note>, position: Int) {
        holder.bindData(items[position], position)


    }


    inner class MainViewHolder(private val binding: RowItemNoteBinding) :
        BaseViewHolder<Note>(binding.root) {
        override fun bindData(item: Note, position: Int) {
            binding.viewModel = item

            binding.tvNote.text = decrypt(item.noteDetails)

            if (item.color == 0) {
                val randomColor = getRandomColorWithOpacity()
                binding.main.setBackgroundColor(randomColor)
                items[position].color = randomColor
            } else {
                binding.main.setBackgroundColor(item.color)
            }

            val layoutParams = binding.tvNote.layoutParams
            if (item.size == 0) {
                var h = getRandomHeight()
                layoutParams.height = h
                binding.tvNote.layoutParams = layoutParams
                items[position].size = h
            } else {
                layoutParams.height = item.size
                binding.tvNote.layoutParams = layoutParams
            }

            binding.main.setOnClickListener {
                mItemClickListener?.onItemClick(item)
            }
            binding.main.setOnLongClickListener {
                mItemClickListener?.onLongClick(item.noteId)
                true
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(item: Any)
        fun onLongClick(id: Int)
    }

    private fun getRandomHeight(): Int {
        // Generate a random height for the items between 100dp and 200dp
        return Random.nextInt(100, 200)
    }


    fun setOnItemClickListener(mItemClickListener: OnItemClickListener) {
        this.mItemClickListener = mItemClickListener
    }
}


