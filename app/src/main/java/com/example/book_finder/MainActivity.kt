package com.example.book_finder

import android.annotation.SuppressLint
import android.content.Intent
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.book_finder.adapter.BookAdapter
import com.example.book_finder.adapter.HistoryAdapter
import com.example.book_finder.api.BookService
import com.example.book_finder.databinding.ActivityMainBinding
import com.example.book_finder.model.BestSellerDto
import com.example.book_finder.model.History
import com.example.book_finder.model.SearchBookDto
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: BookAdapter
    private lateinit var historyAdapter: HistoryAdapter

    private lateinit var bookService: BookService

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        binding.searchButton.setOnClickListener {
            search(binding.searchEditText.text.toString())
        }

        setContentView(binding.root)// = setContentView(R.layout.activity_main)

        initBookRecyclerView()
        initHistoryRecyclerView()

        db = getAppDatabase(this)

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        bookService = retrofit.create(BookService::class.java)

        bookService.getBestSellerBooks(API_KEY)
            .enqueue(object: Callback<BestSellerDto> {
                override fun onResponse(
                    call: Call<BestSellerDto>,
                    response: Response<BestSellerDto>
                ) {
                    if (response.isSuccessful.not()) {
                        return
                    }
                    adapter.submitList(response.body()?.bookList.orEmpty())
                }

                override fun onFailure(call: Call<BestSellerDto>, t: Throwable) {

                    Log.e(TAG, t.toString())
                }

            })
    }

    private fun search(keyword: String) {

        bookService.getBooksByName(getString(R.string.interParkAPIKey), keyword)
            .enqueue(object: Callback<SearchBookDto> {
                override fun onResponse(call: Call<SearchBookDto>, response: Response<SearchBookDto>) {

                    historyRecyclerviewVisibility(false)
                    saveSearchKeyword(keyword)

                    if (response.isSuccessful.not()) {
                        return
                    }
                    adapter.submitList(response.body()?.bookList.orEmpty())
                }

                override fun onFailure(call: Call<SearchBookDto>, t: Throwable) {

                    historyRecyclerviewVisibility(false)
                    Log.e(TAG, t.toString())
                }

            })

    }

    private fun initBookRecyclerView() {

        adapter = BookAdapter(itemClickedListener = {
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("bookModel", it)
            startActivity(intent)
        })

        binding.bookRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.bookRecyclerView.adapter = adapter

    }

    private fun initHistoryRecyclerView() {
        historyAdapter = HistoryAdapter(historyDeleteClickedListener = {
            deleteSearchKeyword(it)
        })

        binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.historyRecyclerView.adapter = historyAdapter

        initSearchEditText()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initSearchEditText() {
        binding.searchEditText.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == MotionEvent.ACTION_DOWN) {
                search(binding.searchEditText.text.toString())
                return@setOnKeyListener true // 키이벤트 실행되었음
            }

            return@setOnKeyListener false // 키이벤트 실행 되지않음, 시스템에 정의 되어있는대로 실행
        }

        binding.searchEditText.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                historyRecyclerviewVisibility(true)
            }
            return@setOnTouchListener false
        }
    }

    private fun historyRecyclerviewVisibility(visible: Boolean) {
        if (visible) {
            Thread {
                val keywords = db.historyDao().getAll().reversed()

                runOnUiThread {
                    historyAdapter.submitList(keywords.orEmpty())
                    binding.historyRecyclerView.isVisible = visible
                }
            }.start()
        }
        else {
            binding.historyRecyclerView.isVisible = visible
        }
    }

    private fun saveSearchKeyword(keyword: String) {
        Thread {
            db.historyDao().insertHistory(History(null, keyword))
        }.start()
    }

    private fun deleteSearchKeyword(keyword: String) {
        Thread {
            db.historyDao().delete(keyword)
            historyRecyclerviewVisibility(true)
        }.start()
    }

    companion object {
        private const val BASE_URL = "https://book.interpark.com"
        private const val API_KEY = "B3B4E96F7D313F9C82C1D4C34D6F9FEF836BF045E064C9516BD83923B97836A4"
        private const val TAG = "MainActivity"
    }
}