package com.kkyoungs.recordapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.kkyoungs.recordapp.databinding.ItemRecordingBinding
import java.io.File

class RecordingAdapter(var context : Context, private var dataModel : UriViewModel): ListAdapter<UriViewModel, RecordingAdapter.RecordingViewHolder>(diffUtil) {
    companion object{
        val diffUtil = object : DiffUtil.ItemCallback<UriViewModel>(){
            override fun areItemsTheSame(oldItem: UriViewModel, newItem: UriViewModel): Boolean {
                return oldItem.uri === newItem.uri
            }

            override fun areContentsTheSame(oldItem: UriViewModel, newItem: UriViewModel): Boolean {
                return oldItem.uri == newItem.uri
            }

        }
    }
    private var listener : OnRecordingClickListener ?=null

    inner class RecordingViewHolder(private var binding : ItemRecordingBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(file : File){
            binding.audioTitleItemAudio.text = file.name

            binding.playBtnItemAudio.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION){
                    if (listener!=null){
                        listener?.onItemClick(it, pos)
                    }
                }
            }
        }
    }

    fun setOnItemClickListener(listener: OnRecordingClickListener){
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordingViewHolder {
        return RecordingViewHolder(ItemRecordingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecordingViewHolder, position: Int) {
        val uriName = dataModel.uri.toString()
        val file = File(uriName)

        holder.bind(file)
    }

    interface OnRecordingClickListener{
        fun onItemClick(view: View, position: Int)
    }
}

