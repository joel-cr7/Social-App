package com.example.socialapp.models

data class Post (val text: String = "",
            val createdBy: User = User(),
            val createdAt: Long = 0L,
            val likedBy: ArrayList<String> = ArrayList())
            //not storing 'likes count' (we can get it by the size of the above ArrayList)