package com.example.androidvidly

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import org.json.JSONArray
import org.json.JSONObject
import android.widget.Toast
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T



const val MOVIE_OBJECT_ID = "tokenlab.com.MOVIE_OBJECT_ID"
const val MOVIE_OBJECT_VOTES = "tokenlab.com.MOVIE_OBJECT_VOTES"
const val MOVIE_OBJECT_TITLE = "tokenlab.com.MOVIE_OBJECT_TITLE"
const val MOVIE_OBJECT_POSTER_URL = "tokenlab.com.MOVIE_OBJECT_POSTER_URL"
const val MOVIE_OBJECT_RELEASE_DATE = "tokenlab.com.MOVIE_OBJECT_RELEASE_DATE"
const val MOVIE_OBJECT_GENRES = "tokenlab.com.MOVIE_OBJECT_GENRES"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.setBackgroundDrawableResource(R.drawable.background)
        val mQueue = Volley.newRequestQueue(this)
        val url = "https://desafio-mobile.nyc3.digitaloceanspaces.com/movies" //API provided
        //val url = "http://10.0.2.2:3000"// Node.js Backend
        val gallery = findViewById<LinearLayout>(R.id.gallery)
        val inflater = LayoutInflater.from(this)
        var mAppName = findViewById<TextView>(R.id.appName)
        mAppName.text="Vidly"
        val gson = Gson()
        var progDailog = ProgressDialog.show( this,"Process ", "Loading Data...",true,true);
        var request = StringRequest(Request.Method.GET, url, Response.Listener { response ->
            var moviesJSONArray = JSONArray(response)
            for (i in 0 until moviesJSONArray.length()) {
                var e : JSONObject = moviesJSONArray.getJSONObject(i)
                //Initializing the object
                val arrayStringType = object : TypeToken<ArrayList<String>>() {}.type
                var genreListAux:ArrayList<String> = gson.fromJson(e.getString("genres"), arrayStringType)
                var movieObject: Movie = Movie(e.getString("id").toInt(), e.getString("vote_average"), e.getString("title"), e.getString("poster_url"), genreListAux, e.getString("release_date"))
                if (i==0) {
                    var mImageView = findViewById<ImageView>(R.id.imageDisplay)
                    mImageView.setTag(movieObject)
                    mImageView.setPadding(0, 0, 0, 0);
                    loadImageFromUrl(movieObject.poster_url, mImageView)
                }
                else if (i==1) {
                    var mImageView = findViewById<ImageView>(R.id.imageDisplay2)
                    mImageView.setTag(movieObject)
                    mImageView.setPadding(0, 0, 0, 0);
                    loadImageFromUrl(movieObject.poster_url, mImageView)
                }
                else{
                    if(i.rem(2)==0) {
                        var view = inflater.inflate(R.layout.activity_main, gallery, false)
                        var mImageView = view.findViewById<ImageView>(R.id.imageDisplay)
                        mImageView.setTag(movieObject)
                        var height = gallery.height
                        view.minimumHeight=height
                        mImageView.setPadding(0, 0, 0, 0);
                        loadImageFromUrl(movieObject.poster_url, mImageView)
                        gallery.addView(view)
                    }
                    else {
                        var view = gallery.getChildAt(gallery.childCount-1)
                        var mImageView = view.findViewById<ImageView>(R.id.imageDisplay2)
                        mImageView.setTag(movieObject)
                        mImageView.setPadding(0, 0, 0, 0);
                        loadImageFromUrl(movieObject.poster_url, mImageView)
                    }
                }

            }
            progDailog.dismiss()

        }, Response.ErrorListener { error ->
            error.printStackTrace()
            val errorToast = Toast.makeText(this, "Connection failed! Please try again", Toast.LENGTH_SHORT)
            errorToast.show()
            progDailog.dismiss()
            connectionFailed()
        })
        mQueue.add(request)

    }

    fun showMore(view: View) {
        val intent = Intent(this, MovieSelectedActivity::class.java).apply {
            var movieObj : Movie = view.getTag() as Movie
            putExtra(MOVIE_OBJECT_ID, movieObj.id)
            putExtra(MOVIE_OBJECT_GENRES, movieObj.genres)
            putExtra(MOVIE_OBJECT_POSTER_URL, movieObj.poster_url)
            putExtra(MOVIE_OBJECT_RELEASE_DATE, movieObj.release_date)
            putExtra(MOVIE_OBJECT_TITLE, movieObj.title)
            putExtra(MOVIE_OBJECT_VOTES, movieObj.vote_average)
        }
        startActivity(intent)
    }
    fun loadImageFromUrl(url: String, mImageView: ImageView) {
        Picasso.with(this).load(url).placeholder(R.mipmap.ic_launcher)
            .error(R.mipmap.ic_launcher)
            .into(
                mImageView, null
            )
    }
    fun connectionFailed() {
        val intent = Intent(this, ConnectionFailed::class.java)
        startActivity(intent)
    }
}
