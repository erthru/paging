package com.erthru.paging

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.Toast
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.androidnetworking.interfaces.JSONObjectRequestListener
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    lateinit var parcelable: Parcelable
    val data = ArrayList<Name>()
    var allData = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mRecyclerView.layoutManager = LinearLayoutManager(this)
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))

        loadPage()

        mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){

            var directiorDown:Boolean = false

            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                directiorDown = dy > 0
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {

                if (recyclerView?.canScrollVertically(1)?.not()!!
                    && newState == RecyclerView.SCROLL_STATE_IDLE
                    && directiorDown) {

                    if(!allData)
                        loadNextPage(data?.get(data?.size - 1)?.id)

                }else{

                }

            }

        })

    }

    private fun loadPage(){

        mProgressBar.visibility = View.VISIBLE
        AndroidNetworking.get("http://192.168.1.66/anows/paging/index.php")
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener{

                override fun onResponse(response: JSONObject?) {

                    Log.d("JSON_REPONSE",""+response.toString())

                    val jsonArray = response?.optJSONArray("result")

                    if(jsonArray != null){

                        for(i in 0 until jsonArray.length()){

                            val jsonObject = jsonArray.optJSONObject(i)
                            data?.add(Name(
                                jsonObject.getInt("id"),
                                jsonObject.getString("name")
                            ))

                        }

                        Log.d("JSON_RESPONSE_LENGHT",""+data?.size)
                        mProgressBar.visibility = View.GONE
                        val adapter = Adapter(data)
                        adapter.notifyDataSetChanged()
                        mRecyclerView.adapter = adapter

                    }else{

                        Toast.makeText(this@MainActivity,"Empty.",Toast.LENGTH_SHORT).show()

                    }

                }

                override fun onError(anError: ANError?) {
                    mProgressBar.visibility = View.GONE
                    Toast.makeText(this@MainActivity,"Failed.",Toast.LENGTH_SHORT).show()
                }

            })

    }

    private fun loadNextPage(id:Int?){

        parcelable = mRecyclerView.layoutManager.onSaveInstanceState()
        mProgressBar2.visibility = View.VISIBLE
        AndroidNetworking.get("http://192.168.1.66/anows/paging/next_page.php?last_id=$id")
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener{

                override fun onResponse(response: JSONObject?) {

                    Log.d("JSON_REPONSE",""+response.toString())

                    val jsonArray = response?.optJSONArray("result")

                    if(jsonArray != null){

                        val dataNext = ArrayList<Name>()

                        for(i in 0 until jsonArray.length()){

                            val jsonObject = jsonArray.optJSONObject(i)
                            dataNext?.add(Name(
                                jsonObject.getInt("id"),
                                jsonObject.getString("name")
                            ))

                        }

                        Log.d("JSON_RESPONSE_LENGHT",""+dataNext?.size)
                        data.addAll(dataNext)
                        mProgressBar2.visibility = View.GONE
                        val adapter = Adapter(data)
                        adapter.notifyDataSetChanged()
                        mRecyclerView.adapter = adapter
                        mRecyclerView.layoutManager.onRestoreInstanceState(parcelable)

                    }else{

                        //Toast.makeText(this@MainActivity,"Empty.",Toast.LENGTH_SHORT).show()
                        allData = true
                        mProgressBar2.visibility = View.GONE

                    }

                }

                override fun onError(anError: ANError?) {
                    mProgressBar2.visibility = View.GONE
                    Toast.makeText(this@MainActivity,"Failed.",Toast.LENGTH_SHORT).show()
                }

            })

    }

}
