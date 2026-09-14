/*
 * This file is part of Neo Feed
 * Copyright (c) 2026   NeoApplications Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saulhdev.feeder.ui.weather

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import com.google.android.libraries.gsa.d.a.DialogListeners
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.saulhdev.feeder.R
import com.saulhdev.feeder.data.weather.WeatherCode
import com.saulhdev.feeder.data.weather.WeatherData
import com.saulhdev.feeder.manager.service.OverlayView
import kotlin.math.roundToInt

object WeatherDialogHelper {

    fun showDetails(context: Context, weather: WeatherData, anchorView: View? = null) {
        val themedContext = ContextThemeWrapper(context, R.style.AppTheme)
        val view = LayoutInflater.from(themedContext).inflate(R.layout.dialog_weather_details, null)

        view.findViewById<TextView>(R.id.dialog_weather_city).text = weather.cityName
        view.findViewById<ImageView>(R.id.dialog_weather_icon)
            .setImageResource(WeatherCode.getIconRes(weather.weatherCode, weather.isDay))
        view.findViewById<TextView>(R.id.dialog_weather_temp).text =
            "${weather.temperature.roundToInt()}${weather.unit}"
        view.findViewById<TextView>(R.id.dialog_weather_condition).setText(
            WeatherCode.getDescriptionRes(weather.weatherCode)
        )
        view.findViewById<TextView>(R.id.dialog_weather_feels_like).text =
            themedContext.getString(
                R.string.weather_feels_like,
                "${weather.apparentTemperature.roundToInt()}${weather.unit}"
            )

        view.findViewById<TextView>(R.id.dialog_weather_humidity).text = "${weather.humidity}%"
        view.findViewById<TextView>(R.id.dialog_weather_wind).text =
            "${weather.windSpeed.roundToInt()} km/h"
        view.findViewById<TextView>(R.id.dialog_weather_precip).text =
            "${weather.precipitation} mm"
        view.findViewById<TextView>(R.id.dialog_weather_range).text =
            "${weather.maxTemp.roundToInt()}° / ${weather.minTemp.roundToInt()}°"

        val hourlyContainer = view.findViewById<LinearLayout>(R.id.dialog_hourly_container)
        val inflater = LayoutInflater.from(themedContext)
        for (item in weather.hourly) {
            val itemLayout = inflater.inflate(R.layout.item_weather_hourly, hourlyContainer, false)
            itemLayout.findViewById<TextView>(R.id.hourly_time).text = item.time
            itemLayout.findViewById<ImageView>(R.id.hourly_icon)
                .setImageResource(WeatherCode.getIconRes(item.weatherCode, item.isDay))
            itemLayout.findViewById<TextView>(R.id.hourly_temp).text =
                "${item.temperature.roundToInt()}${weather.unit}"
            hourlyContainer.addView(itemLayout)
        }

        try {
            val dialog = MaterialAlertDialogBuilder(themedContext)
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .create()

            val overlay = context as? OverlayView
            val token = anchorView?.windowToken
                ?: overlay?.window?.attributes?.token
                ?: overlay?.windowView?.windowToken

            dialog.window?.let { window ->
                if (token != null) {
                    val lp = window.attributes
                    lp.token = token
                    lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL
                    window.attributes = lp
                }
            }

            if (context is DialogListeners) {
                dialog.setOnShowListener(context)
                dialog.setOnDismissListener(context)
            }

            dialog.show()
        } catch (e: Exception) {
            Log.e("WeatherDialogHelper", "Error showing weather details dialog", e)
        }
    }
}
