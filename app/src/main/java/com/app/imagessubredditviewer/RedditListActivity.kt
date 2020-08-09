package com.app.imagessubredditviewer

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.imagessubredditviewer.adapter.RedditImageAdapter
import com.app.imagessubredditviewer.network.RetrofitFactory
import com.app.imagessubredditviewer.utils.NetworkUtils
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class RedditListActivity : AppCompatActivity() {
    lateinit var activity: RedditListActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        activity = this
        setAdapter()
        Dexter.withActivity(activity)
            .withPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    getListRecords()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {


                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {

                    Toast.makeText(
                        activity,
                        "Please provide storage permission",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }).check()


    }

    private fun getListRecords() {

        if (NetworkUtils.isNetworkConnected(this)) {
            progressHorizontal.visibility = View.VISIBLE
            val service = RetrofitFactory.makeRetrofitService()
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    val response = service.getListing()
                    withContext(Dispatchers.Main) {
                        progressHorizontal.visibility = View.GONE
                        try {
                            if (response.isSuccessful) {
                                redditImageAdapter?.setRecords(response.body()?.data?.children)
                            } else {
                                Toast.makeText(
                                    activity,
                                    "Error: ${response.code()}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: HttpException) {
                            Toast.makeText(activity, "Exception ${e.message}", Toast.LENGTH_SHORT)
                                .show()
                        } catch (e: Throwable) {
                            Toast.makeText(
                                activity,
                                getString(R.string.something_went_wrong),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                progressHorizontal.visibility = View.GONE
                Toast.makeText(activity, getString(R.string.error_result), Toast.LENGTH_SHORT)
                    .show()

            }

        } else {
            Toast.makeText(activity, getString(R.string.check_network), Toast.LENGTH_SHORT).show()
        }
    }

    var redditImageAdapter: RedditImageAdapter? = null

    private fun setAdapter() {

        with(rvImageList) {

            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            redditImageAdapter = RedditImageAdapter()
            adapter = redditImageAdapter

        }

    }
}