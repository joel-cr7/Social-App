package com.example.socialapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.socialapp.daos.PostDao
import com.example.socialapp.databinding.ActivityMainBinding
import com.example.socialapp.models.Post
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query


class MainActivity : AppCompatActivity(), PostAdapter.onLikeListener {

    lateinit var binding: ActivityMainBinding
    private lateinit var adapter: PostAdapter
    private lateinit var postDao: PostDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        postDao = PostDao()

        binding.fab.setOnClickListener{
            val intent = Intent(this, CreatePostActivity::class.java)
            startActivity(intent)
        }

        setUpRecyclerView()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_item, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.logout -> {
                FirebaseAuth.getInstance().signOut();
                //each time the user logout and tries to login again then the gmail chooser must popup
                //so completely log out the user,
                GoogleSignIn.getClient(
                        this,
                        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                ).signOut()
                Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, SigninActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setUpRecyclerView() {
        //pass a firestore recycler option in constructor of adapter
        //make a query to retrieve data from firebase
        val postCollection = postDao.collection   //first get the collection
        val query = postCollection.orderBy("createdAt", Query.Direction.DESCENDING)  //orderby the time that post is created and sort in descending order
        val recyclerOptions = FirestoreRecyclerOptions.Builder<Post>().setQuery(query, Post::class.java).build()   //setting the query here

        adapter = PostAdapter(recyclerOptions, this)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun onStart() {
        super.onStart()
        //specify when to start listening for changes
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        //specify when to stop listening for changes
        adapter.stopListening()
    }

    override fun onLiked(postId: String) {
        postDao.updateLikes(postId)
    }
}