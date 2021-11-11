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
    private val clipboardManager by lazy { getSystemService(CLIPBOARD_SERVICE) as ClipboardManager }
    private val executor: Executor = Executors.newFixedThreadPool(4)
    private val handler = Handler(Looper.getMainLooper())
    private var results: Results? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        intent.extras?.let {
            it.getString(EXTRA_RESULTS)?.let { results ->
                this.results = Results(Jsoup.parse(results))
            }
        }

        findViewById<ResultsRecyclerView>(R.id.results).let {
            it.layoutManager = LinearLayoutManager(this)
            it.emptyView = findViewById(R.id.no_results)
            it.adapter = ResultsAdapter(results!!.results)
        }
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
            var extUrls = listOf<String>()

            val thumbnail = view.findViewById<ImageView>(R.id.thumbnail)!!
            val metadata = view.findViewById<TextView>(R.id.metadata)!!
            val similarity = view.findViewById<TextView>(R.id.similarity)!!
            val title = view.findViewById<TextView>(R.id.title)!!

            override fun onClick(view: View) {
                if (extUrls.isEmpty()) {
                    return
                }
                if (extUrls.size == 1) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(extUrls.first())))
                    return
                }
                PopupMenu(view.context, view).let {
                    extUrls.forEachIndexed { key, value -> it.menu.add(0, key, key, value) }
                    it.setOnMenuItemClickListener { item ->
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(extUrls[item.itemId])))
                        true
                    }
                    it.show()
                }
            }

            override fun onLongClick(view: View): Boolean {
                PopupMenu(view.context, view).let {
                    it.inflate(R.menu.card_long_press)
                    it.setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.copy_to_clipboard_item -> {
                                clipboardManager.setPrimaryClip(
                                    ClipData.newPlainText("", title.text)
                                )
                                Toast.makeText(
                                    view.context,
                                    R.string.title_copied_to_clipboard,
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
                    it.show()
                }
                return false
            }

            init {
                view.findViewById<View>(R.id.card_result).let {
                    it.setOnClickListener(this)
                    it.setOnLongClickListener(this)
                }
            }
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ResultsViewHolder {
            return ResultsViewHolder(
                LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.card_result, viewGroup, false)
            )
        }

        override fun onBindViewHolder(resultsViewHolder: ResultsViewHolder, i: Int) {
            val result = results[i]

            // Load thumbnail in executor
            executor.execute {
                try {
                    val bitmap = BitmapFactory.decodeStream(URL(result.thumbnail).openStream())
                    handler.post { resultsViewHolder.thumbnail.setImageBitmap(bitmap) }
                } catch (e: MalformedURLException) {
                    Log.e(LOG_TAG, "Invalid thumbnail URL", e)
                } catch (e: IOException) {
                    Log.e(LOG_TAG, "Unable to load thumbnail", e)
                }
            }

            // Load index specific data
            result.title.let {
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

        override fun getItemCount() = results.size
    }

    companion object {
        private val LOG_TAG = ResultsActivity::class.java.simpleName

        const val EXTRA_RESULTS = "extra_results"
    }
}