package com.zotreex.sample_project

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.*
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.image.ImageProvider
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

    private val viewModel: MainViewModel by viewModels<MainViewModel> { mainViewModelFactory }

    val geocodeObserver: Observer<Resource<Geocode>> = Observer<Resource<Geocode>> {
        when (it.status) {
            Status.ERROR -> {

            }
            Status.LOADING -> {

            }
            Status.SUCCESS -> {
                it.data?.let {
                    drawNote(it)
                }
            }
        }
    }

    val geoNotesObserver: Observer<List<GeoNote>> = Observer<List<GeoNote>>{
        drawNotesOnMap(it)
    }

    val permissionsUtils = PermissionsUtils()

    private var currentLocation: Location? = null

    private lateinit var mapView: MapView
    private var userLocationLayer: UserLocationLayer? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_main)
        super.onCreate(savedInstanceState)
        (applicationContext as MainApplication).appComponent.inject(this)

        mapView = findViewById<MapView>(R.id.mapview)
        viewModel.geocodeLiveData.observe(this, geocodeObserver)
        viewModel.geoNotes.observe(this, geoNotesObserver)

        setMap()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        permissionsUtils.askFineLocation({ subscribeLocationUpdate() }, this)

        val intent = Intent(this, NoteSevice::class.java) // Build the intent for the service
        applicationContext.startForegroundService(intent)
        viewModel.getNotes()

        findViewById<ImageButton>(R.id.open_note_list_button).setOnClickListener{
            openNotesList()
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

    fun setCameraPosition(lat: Double, long: Double, zoom: Float = 14F) {
        Log.i("locationArrow", "setCameraPosition: ")
        mapView.let {
            it.map.isRotateGesturesEnabled = true
            it.map.move(CameraPosition(Point(lat, long), zoom, 0F, 0F))
            it.map.mapObjects.addPlacemark(
                Point(lat, long),
                ImageProvider.fromResource(this, R.drawable.ic_baseline_arrow_drop_down_circle_24)
            )
            viewModel.geoNotes.value?.let{list->
                list.forEach { geo->
                    it.map.mapObjects.addPlacemark(
                        Point(geo.latitude, geo.longtitude),
                        ImageProvider.fromResource(this, R.drawable.ic_baseline_edit_location_24)
                    )
                }
            }

        }
        drawNotesOnMap(viewModel.geoNotes.value)
    }

    fun drawNotesOnMap(geonotes: List<GeoNote>?){
        mapView.let {
            geonotes?.forEach { geo->
                Log.i("NoteTag", "drawNotesOnMap: ${geo.address} ")
                it.map.mapObjects.addPlacemark(
                    Point(geo.latitude, geo.longtitude),
                    ImageProvider.fromResource(this, R.drawable.ic_baseline_edit_location_24)
                )
            }
        }
    }

    fun setMap() {
        Log.i("locationArrow", "setMap: ")
        mapView.let {
            it.map.isRotateGesturesEnabled = false
            it.map.move(CameraPosition(Point(0.0, 0.0), 14F, 0F, 0F))
            val mapKit = MapKitFactory.getInstance()
            mapKit.resetLocationManagerToDefault()
            userLocationLayer = mapKit.createUserLocationLayer(it.mapWindow)
            userLocationLayer!!.isVisible = true
            userLocationLayer!!.isHeadingEnabled = true
            userLocationLayer!!.setObjectListener(this)
            it.map.addInputListener(this)
        }

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {

        when (requestCode) {
            PERMISSIONS_REQUEST_FINE_LOCATION -> {
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    permissionsUtils.askCoarseLocation({ subscribeLocationUpdate() }, this)
                }
            }
            PERMISSIONS_REQUEST_COARSE_LOCATION -> {
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    permissionsUtils.askBackground({ subscribeLocationUpdate() }, this)
                }
            }
            PERMISSIONS_REQUEST_ACCESS_BACKGROUND_LOCATION -> {
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
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


    fun drawNote(geocode: Geocode) {
        viewModel.getNotes()
        val notes = viewModel.geoNotes.value
        val note = findViewById<ConstraintLayout>(R.id.notes)
        note.visibility = View.VISIBLE
        val noteTitle = findViewById<TextView>(R.id.notes_name)
        val name =
            "${geocode.response.geoObjectCollection.featureMember[0].geoObject.name}: ${geocode.response.geoObjectCollection.featureMember[0].geoObject.metaDataProperty.geocoderMetaData.text} "
        noteTitle.text = name

        val noteInput = findViewById<EditText>(R.id.notes_edit_text)
        val geoName =
            geocode.response.geoObjectCollection.featureMember[0].geoObject.metaDataProperty.geocoderMetaData.text

        var pastNote: GeoNote? = null

        val noteAtThisAddress: GeoNote? = notes?.find { x -> x.address == geoName }
        if (noteAtThisAddress != null) {
            noteInput.setText(noteAtThisAddress.note, TextView.BufferType.EDITABLE)
            pastNote = noteAtThisAddress
        } else {
            noteInput.setText("", TextView.BufferType.EDITABLE)
        }

        findViewById<ImageButton>(R.id.notes_save_button).setOnClickListener {
            if (noteInput.text.toString().length > 3) {
                val pos =
                    geocode.response.geoObjectCollection.featureMember[0].geoObject.point.pos.split(
                        " "
                    ).map { x -> x.toDouble() }

                if(pastNote ==null){
                    viewModel.insertNote(geoName, pos[1], pos[0], noteInput.text.toString())
                }
                pastNote?.let {
                    viewModel.updateNote(geoName, it.latitude, it.longtitude, noteInput.text.toString())
                }
            }
            note.visibility = View.GONE
            Toast.makeText(applicationContext, "saved", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageButton>(R.id.notes_delete_button).setOnClickListener {
            if (pastNote != null) {
                viewModel.deleteNote(pastNote.address)
            }
            noteInput.setText("", TextView.BufferType.EDITABLE)
            note.visibility = View.GONE
            Toast.makeText(applicationContext, "deleted", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageButton>(R.id.notes_exit_button).setOnClickListener {
            note.visibility = View.GONE
        }
    }

    override fun onMapTap(p0: Map, p1: Point) {
        Log.i("onObjectTap", "onObjectTap: ")
        viewModel.getGeocode(p1.latitude, p1.longitude)

    }

    override fun onMapLongTap(p0: Map, p1: Point) {
        TODO("Not yet implemented")
    }

    private fun openNotesList(){
        val intent = Intent(this, NotesActivity::class.java)
        startActivityForResult(intent, NotesActivity.NOTES_LIST)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data == null) {
            return
        }
        val address = data.getStringExtra(NotesActivity.ADDRESS_ARG)
        address?.let { findNoteOnMap(it) }

    }

    private fun findNoteOnMap(address: String){
        viewModel.geoNotes.value?.let { it ->
            if(it.isEmpty()) return

            it.filter { x-> x.address == address }.forEach {note->
                setCameraPosition(note.latitude, note.longtitude, 17F)
            }
        }
    }

    companion object {
        val PERMISSIONS_REQUEST_FINE_LOCATION = 1
        val PERMISSIONS_REQUEST_COARSE_LOCATION = 2
        val PERMISSIONS_REQUEST_ACCESS_BACKGROUND_LOCATION = 3
    }
}
