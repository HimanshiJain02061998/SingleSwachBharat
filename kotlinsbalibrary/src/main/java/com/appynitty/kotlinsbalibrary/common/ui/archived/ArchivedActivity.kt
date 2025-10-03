package com.appynitty.kotlinsbalibrary.common.ui.archived

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.appynitty.kotlinsbalibrary.R
import com.appynitty.kotlinsbalibrary.common.utils.BackBtnPressedUtil
import com.appynitty.kotlinsbalibrary.common.utils.LanguageConfig
import com.appynitty.kotlinsbalibrary.common.utils.datastore.LanguageDataStore
import com.appynitty.kotlinsbalibrary.common.utils.datastore.model.AppLanguage
import com.appynitty.kotlinsbalibrary.databinding.ActivityArchivedBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ArchivedActivity : AppCompatActivity() {

    private val viewModel: ArchivedViewModel by viewModels()
    private lateinit var binding: ActivityArchivedBinding
    private lateinit var adapter: ArchivedAdapter
    private var languageId: String? = null

    //multi language functionality
    override fun attachBaseContext(newBase: Context?) {

        var context: Context? = newBase
        if (newBase != null) {
            val languageDataStore = LanguageDataStore(newBase.applicationContext)
            val appLanguage = languageDataStore.currentLanguage
            context = newBase.let { LanguageConfig.changeLanguage(it, appLanguage.languageId) }
        }
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityArchivedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initVars()
        initToolbar()
        subscribeLiveData()
        BackBtnPressedUtil.handleBackBtnPressed(this, this, this)
    }

    private fun initToolbar() {
        binding.toolbar.title = resources.getString(R.string.title_activity_archived)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> BackBtnPressedUtil.exitOnBackPressed(this)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initVars() {
        languageId = intent.getStringExtra("languageId")

        adapter = ArchivedAdapter(languageId)

        binding.archivedRecyclerView.setHasFixedSize(true)
        binding.archivedRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.archivedRecyclerView.adapter = adapter
    }

    private fun subscribeLiveData() {

        viewModel.archivedLiveData.observe(this) {
            Log.d("TAG", "subscribeLiveData: $it")
            if (it.isEmpty()) {
                binding.showErrorOfflineData.visibility = View.VISIBLE
                binding.lineView.visibility = View.GONE
            } else {
                binding.lineView.visibility = View.VISIBLE
                binding.showErrorOfflineData.visibility = View.GONE

                Log.d("TAG", "subscribeLiveData: $it")
                adapter.submitList(null)
                adapter.submitList(it)
            }
        }
    }

}