package com.example.socialapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.socialapp.databinding.ItemPostBinding
import com.example.socialapp.models.Post
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

//firestore provides custom adapter (could have used recyclerview adapter)
//this adapter listens to realtime data from the firebase firestore
                                                                                            //mention what type of item we will be putting in the recyclerview ie. Post and next is the viewHolder
class PostAdapter(options: FirestoreRecyclerOptions<Post>, val listener: onLikeListener) : FirestoreRecyclerAdapter<Post, PostAdapter.PostViewHolder>(
    options
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post,parent,false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int, model: Post) {
        holder.binding.postTitle.text = model.text
        holder.binding.userName.text = model.createdBy.displayName
        holder.binding.likeCount.text = model.likedBy.size.toString()
        holder.binding.emailId.text = "email: "+ model.createdBy.email
        Glide.with(holder.binding.userimage.context).load(model.createdBy.imageUrl).circleCrop().into(holder.binding.userimage)
        holder.binding.createdAt.text = Utils.getTimeAgo(model.createdAt)

        //note: we are not handling likes using clicks
        //logic for the like button
        val auth = Firebase.auth    //getting current user
        val userId = auth.currentUser!!.uid   //getting the users id

        //if userid is inside arraylist ie. inside 'likedBy' then post is liked else not liked
        if(model.likedBy.contains(userId)){
            //setting imageView
            holder.binding.likeButton.setImageDrawable(ContextCompat.getDrawable(holder.binding.likeButton.context, R.drawable.ic_like))
        }
        else{
            holder.binding.likeButton.setImageDrawable(ContextCompat.getDrawable(holder.binding.likeButton.context, R.drawable.ic_unlike))
        }
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val binding = ItemPostBinding.bind(itemView)

        init {
            binding.likeButton.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            //use 'snapshots' from the Firestore recyclerview adapter to get a particular post from firestore and then get its id
            listener.onLiked(snapshots.getSnapshot(adapterPosition).id)
        }
    }

    //interface for handling likes on posts
    interface onLikeListener{
        fun onLiked(postId: String)
    }
}
