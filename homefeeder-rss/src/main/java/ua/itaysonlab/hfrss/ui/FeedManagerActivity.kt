package ua.itaysonlab.hfrss.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.prof.rssparser.Channel
import com.prof.rssparser.Parser
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import ua.itaysonlab.hfrss.R
import ua.itaysonlab.hfrss.data.SavedFeedModel
import ua.itaysonlab.hfrss.databinding.FeedManagerBinding
import ua.itaysonlab.hfrss.pref.HFPluginPreferences

class FeedManagerActivity: AppCompatActivity(), CoroutineScope by MainScope() {
    var list = listOf<SavedFeedModel>()
    private lateinit var binding: FeedManagerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.feed_manager)

        binding = FeedManagerBinding.inflate(layoutInflater)

        findViewById<ExtendedFloatingActionButton>(R.id.add_feed).setOnClickListener {
            NewFeedBottomSheet { url ->
                launch {
                    var data: Channel? = null

                    withContext(Dispatchers.Default) {
                        val parser = Parser(OkHttpClient())
                        try {
                            data = parser.getChannel(url)
                        } catch (_: Exception) {

                        }
                    }

                    data ?: run {
                        Toast.makeText(this@FeedManagerActivity, "URL is not a RSS feed!", Toast.LENGTH_LONG).show()
                        return@launch
                    }
                    val title = data!!.title ?: "Unknown"
                    HFPluginPreferences.add(SavedFeedModel(
                        title,
                        data!!.description ?: "",
                        url,
                        data!!.image?.url ?: ""
                    ))

                    reload()
                }
            }.show(supportFragmentManager, null)
        }

        reload()
    }

    private fun reload() {
        list = HFPluginPreferences.parsedFeedList
        binding.feeds.apply {
            layoutManager = LinearLayoutManager(this@FeedManagerActivity)
            adapter = SourceAdapter()
        }
    }

    inner class SourceAdapter: RecyclerView.Adapter<SourceAdapter.SourceVH>() {
        private lateinit var inflater: LayoutInflater

        inner class SourceVH(itemView: View): RecyclerView.ViewHolder(itemView)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SourceVH {
            if (!::inflater.isInitialized) inflater = LayoutInflater.from(parent.context)
            return SourceVH(inflater.inflate(R.layout.feed_item, parent, false))
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: SourceVH, position: Int) {
            val item = list[position]
            val appIcon = findViewById<ImageView>(R.id.app_icon)

            holder.itemView.apply {
                if (item.pic_url.isBlank()) {
                    appIcon.visibility = View.GONE
                } else {
                    appIcon.visibility = View.VISIBLE
                    appIcon.load(item.pic_url)
                }

                findViewById<TextView>(R.id.plugin_name).text = item.name
                findViewById<TextView>(R.id.plugin_author).text = item.feed_url
                findViewById<TextView>(R.id.plugin_desc).text = item.desc

                findViewById<ImageView>(R.id.plugin_status).setOnClickListener {
                    AlertDialog.Builder(it.context).apply {
                        setTitle(R.string.remove_title)
                        setMessage(resources.getString(R.string.remove_desc, item.name))
                        setNeutralButton(R.string.remove_action_nope, null)
                        setPositiveButton(R.string.remove_action_yes) { _, _ ->
                            HFPluginPreferences.remove(item)
                            reload()
                        }
                    }.show()
                }
            }
        }
    }
}