package com.example.socialapp.daos

import com.example.socialapp.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

//dao class is created to interact with firebase firestore
//collections in firestore are a way of storing various things

class UserDao {
    private val db = FirebaseFirestore.getInstance()
    private val userCollection = db.collection("users")   //to create a collection with specified name . If already existing add key and value to it

    //adding data to firebase Firestore
    fun addUser(user: User){
        user?.let {
            GlobalScope.launch(Dispatchers.IO) {
                //here we are setting the collection with a key which is 'uid', and its data/value will be the whole 'User' object ie. 'it'
                userCollection.document(user.uid).set(it)
            }
        }
    }

    //this function is called inside the PostDao to get the user with the mentioned id
    //note: All firebase callbacks are Asynchronous ie. it takes time.
    //get() returns 'task' of type 'DocumentSnapshot' so return type of the function is 'Task', which also takes some time
    //so when we call this function we can do it using coroutines using await() function
    fun getUser(userId: String): Task<DocumentSnapshot> {
        return userCollection.document(userId).get()
    }
}