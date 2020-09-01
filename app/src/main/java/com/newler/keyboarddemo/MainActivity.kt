package com.newler.keyboarddemo

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var msgList: ArrayList<String>

    private val keyboardPanel by lazy {
        KeyboardPanel(etChat)
    }
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var keyboardHelper: KeyboardHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        msgList = arrayListOf()
        for (i in 0 until 2) {
            msgList.add("Msg${i + 1}")
        }
        keyboardHelper = KeyboardHelper(this, layout_main, recycler_view, llChat, stickerPanel, morePanel, keyboardPanel)
        btMore.setOnClickListener {
            keyboardHelper.convertInputType(InputType.MORE)
        }

        recycler_view.setHasFixedSize(true)
        val adapter = MsgListAdapter(this)
        recycler_view.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        layoutManager = LinearLayoutManager(this)
        recycler_view.layoutManager = layoutManager
        recycler_view.adapter = adapter
        scrollToBottom()

        btSticker.setOnClickListener {
            keyboardHelper.convertInputType(InputType.STICKER)
        }

        btAdd.setOnClickListener {
            msgList.add("Msg${msgList.size + 1}")
            msgList.add("Msg${msgList.size + 1}")
            adapter.notifyItemRangeInserted(msgList.size-2, 2)
            recycler_view.post {
                Log.d("MainActivity","${layoutManager.findLastVisibleItemPosition()}")
            }
        }

        btDelete.setOnClickListener {
            msgList.removeLast()
            adapter.notifyItemRemoved(msgList.size)
        }


        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
            }
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                Log.d("MainActivity", "itemCount = $itemCount posStart = $positionStart lastVisible = ${layoutManager.findLastVisibleItemPosition()}")
                Log.d("MainActivity", layoutManager.findViewByPosition(positionStart)?.toString() ?: "哈哈")
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            }
        })

        recycler_view.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
//                keyboardHelper.reset()
            }
            false
        }
    }



    private fun scrollToBottom() {
        recycler_view.adapter?.itemCount?.minus(1)?.let { recycler_view.scrollToPosition(it) }
    }


    private inner class MsgListAdapter(val context: Context) :
            RecyclerView.Adapter<MsgListAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                    LayoutInflater.from(context).inflate(R.layout.item_msg, parent, false)
            )
        }

        override fun getItemCount(): Int {
            return if (msgList.isNullOrEmpty()) 0 else msgList.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.msgTextView.text = msgList[position]
        }

        private inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val msgTextView: TextView = itemView.findViewById(R.id.tv_msg)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        keyboardHelper.release()
    }
}