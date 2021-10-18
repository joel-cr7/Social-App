package com.example.socialapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.socialapp.daos.PostDao
import com.example.socialapp.databinding.ActivityCreatePostBinding

class CreatePostActivity : AppCompatActivity() {

    lateinit var binding: ActivityCreatePostBinding
    private lateinit var postDao: PostDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        postDao = PostDao()

        binding.postButton.setOnClickListener{
            val input = binding.postInput.text.toString().trim()
            if(input.isNotEmpty()){
                postDao.addPost(input)
                finish()
                Toast.makeText(this, "Post uploaded", Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(this, "Enter your text !!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}