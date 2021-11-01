package com.luk.saucenao

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import org.jsoup.Jsoup
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class ResultsActivity : AppCompatActivity() {
    private val executor: Executor = Executors.newFixedThreadPool(4)
    private val handler = Handler(Looper.getMainLooper())
    private var clipboardManager: ClipboardManager? = null
    private var results: Results? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        intent.extras?.let {
            it.getString(EXTRA_RESULTS)?.let { results ->
                this.results = Results(Jsoup.parse(results))
            }
        }

        val resultsRecyclerView = findViewById<ResultsRecyclerView>(R.id.results)
        resultsRecyclerView.layoutManager = LinearLayoutManager(this)
        resultsRecyclerView.emptyView = findViewById(R.id.no_results)
        resultsRecyclerView.adapter = ResultsAdapter(results!!.results)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return true
    }

    inner class ResultsAdapter(private val results: ArrayList<Results.Result>) :
        RecyclerView.Adapter<ResultsAdapter.ResultsViewHolder>() {
        inner class ResultsViewHolder(view: View) : ViewHolder(view), View.OnClickListener,
            OnLongClickListener {
            var extUrls: ArrayList<String>? = null
            var thumbnail: ImageView = view.findViewById(R.id.thumbnail)
            var metadata: TextView = view.findViewById(R.id.metadata)
            var similarity: TextView = view.findViewById(R.id.similarity)
            var title: TextView = view.findViewById(R.id.title)

            override fun onClick(view: View) {
                if (extUrls == null || extUrls!!.isEmpty()) {
                    return
                }
                if (extUrls!!.size == 1) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(extUrls!!.first())))
                    return
                }
                val popupMenu = PopupMenu(view.context, view)
                extUrls!!.indices.forEach { popupMenu.menu.add(0, it, it, extUrls!![it]) }
                popupMenu.show()
                popupMenu.setOnMenuItemClickListener {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(extUrls!![it.itemId])))
                    true
                }
            }

            override fun onLongClick(view: View): Boolean {
                val popupMenu = PopupMenu(view.context, view)
                popupMenu.inflate(R.menu.card_long_press)
                popupMenu.show()
                popupMenu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.copy_to_clipboard_item -> {
                            clipboardManager!!.setPrimaryClip(ClipData.newPlainText("", title.text))
                            Toast.makeText(
                                view.context,
                                getString(R.string.title_copied_to_clipboard),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        R.id.share_item -> {
                            val intent = Intent(Intent.ACTION_SEND)
                            intent.type = "text/plain"
                            intent.putExtra(Intent.EXTRA_TEXT, title.text)
                            startActivity(
                                Intent.createChooser(
                                    intent,
                                    getString(R.string.abc_shareactionprovider_share_with)
                                )
                            )
                        }
                    }
                    true
                }
                return false
            }

            init {
                val cardResult = view.findViewById<View>(R.id.card_result)
                cardResult.setOnClickListener(this)
                cardResult.setOnLongClickListener(this)
            }
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ResultsViewHolder {
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.card_result, viewGroup, false)
            return ResultsViewHolder(view)
        }

        override fun onBindViewHolder(resultsViewHolder: ResultsViewHolder, i: Int) {
            val result = results[i]

            // Load thumbnail in executor
            executor.execute {
                try {
                    val bitmap = BitmapFactory.decodeStream(
                        URL(result.thumbnail).openStream()
                    )
                    handler.post { resultsViewHolder.thumbnail.setImageBitmap(bitmap) }
                } catch (e: MalformedURLException) {
                    Log.e(LOG_TAG, "Invalid thumbnail URL", e)
                } catch (e: IOException) {
                    Log.e(LOG_TAG, "Unable to load thumbnail", e)
                }
            }

            // Load index specific data
            result.title?.let {
                val titleAndMetadata = it.split("\n", limit = 2).toTypedArray()
                if (titleAndMetadata.isNotEmpty()) {
                    resultsViewHolder.title.text = titleAndMetadata[0]
                    if (titleAndMetadata.size == 2) {
                        result.columns.add(0, titleAndMetadata[1])
                    }
                }
            }
            resultsViewHolder.metadata.text = result.columns.joinToString("\n")

            // Load global data
            resultsViewHolder.extUrls = result.extUrls
            resultsViewHolder.similarity.text = result.similarity
        }

        override fun getItemCount(): Int {
            return results.size
        }
    }

    companion object {
        private val LOG_TAG = ResultsActivity::class.java.simpleName
        const val EXTRA_RESULTS = "extra_results"
    }
}