package com.fullmob.familylocation.ui.home

import android.os.Bundle
import android.os.Debug
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.fullmob.FamilyLocationApp
import com.fullmob.familylocation.R
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.scope.scope
import org.koin.ext.scope

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val textView: TextView = root.findViewById(R.id.text_home)
        homeViewModel.text.observe(this, Observer {
            val json = familyApp().cellTowerProvider.getCellTowerIds().toJson()
            textView.text = json.toString()
            GlobalScope.launch {
                val json2 = familyApp().wifiProvider.getNetworks()
                json.put("wifi", json2.toJson())
                Log.d("Family", json.toString())
            }
        })



        return root
    }

    fun familyApp(): FamilyLocationApp {
        return activity?.application as FamilyLocationApp
    }
}