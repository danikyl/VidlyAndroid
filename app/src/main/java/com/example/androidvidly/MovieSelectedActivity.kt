package com.example.androidvidly

import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MovieSelectedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_selected)
        //Getting movie object information passed through tag by previous activity
        val title = intent.getStringExtra(MOVIE_OBJECT_TITLE)
        val imageURL = intent.getStringExtra(MOVIE_OBJECT_POSTER_URL)
        val movieYear = intent.getStringExtra(MOVIE_OBJECT_RELEASE_DATE)
        val genres = intent.getStringExtra(MOVIE_OBJECT_GENRES)
        var scoreText = intent.getStringExtra(MOVIE_OBJECT_VOTES) + "pts"
        //Finished gathering object information
        //Setting the views for this new screen
        val suggestedOne = findViewById<ImageView>(R.id.suggestedImage1)
        val suggestedTwo = findViewById<ImageView>(R.id.suggestedImage2)
        val suggestedThree = findViewById<ImageView>(R.id.suggestedImage3)
        val movieYearText = findViewById<TextView>(R.id.movieYear).apply {
            text = movieYear
        }
        val mMovieTitleText = findViewById<TextView>(R.id.movieTitle).apply {
            text = title
        }
        val score = findViewById<TextView>(R.id.score).apply {
            text=scoreText
        }
        val related = findViewById<TextView>(R.id.suggestions).apply {
            text="Movies you might like:"
        }
        val homeButton = findViewById<Button>(R.id.homeButton)
        homeButton.text = "Home"
        val mMovieIcon = findViewById<ImageView>(R.id.movieIcon)
        //instantiating an object from Main activity to use the function loadImageFromUrl()
        val mainActivityObject:MainActivity=MainActivity()
        mainActivityObject.loadImageFromUrl(imageURL, mMovieIcon)
        //Finished setting the views

        //Loop below gets the suggested movies
        var i = 0
        while (i < 3) {
            when(i) {
                0 -> {
                    var randomMovie = movieObjectsList.random()
                    if (randomMovie.title != title) {
                        suggestedOne.setTag(randomMovie)
                        suggestedOne.setPadding(0, 0, 0, 0);
                        mainActivityObject.loadImageFromUrl(randomMovie.poster_url, suggestedOne)
                    }
                    else i--
                }
                1 -> {
                    var randomMovie = movieObjectsList.random()
                    if ((randomMovie.title != (suggestedOne.getTag() as Movie).title) and (randomMovie.title != title)) {
                        suggestedTwo.setTag(randomMovie)
                        suggestedTwo.setPadding(0, 0, 0, 0);
                        mainActivityObject.loadImageFromUrl(randomMovie.poster_url, suggestedTwo)
                    }
                    else i--
                }
                2 -> {
                    var randomMovie = movieObjectsList.random()
                    if ((randomMovie.title != (suggestedOne.getTag() as Movie).title)
                        and (randomMovie.title != (suggestedTwo.getTag() as Movie).title) and  (randomMovie.title != title)) {
                        suggestedThree.setTag(randomMovie)
                        suggestedThree.setPadding(0, 0, 0, 0);
                        mainActivityObject.loadImageFromUrl(randomMovie.poster_url, suggestedThree)
                    }
                    else i--
                }
            }
            i++
        }
        //Finished getting the suggested movies.

    }
    fun showMore(view: View) {
        val intent = Intent(this, MovieSelectedActivity::class.java).apply {
            var movieObj : Movie = view.getTag() as Movie
            putExtra(MOVIE_OBJECT_ID, movieObj.id)
            putExtra(MOVIE_OBJECT_GENRES, movieObj.genres.toString())
            putExtra(MOVIE_OBJECT_POSTER_URL, movieObj.poster_url)
            putExtra(MOVIE_OBJECT_RELEASE_DATE, movieObj.release_date)
            putExtra(MOVIE_OBJECT_TITLE, movieObj.title)
            putExtra(MOVIE_OBJECT_VOTES, movieObj.vote_average)
        }
        startActivity(intent)
    }
    //Function to be used by onCLick method for 'Home' button, send the user back to first screen.
    fun goHome(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}