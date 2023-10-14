package com.nbcamp.tripgo.view.weather

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nbcamp.tripgo.R

class WeatherFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_weather, container, false)
    }

    companion object {
        fun newInstance() = WeatherFragment()

        const val TAG = "WEATHER_FRAGMENT"
    }
}
