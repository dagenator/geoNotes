package com.zotreex.sample_project

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zotreex.sample_project.di.MainViewModelFactory
import com.zotreex.sample_project.domain.data.models.GeoNote
import com.zotreex.sample_project.ui.MainViewModel
import com.zotreex.sample_project.ui.adapter.NotesAdapter
import javax.inject.Inject


class NotesActivity : AppCompatActivity(R.layout.fragment_notes_list) {

    @Inject
    lateinit var mainViewModelFactory: MainViewModelFactory

    private val viewModel: MainViewModel by viewModels { mainViewModelFactory }

    private val geoNotesObserver: Observer<List<GeoNote>> = Observer<List<GeoNote>> {
        setRecycle(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.fragment_notes_list)
        super.onCreate(savedInstanceState)
        (applicationContext as MainApplication).appComponent.inject(this)

        viewModel.geoNotes.observe(this, geoNotesObserver)
        viewModel.getNotes()
    }

    private fun onFinish(address: String) {
        val intent = Intent()
        intent.putExtra(ADDRESS_ARG, address)
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun setRecycle(list: List<GeoNote>) {
        val adapter = NotesAdapter(list.toTypedArray()) { x -> onFinish(x) }
        val recycler = findViewById<RecyclerView>(R.id.list)
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(this)
    }

    companion object {
        const val ADDRESS_ARG = "ADDRESS"
        const val NOTES_LIST = 1
    }
}