package com.mathias.android.carcass

import android.util.Log
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*
import com.mathias.android.carcass.model.AnimalType
import com.mathias.android.carcass.model.Carcass
import com.mathias.android.carcass.model.CarcassDB
import java.util.*
import kotlin.collections.HashMap

class FireDBHelper {
    private lateinit var mDBCarcassRef: DatabaseReference
    private lateinit var mDBAnimalTypeRef: DatabaseReference
    private val mMap: GoogleMap

    constructor(map: GoogleMap) {
        mMap = map
    }

    fun initFirebaseDB() {
        Log.i(TAG, "initialize Firebase DB")
        mDBAnimalTypeRef = FirebaseDatabase.getInstance().reference.child("animalTypes")
        mDBCarcassRef = FirebaseDatabase.getInstance().reference.child("carcasses")
        mDBAnimalTypeRef.addChildEventListener(AnimalTypeListener())
        mDBCarcassRef.addChildEventListener(CarcassListener())
    }

    private fun pushCarcass(carcass: Carcass): String {
        val ref = mDBCarcassRef.push()
        ref.setValue(carcass.toCarcassDB())
        return ref.key!!
    }

    fun removeCarcass(carcass: Carcass): Boolean {
        val entry = carcasses.entries.stream()
            .filter { e -> e.value == carcass }
            .findFirst()
            .orElse(null)
        return mDBCarcassRef.child(entry.key).removeValue().isSuccessful
    }

    fun addMarker(
        ref: String,
        carcass: Carcass
    ): Marker {
        val marker =
            mMap.addMarker(MarkerOptions().position(carcass.location!!).title(carcass.type!!.name))
        marker.tag = ref
        markers[marker] = carcass
        return marker
    }

    private fun removeMarker(key: String) {
        val marker = markers.keys.stream()
            .filter { m -> key == m.tag }
            .findFirst()
            .orElse(null)
        Log.i(TAG, marker.toString())
        markers.remove(marker)
        marker.remove()
    }

    fun insertDemoData(userPos: LatLng) {
        Log.i(TAG, "insertDemoData")
        val rand = Random()
        val scale = 1 / 80.0
        for (i in 1..2) {
            for (j in 1..4) {
                var addLat = rand.nextDouble() * scale
                var addLng = rand.nextDouble() * scale
                if (i % 2 == 0) addLat *= -1
                if (j % 2 == 0) addLng *= -1
                val loc = LatLng(
                    userPos.latitude + addLat,
                    userPos.longitude + addLng
                )
                val type = animalTypes.values.elementAt(j - 1)
                val c = Carcass(type, "a dead ${type.name}", Date(), loc)
                val ref = pushCarcass(c)
            }
        }
    }

    inner class AnimalTypeListener : ChildEventListener {

        override fun onCancelled(error: DatabaseError) {
            Log.w("Failed to read value.", error.toException())
        }

        override fun onChildMoved(snapshot: DataSnapshot, prevChild: String?) {
            Log.d(TAG, "onChildMoved: $snapshot, $prevChild")
        }

        override fun onChildChanged(snapshot: DataSnapshot, prevChild: String?) {
            Log.d(TAG, "onChildChanged: $snapshot, $prevChild")
            if (animalTypes.containsKey(snapshot.key)) {
                Log.i(TAG, "update entry")
                val c = snapshot.getValue(AnimalType::class.java)!!
                animalTypes[snapshot.key!!]!!.name = c.name
            }
        }

        override fun onChildAdded(snapshot: DataSnapshot, prevChild: String?) {
            Log.d(TAG, "onChildAdded: $snapshot, $prevChild")
            if (!animalTypes.containsKey(snapshot.key)) {
                Log.i(TAG, "add new entry")
                val c = snapshot.getValue(AnimalType::class.java)!!
                animalTypes[snapshot.key!!] = c
            }
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            Log.d(TAG, "onChildRemoved: $snapshot")
            if (animalTypes.containsKey(snapshot.key)) {
                Log.i(TAG, "remove entry")
                animalTypes.remove(snapshot.key)
            }
        }
    }

    inner class CarcassListener : ChildEventListener {

        override fun onCancelled(error: DatabaseError) {
            Log.w("Failed to read value.", error.toException())
        }

        override fun onChildMoved(snapshot: DataSnapshot, prevChild: String?) {
            Log.d(TAG, "onChildMoved: $snapshot, $prevChild")
        }

        override fun onChildChanged(snapshot: DataSnapshot, prevChild: String?) {
            Log.d(TAG, "onChildChanged: $snapshot, $prevChild")
            if (carcasses.containsKey(snapshot.key)) {
                Log.i(TAG, "update entry")
                val c = snapshot.getValue(CarcassDB::class.java)!!.toCarcass()
                carcasses[snapshot.key!!]!!.updateValues(c)
            }
        }

        override fun onChildAdded(snapshot: DataSnapshot, prevChild: String?) {
            Log.d(TAG, "onChildAdded: $snapshot, $prevChild")
            if (!carcasses.containsKey(snapshot.key)) {
                Log.i(TAG, "add new entry")
                val c = snapshot.getValue(CarcassDB::class.java)!!.toCarcass()
                carcasses[snapshot.key!!] = c
                addMarker(snapshot.key!!, c)
            }
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            Log.d(TAG, "onChildRemoved: $snapshot")
            if (carcasses.containsKey(snapshot.key)) {
                Log.i(TAG, "remove entry")
                val c = snapshot.getValue(CarcassDB::class.java)!!.toCarcass()
                carcasses.remove(snapshot.key)
                removeMarker(snapshot.key!!)
            }
        }
    }

    companion object {
        private const val TAG = "FireDBHelper"
        var carcasses: HashMap<String, Carcass> = HashMap()
        var markers: HashMap<Marker, Carcass> = HashMap()
        var animalTypes: HashMap<String, AnimalType> = HashMap()
    }

}