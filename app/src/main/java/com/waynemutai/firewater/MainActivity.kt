package com.waynemutai.firewater

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.UserHandle
import android.util.Log
import android.view.FocusFinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.core.view.get
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var mDatabaseReference : DatabaseReference = FirebaseDatabase.getInstance().reference
    private lateinit var mPostGet : Button
    private val USER_ID = "Wayne"
    private lateinit var mAddMessageTextView :TextView
    private var mPostList = ArrayList<Post>()
    private lateinit var postAdapter: PostAdapter


    //Get value of specific Element
    private val postListener = object : ValueEventListener{
        override fun onDataChange(snapshot: DataSnapshot) {
            val onlinePosts = snapshot.child("posts").child("27").getValue<Post>()
            Log.d("TAG", "onDataChange: The author is ${onlinePosts?.getAuthor()} and the post is ${onlinePosts?.getContents()}")
        }

        override fun onCancelled(error: DatabaseError) {
            Log.d("TAG", "onCancelled: didn't happen")
        }

    }

    override fun onStart() {
        super.onStart()
        postAdapter = PostAdapter(mPostList,this)
        MessageList.adapter = postAdapter
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mPostGet = findViewById(R.id.postGetButton)
        mAddMessageTextView = findViewById(R.id.addMessage)
        mPostList.add(Post("Wayne","Here here"))

        MessageList.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            Toast.makeText(this,"Click on" + mPostList[i].getAuthor(), Toast.LENGTH_SHORT).show()
        }
        showMessageList()

        mAddMessageTextView.setOnEditorActionListener { textView, i, keyEvent ->
            if (i == EditorInfo.IME_ACTION_DONE){
                sendPost()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        mPostGet.setOnClickListener {
            sendPost()
            return@setOnClickListener
        }
    }

    inner class PostAdapter : BaseAdapter{
        private var pPostList = ArrayList<Post>()
        private var context : Context? = null

        constructor(pPostList : ArrayList<Post>, context: Context){
            this.pPostList = pPostList
            this.context = context
        }

        override fun getView(position: Int, contentView : View?, parent: ViewGroup?): View {
            val vh : ViewHolder
            val view : View?

            if (contentView == null){
                view = layoutInflater.inflate(R.layout.note,parent,false)
                vh = ViewHolder(view)
                view.tag = vh
            }else{
                view = contentView
                vh = view.tag as ViewHolder
            }
            vh.tvTitle.text = pPostList[position].getAuthor().toString()
            vh.tvContent.text = pPostList[position].getContents().toString()

            return view!!
        }

        override fun getItem(p0: Int): Any {
            return pPostList[p0]
        }

        override fun getItemId(p0: Int): Long {
            return p0.toLong()
        }

        override fun getCount(): Int {
            return pPostList.size
        }
    }

    private fun showMessageList(){
        val postListenerQuery = mDatabaseReference.child("posts")

        postListenerQuery.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (mPostList.size == 1){
                    for (messages in snapshot.children){
                        val post = messages.getValue(Post::class.java)
                        mPostList.add(post!!)
                        Log.d("TAG", "onDataChange: Success in Adding")
                    }
                }
                postAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("TAG", "onCancelled: Can't Connect")
            }
        })
    }

    private class ViewHolder(view: View?){
        val tvTitle : TextView
        val tvContent : TextView

        init {
            this.tvTitle = view?.findViewById(R.id.tvTitle) as TextView
            this.tvContent = view?.findViewById(R.id.tvContent) as TextView
        }
    }

    data class Post(
            private var author: String? = "",
            private var contents: String? = ""
    ) {

        fun getAuthor() : String?{
            return author
        }
        fun getContents() : String?{
            return contents
        }
    }

    private fun sendPost(){
        val input = mAddMessageTextView.text.toString()
        if (input != ""){
            //mDatabaseReference.addValueEventListener(postListener)
            writeNewPost(USER_ID,mAddMessageTextView.text.toString())
            mAddMessageTextView.text = ""
            Toast.makeText(this,"Message was added", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this,"TextSpace is Empty", Toast.LENGTH_SHORT).show()
        }
    }

    private fun writeNewPost( author: String, contents: String){
        val post = Post(author,contents)
        mDatabaseReference.child("posts").push().setValue(post)
        Toast.makeText(this,"Successfully Updated", Toast.LENGTH_SHORT).show()
    }
}