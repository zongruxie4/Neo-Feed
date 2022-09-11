package ua.itaysonlab.homefeeder.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.preference.Preference
import com.saulhdev.feeder.BuildConfig
import com.saulhdev.feeder.R
import ua.itaysonlab.homefeeder.fragments.base.FixedPreferencesFragment

class AboutFragment : FixedPreferencesFragment() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_about, rootKey)

        findPreference<Preference>("about_app")!!.summary = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"

        val tg = findPreference<Preference>("about_tg")!!
        val tgdev = findPreference<Preference>("about_tg_dev")!!
        val git = findPreference<Preference>("about_source")!!

        tg.summary = "@homefeeder"
        tg.setOnPreferenceClickListener {
            openLink(requireActivity(), "https://t.me/neo_launcher")
            true
        }
        tgdev.summary = "Saul Henriquez"
        tgdev.setOnPreferenceClickListener {
            openLink(requireActivity(), "https://github.com/saulhdev")
            true
        }
        git.setOnPreferenceClickListener {
            openLink(requireActivity(), "https://github.com/saulhdev/OmegaFeeder/")
            true
        }
    }

    private fun openLink(context: Context, url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}
