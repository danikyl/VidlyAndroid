package com.example.androidvidly

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MovieSelectedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_selected)

        val title = intent.getStringExtra(MOVIE_OBJECT_TITLE)
        val imageURL = intent.getStringExtra(MOVIE_OBJECT_POSTER_URL)
        val movieYear = intent.getStringExtra(MOVIE_OBJECT_RELEASE_DATE)
        val movieYearText = findViewById<TextView>(R.id.movieYear).apply {
            text = movieYear
        }
        val mMovieTitleText = findViewById<TextView>(R.id.movieTitle).apply {
            text = title
        }

        val mMovieIcon = findViewById<ImageView>(R.id.movieIcon)
        var score = findViewById<TextView>(R.id.score)
        var scoreText = intent.getStringExtra(MOVIE_OBJECT_VOTES) + "pts"
        score.text=scoreText
        val scrollObject:MainActivity=MainActivity()
        scrollObject.loadImageFromUrl(imageURL, mMovieIcon)

    }
}