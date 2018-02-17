package com.ehab.driverbroadcast.ui

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast

import com.ehab.driverbroadcast.R
import com.ehab.driverbroadcast.model.BusReservationInformation
import com.ehab.driverbroadcast.model.ReservationsAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

import java.util.ArrayList

import butterknife.ButterKnife
import butterknife.OnClick

import com.ehab.driverbroadcast.utils.NavigationDrawerUtil.DATE_EXTRA
import com.ehab.driverbroadcast.utils.NavigationDrawerUtil.TIME_EXTRA

class TodayReservationsActivity : AppCompatActivity(), ReservationsAdapter.OnItemClickListener {

    private lateinit var mDatabaseRef: DatabaseReference
    private lateinit var mReservationsRecycler: RecyclerView
    private lateinit var mAdapter: ReservationsAdapter

    internal lateinit var date: String
    internal lateinit var time: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_today_reservations)
        ButterKnife.bind(this)
        val auth = FirebaseAuth.getInstance().currentUser
        val userDataIntent = intent
        date = userDataIntent.getStringExtra(DATE_EXTRA)
        time = userDataIntent.getStringExtra(TIME_EXTRA)

        mDatabaseRef = FirebaseDatabase.getInstance().reference.child("bus-reservations").child(date).child(time)

        mReservationsRecycler = findViewById<View>(R.id.reservations_recyclerview) as RecyclerView
        mReservationsRecycler!!.layoutManager = LinearLayoutManager(this)



    }

    override fun onStart() {
        super.onStart()
        mAdapter = ReservationsAdapter(this, mDatabaseRef)
        mReservationsRecycler!!.adapter = mAdapter
    }

    override fun onStop() {
        super.onStop()
        mAdapter!!.cleanupListener()
    }


    override fun onClick(code: String) {
        //DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("bus-reservations").child(date).child(time).child(code);
        //dbRef.setValue(null);
    }

    companion object {
        private val TAG = "Message"
    }
}
