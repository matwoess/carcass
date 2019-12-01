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

    fun initFirebaseDB(mMap: GoogleMap) {
        Log.i(TAG, "initialize Firebase DB")
        mDBAnimalTypeRef = FirebaseDatabase.getInstance().reference.child("animalTypes")
        val animalTypeListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var t: GenericTypeIndicator<HashMap<String, AnimalType>> = object :
                    GenericTypeIndicator<HashMap<String, AnimalType>>() {}
                val value: HashMap<String, AnimalType>? = dataSnapshot.getValue(t)
                Log.d("Value is: ", value.toString())
                if (value != null) {
                    animalTypes = value.values.toSet()
                    Log.d("Animals after: ", animalTypes.toString())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Failed to read value.", error.toException())
            }
        }
        mDBAnimalTypeRef.addValueEventListener(animalTypeListener)
        mDBCarcassRef = FirebaseDatabase.getInstance().reference.child("carcasses")
        val carcassListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var t: GenericTypeIndicator<HashMap<String, CarcassDB>> = object :
                    GenericTypeIndicator<HashMap<String, CarcassDB>>() {}
                val value: HashMap<String, CarcassDB>? = dataSnapshot.getValue(t)
                Log.d("Value is: ", value.toString())
                if (value != null) {
                    for (entry in value) {
                        if (!carcasses.containsKey(entry.key)) {
                            val c = entry.value.toCarcass()
                            carcasses[c.id!!] = c
                            addMarker(c.location!!, c, mMap)
                        }
                    }
                    Log.d("Carcasses after: ", carcasses.toString())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Failed to read value.", error.toException())
            }
        }
        mDBCarcassRef.addValueEventListener(carcassListener)
    }

    fun insertDemoData(
        userPos: LatLng,
        mMap: GoogleMap
    ) {
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
                val type = animalTypes.elementAt(j - 1)
                val c = Carcass("-abc$i", type, "a dead ${type.name}", Date(), loc)

                c.id = pushCarcass(c)
                addMarker(loc, c, mMap)
            }
        }
    }

    private fun pushCarcass(carcass: Carcass): String? {
        val ref = mDBCarcassRef.push()
        ref.setValue(carcass.toCarcassDB())
        return ref.key
    }

    fun addMarker(
        latLng: LatLng,
        carcass: Carcass,
        mMap: GoogleMap
    ): Marker {
        val marker = mMap.addMarker(MarkerOptions().position(latLng).title(carcass.type!!.name));
        markers[marker] = carcass
        return marker
    }


    companion object {
        private const val TAG = "FireDBHelper"
        var carcasses: HashMap<String, Carcass> = HashMap()
        var markers: HashMap<Marker, Carcass> = HashMap()
        var animalTypes: Set<AnimalType> = HashSet()
    }

}