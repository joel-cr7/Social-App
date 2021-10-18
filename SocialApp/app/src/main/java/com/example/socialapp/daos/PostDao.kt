package com.example.socialapp.daos

import com.example.socialapp.models.Post
import com.example.socialapp.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

//dao class is created to interact with firebase firestore

class PostDao {
    private val db = FirebaseFirestore.getInstance()
    val collection = db.collection("posts")
    private val auth = Firebase.auth  //get instance of firebase authentication

    //only passing text of the post and not the whole post as other attributes of the post can be generated later
    fun addPost(text: String){

        //function is called only when the post button is clicked so currentUser cant be null
        val currentUserId = auth.currentUser!!.uid

        //note: point to remember about global scope is that it runs until the whole application is running
        //sometimes it may be dangerous when we want to only associate a coroutine with a fragment/activity, bcz then the global scope coroutine will run until the whole application is running
        //so the solution is that we will have to create a coroutine Lifecycle Scopes which only runs when the fragment/activity is running
        GlobalScope.launch {
            val userDao = UserDao()
            val user = userDao.getUser(currentUserId).await().toObject(User::class.java)!!   //user cant be null
            //we only get 'DocumentSnapshot' from the getUser() function of the UserDao, convert it to 'User'
            //note: we can call await() only from coroutine or suspended function

            val currentTime = System.currentTimeMillis()

            //creating post
            val post = Post(text,user,currentTime)   //as there are no likes so it is empty, no need to pass
            collection.document().set(post)
        }

    }

    //helper function for updateLikes()
    fun getPostById(postId: String): Task<DocumentSnapshot> {
        return collection.document(postId).get()
    }

    //to update the 'likedBy' arraylist of each 'Post'
    //if the user has already liked the post then the user must be removed from the list
    //if user has not liked the post then the user must be added to the list
    fun updateLikes(postId: String){
        GlobalScope.launch {
            //getting userid and the post corresponding to the postid given in arguments
            val currentUserId = auth.currentUser!!.uid
            val post = getPostById(postId).await().toObject(Post::class.java)!!   //getting the post corresponding to the postid given in arguments
            val isLiked = post.likedBy.contains(currentUserId)   //checking if the list contains the userid

            if(isLiked){
                post.likedBy.remove(currentUserId)
            }
            else{
                post.likedBy.add(currentUserId)
            }

            collection.document(postId).set(post)
        }
    }
}