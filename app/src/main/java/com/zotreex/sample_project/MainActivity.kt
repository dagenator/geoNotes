package com.zotreex.sample_project

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.image.ImageProvider
import com.yandex.runtime.ui_view.ViewProvider
import com.zotreex.sample_project.di.MainViewModelFactory
import com.zotreex.sample_project.domain.data.models.GeoNote
import com.zotreex.sample_project.domain.data.models.Geocode
import com.zotreex.sample_project.domain.data.models.Resource
import com.zotreex.sample_project.domain.data.models.Status
import com.zotreex.sample_project.ui.MainViewModel
import javax.inject.Inject


class MainActivity : AppCompatActivity(R.layout.activity_main), UserLocationObjectListener,
    InputListener {

    @Inject
    lateinit var mainViewModelFactory: MainViewModelFactory

    private val viewModel: MainViewModel by viewModels { mainViewModelFactory }

    private val geocodeObserver: Observer<Resource<Geocode>> = Observer<Resource<Geocode>> {
        when (it.status) {
            Status.ERROR -> {}
            Status.LOADING -> {}
            Status.SUCCESS -> {
                it.data?.let { data ->
                    drawNote(data)
                }
            }
        }
    }

    val permissionsUtils = PermissionsUtils()

    private lateinit var mapView: MapView
    private var userLocationLayer: UserLocationLayer? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var editLocationIcon: ViewProvider
    private var markerTapListener: MapObjectTapListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_main)
        super.onCreate(savedInstanceState)
        (applicationContext as MainApplication).appComponent.inject(this)

        mapView = findViewById<MapView>(R.id.mapview)
        viewModel.geocodeLiveData.observe(this, geocodeObserver)
        viewModel.geoNotes.observe(this) { drawNotesOnMap(it) }

        setMap()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        permissionsUtils.askFineLocation({ subscribeLocationUpdate() }, this)

        val intent = Intent(this, NoteSevice::class.java) // Build the intent for the service
        applicationContext.startForegroundService(intent)
        viewModel.getNotes()

        editLocationIcon = ViewProvider(View(this).apply {
            @SuppressLint("UseCompatLoadingForDrawables")
            background = getDrawable(R.drawable.ic_baseline_edit_location_24)
        })
        markerTapListener = MapObjectTapListener { obj, point ->
            Log.i("NoteTap", "(${point.latitude}:${point.longitude})")
            obj.userData?.let {
                saveOrEdit(it as GeoNote)
                mapView.map.mapObjects.remove(obj)
            }
            drawNotesOnMap(viewModel.geoNotes.value.orEmpty())
            true
        }
        findViewById<ImageButton>(R.id.open_note_list_button).setOnClickListener {
            openNotesList()
        }
        findViewById<ImageButton>(R.id.notes_exit_button).setOnClickListener {
            it.visibility = View.GONE
            hideKeyboard()
        }
    }

    private fun subscribeLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            val task = fusedLocationClient.lastLocation
            task.addOnCompleteListener {
                doLocationWork(it.result.latitude, it.result.longitude)
            }
        } else {
            //permissionsUtils.requestLocationPermission(1, {subscrideLocationUpdate()}, this)
        }
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
    }

    override fun onObjectAdded(userLocationView: UserLocationView) {
        userLocationView.accuracyCircle.fillColor = Color.BLUE and -0x66000001
    }

    private fun doLocationWork(latitude: Double, longitude: Double) {
        Log.i("locationArrow", "doLocationWork: ($latitude,$longitude)")
        setCameraPosition(latitude, longitude)
    }

    private fun setCameraPosition(lat: Double, long: Double, zoom: Float = 14F) {
        Log.i("locationArrow", "setCameraPosition: ")
        mapView.let {
            it.map.isRotateGesturesEnabled = true
            it.map.move(CameraPosition(Point(lat, long), zoom, 0F, 0F))
            it.map.mapObjects.addPlacemark(
                Point(lat, long),
                ImageProvider.fromResource(this, R.drawable.ic_baseline_arrow_drop_down_circle_24)
            )
        }
        drawNotesOnMap(viewModel.geoNotes.value.orEmpty())
    }

    private fun drawNotesOnMap(geoNotes: List<GeoNote>) {
        Log.i("NoteTag", "Observer drawNotesOnMap, list.size=${geoNotes.size}")
        mapView.map.mapObjects.clear()
        geoNotes.forEach { geo ->
            drawGeoNote(geo)
        }
    }

    private fun drawGeoNote(geo: GeoNote) {
        Log.i("NoteTag", "drawNotesOnMap: ${geo.address} ")
        val marker = mapView.map.mapObjects.addPlacemark(
            Point(geo.latitude, geo.longtitude),
            editLocationIcon
        )
        marker.userData = geo
        markerTapListener?.let { marker.addTapListener(it) }
    }

    private fun setMap() {
        Log.i("locationArrow", "setMap: ")
        mapView.map.isRotateGesturesEnabled = false
        mapView.map.move(CameraPosition(Point(0.0, 0.0), 14F, 0F, 0F))
        val mapKit = MapKitFactory.getInstance()
        mapKit.resetLocationManagerToDefault()
        userLocationLayer = mapKit.createUserLocationLayer(mapView.mapWindow)
        userLocationLayer!!.isVisible = true
        userLocationLayer!!.isHeadingEnabled = true
        userLocationLayer!!.setObjectListener(this)
        mapView.map.addInputListener(this)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray,
    ) {
        when (requestCode) {
            PERMISSIONS_REQUEST_FINE_LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    permissionsUtils.askCoarseLocation({ subscribeLocationUpdate() }, this)
                }
            }
            PERMISSIONS_REQUEST_COARSE_LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    permissionsUtils.askBackground({ subscribeLocationUpdate() }, this)
                }
            }
            PERMISSIONS_REQUEST_ACCESS_BACKGROUND_LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    subscribeLocationUpdate()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onObjectRemoved(p0: UserLocationView) {
    }

    override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) {
    }


    private fun saveOrEdit(geoNote: GeoNote) {
        Log.i("GeoNote", "save or edit note: $geoNote")
        val note = findViewById<ConstraintLayout>(R.id.notes)
        note.visibility = View.VISIBLE

        findViewById<TextView>(R.id.notes_name).text = geoNote.address
        val noteInput = findViewById<EditText>(R.id.notes_edit_text)
        noteInput.setText(geoNote.note, TextView.BufferType.EDITABLE)

        findViewById<ImageButton>(R.id.notes_save_button).setOnClickListener {
            geoNote.note = noteInput.text.toString()
            if (geoNote.id == null) {
                viewModel.insertNote(geoNote)
            } else {
                viewModel.updateNote(geoNote)
            }
            hideKeyboard()
            note.visibility = View.GONE
            Toast.makeText(applicationContext, "saved", Toast.LENGTH_SHORT).show()
            viewModel.getNotes()
        }
        findViewById<ImageButton>(R.id.notes_delete_button).setOnClickListener {
            hideKeyboard()
            note.visibility = View.GONE
            viewModel.deleteNote(geoNote.address)
            Toast.makeText(applicationContext, "deleted", Toast.LENGTH_SHORT).show()

            viewModel.getNotes()
        }
    }

    private fun drawNote(geocode: Geocode) {
        viewModel.getNotes()
        val notes = viewModel.geoNotes.value
        val geoName =
            geocode.response.geoObjectCollection.featureMember[0].geoObject.metaDataProperty.geocoderMetaData.text
        val noteAtThisAddress: GeoNote? = notes?.find { x -> x.address == geoName }
        if (noteAtThisAddress != null) {
            saveOrEdit(noteAtThisAddress)
        } else {
            val pos =
                geocode.response.geoObjectCollection.featureMember[0].geoObject.point.pos.split(" ")
                    .map(String::toDouble)
            saveOrEdit(GeoNote(null, geoName, pos[1], pos[0], ""))
        }
    }

    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onMapTap(p0: Map, p1: Point) {
        Log.i("onObjectTap", "onObjectTap: ")
        viewModel.getGeocode(p1.latitude, p1.longitude)
    }

    override fun onMapLongTap(p0: Map, p1: Point) {
        TODO("Not yet implemented")
    }

    private fun openNotesList() {
        val intent = Intent(this, NotesActivity::class.java)
        startActivityForResult(intent, NotesActivity.NOTES_LIST)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null) {
            return
        }
        val address = data.getStringExtra(NotesActivity.ADDRESS_ARG)
        address?.let { findNoteOnMap(it) }
    }

    private fun findNoteOnMap(address: String) {
        viewModel.geoNotes.value?.let { it ->
            if (it.isEmpty()) return

            it.filter { x -> x.address == address }.forEach { note ->
                setCameraPosition(note.latitude, note.longtitude, 17F)
            }
        }
    }

    companion object {
        const val PERMISSIONS_REQUEST_FINE_LOCATION = 1
        const val PERMISSIONS_REQUEST_COARSE_LOCATION = 2
        const val PERMISSIONS_REQUEST_ACCESS_BACKGROUND_LOCATION = 3
    }
}
