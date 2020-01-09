package com.example.androidvidly

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import org.json.JSONArray
import org.json.JSONObject
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.os.Environment
import android.widget.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.PrintWriter


const val MOVIE_OBJECT_ID = "tokenlab.com.MOVIE_OBJECT_ID"
const val MOVIE_OBJECT_VOTES = "tokenlab.com.MOVIE_OBJECT_VOTES"
const val MOVIE_OBJECT_TITLE = "tokenlab.com.MOVIE_OBJECT_TITLE"
const val MOVIE_OBJECT_POSTER_URL = "tokenlab.com.MOVIE_OBJECT_POSTER_URL"
const val MOVIE_OBJECT_RELEASE_DATE = "tokenlab.com.MOVIE_OBJECT_RELEASE_DATE"
const val MOVIE_OBJECT_GENRES = "tokenlab.com.MOVIE_OBJECT_GENRES"
val movieObjectsList = arrayListOf<Movie>()//Creating array of movieObjects


class MainActivity : AppCompatActivity() {




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.setBackgroundDrawableResource(R.drawable.background)
        var mAppName = findViewById<TextView>(R.id.appName)
        mAppName.text="Vidly"//Setting the app name to first screen
        //Creating http request
        val mQueue = Volley.newRequestQueue(this)
        val url = "https://desafio-mobile.nyc3.digitaloceanspaces.com/movies" //API provided
        //val url = "http://10.0.2.2:3000"// Node.js Backend
        //Creating dialog box to notify user that data is being downloaded
        var progDailog = ProgressDialog.show( this,"Process ", "Loading Data...",true,true);
        var request = StringRequest(Request.Method.GET, url, Response.Listener { response ->
            writeCache(response) //writes the response to cache
            processData(response)
            progDailog.dismiss()

        }, Response.ErrorListener { error ->// When response failed, first try to get data from cache, if failed then show the user another screen with option to retry to connect.
            progDailog.dismiss()
            if(!getCache()) {
                error.printStackTrace()
                val errorToast =
                    Toast.makeText(this, "Connection failed! Please try again", Toast.LENGTH_SHORT)
                errorToast.show()
                connectionFailed()
            }
        })
        mQueue.add(request)
        //Finished creating http request

    }

    //Function to convert URL to image
    fun loadImageFromUrl(url: String, mImageView: ImageView) {
        try {
            Picasso.with(this).load(url).placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .into(
                    mImageView, null
                )
        }
        catch(e : Exception) {
            val errorToast = android.widget.Toast.makeText(this, "Invalid URL!", android.widget.Toast.LENGTH_SHORT)
            errorToast.show()
        }
    }

    //Function called onClick() method for imageViews of first screen
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

    //Function to start another activity (show new screen) when connection failed and cache does not exist.
    fun connectionFailed() {
        val intent = Intent(this, ConnectionFailed::class.java)
        startActivity(intent)
    }
    //Writes the successful result from API and store in cache for future offline use
    fun writeCache(response : String) : Boolean{
        val m_cacheLocation = File(getExternalCacheDir(), "/requestCache")
        var success = true
        if (!m_cacheLocation.exists()) {
            success = m_cacheLocation.mkdirs()
        }
        if (success) {
            val requestCacheFile = File(m_cacheLocation,"requestCache.txt")

            if (!requestCacheFile.exists()) {
                success = requestCacheFile.createNewFile()
            }
            if (success) {
                // directory exists or already created
                try {
                    // response is the data written to file
                    FileOutputStream(requestCacheFile).use {
                        PrintWriter(requestCacheFile).use { out -> out.println(response)
                        }
                    }
                }
                catch (e : Exception) {
                    Log.d("WRITECACHEFAILED","Failed to write to cache!" + e)
                    return false
                }

            }
            else {
                return false
            }
        }
        return true
    }
    fun getCache() : Boolean{
        var inputAsString=""
        val cacheToast =
            Toast.makeText(this, "Offline! Getting data from cache", Toast.LENGTH_SHORT)
        cacheToast.show()
        var progDailog = ProgressDialog.show( this,"Process ", "Loading cache...",true,true);
        //Starts getting string request from CACHE
        val m_cacheLocation = File(getExternalCacheDir(), "/requestCache")
        var success = true
        if (!m_cacheLocation.exists()) {
            Log.d("READCACHEFAILE", "Cache location doesn't exist.")
            success=false
        }
        if (success) {
            val requestCacheFile = File(m_cacheLocation,"requestCache.txt")

            if (!requestCacheFile.exists()) {
                Log.d("READCACHEFAILE", "Cache FILE doesn't exist.")
                success=false
            }
            if (success) {
                try {
                    inputAsString = FileInputStream(requestCacheFile).bufferedReader().use { it.readText() }

                }
                catch (e : Exception) {
                    Log.d("READCACHEFAILED","Failed to read cache!" + e)
                    success = false
                }

            }
            else {
                success = false
            }
        }
        //Stops getting string request from CACHE
        //Process the string request to generate views
        if (inputAsString.length > 0 && success) {
            processData(inputAsString)
        }
        else {
            success=false
        }
        progDailog.dismiss()
        if (!success) return false
        return true
    }
    //Function to process the result from API request or from CACHE
    fun processData(inputAsString: String) {
        val gson = Gson()
        val gallery = findViewById<LinearLayout>(R.id.gallery)//getting linear layout to inflate
        val inflater = LayoutInflater.from(this)//setting inflater
        var moviesJSONArray = JSONArray(inputAsString)
        //Loop below gets the response in Json format and converts to objects of type "Movie"
        for (i in 0 until moviesJSONArray.length()) {
            var e: JSONObject = moviesJSONArray.getJSONObject(i)
            //Initializing the object
            val arrayStringType = object : TypeToken<ArrayList<String>>() {}.type
            var genreListAux: ArrayList<String> =
                gson.fromJson(e.getString("genres"), arrayStringType)
            var movieObject: Movie = Movie(
                e.getString("id").toInt(),
                e.getString("vote_average"),
                e.getString("title"),
                e.getString("poster_url"),
                genreListAux,
                e.getString("release_date")
            )
            //Finisehd creating the object
            movieObjectsList.add(movieObject)//Inserting the movie object in array of objects
            if (i == 0) {//In case of first image, no need to inflate. Simple adding the image to imageView and setting the tag of image view to carry object Movie related to the view
                var mImageView = findViewById<ImageView>(R.id.imageDisplay)
                mImageView.setTag(movieObject)
                mImageView.setPadding(0, 0, 0, 0);
                loadImageFromUrl(movieObject.poster_url, mImageView)
            } else if (i == 1) {//In case of second image, follow the same approach as first Image, but use imageDisplay2 instead.
                var mImageView = findViewById<ImageView>(R.id.imageDisplay2)
                mImageView.setTag(movieObject)
                mImageView.setPadding(0, 0, 0, 0);
                loadImageFromUrl(movieObject.poster_url, mImageView)
            } else {
                //Case where we do need to inflate and add image to left imageView
                if (i.rem(2) == 0) {
                    var view = inflater.inflate(R.layout.activity_main, gallery, false)
                    var mImageView = view.findViewById<ImageView>(R.id.imageDisplay)
                    mImageView.setTag(movieObject)
                    var height = gallery.height
                    view.minimumHeight = height
                    mImageView.setPadding(0, 0, 0, 0);
                    loadImageFromUrl(movieObject.poster_url, mImageView)
                    gallery.addView(view)
                }
                //Case where we do need to inflate and add image to right imageView
                else {
                    var view = gallery.getChildAt(gallery.childCount - 1)
                    var mImageView = view.findViewById<ImageView>(R.id.imageDisplay2)
                    mImageView.setTag(movieObject)
                    mImageView.setPadding(0, 0, 0, 0);
                    loadImageFromUrl(movieObject.poster_url, mImageView)
                }
            }

        }
    }
}
