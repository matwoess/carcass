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
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class FireDBHelper {
    private lateinit var mDBCarcassRef: DatabaseReference
    private lateinit var mDBAnimalTypeRef: DatabaseReference

    fun initFirebaseDB(mMap: GoogleMap) {
        mDBAnimalTypeRef = FirebaseDatabase.getInstance().reference.child("animalTypes")
        val animalTypeListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var t: GenericTypeIndicator<ArrayList<AnimalType>> = object :
                    GenericTypeIndicator<ArrayList<AnimalType>>() {}
                val value: ArrayList<AnimalType>? = dataSnapshot.getValue(t)
                Log.d("Value is: ", value.toString())
                if (value != null) {
                    animalTypes = value.toSet()
                    Log.d("Value is: ", animalTypes.toString())
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
                var t: GenericTypeIndicator<ArrayList<CarcassDB>> = object :
                    GenericTypeIndicator<ArrayList<CarcassDB>>() {}
                val value: ArrayList<CarcassDB>? = dataSnapshot.getValue(t)
                Log.d("Value is: ", value.toString())
                if (value != null) {
                    var carcs: ArrayList<Carcass> = ArrayList()
                    for (carc in value) {
                        val c = carc.toCarcass()
                        carcs.add(c)
                        carcasses[addMarker(c.location!!, c, carcasses, mMap)] = c
                    }
                    Log.d("Value is: ", carcs.toString())
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
        carcasses: java.util.HashMap<Marker, Carcass>,
        mMap: GoogleMap
    ) {
        val rand = Random()
        val scale = 1 / 80.0
        for (i in 0..1) {
            val loc1 = LatLng(
                userPos.latitude + rand.nextDouble() * scale,
                userPos.longitude + rand.nextDouble() * scale
            )
            val loc2 = LatLng(
                userPos.latitude + rand.nextDouble() * scale,
                userPos.longitude - rand.nextDouble() * scale
            )
            val loc3 = LatLng(
                userPos.latitude - rand.nextDouble() * scale,
                userPos.longitude - rand.nextDouble() * scale
            )
            val loc4 = LatLng(
                userPos.latitude - rand.nextDouble() * scale,
                userPos.longitude + rand.nextDouble() * scale
            )
            val carcass1 =
                Carcass(
                    (i + 1) * 1L,
                    AnimalType("Hedgehog"), "a dead hedgehog", Date(), loc1
                )
            carcasses[addMarker(loc1, carcass1, carcasses, mMap)] = carcass1
            val carcass2 =
                Carcass(
                    (i + 1) * 2L,
                    AnimalType("Deer"), "a dead deer", Date(), loc2
                )
            carcasses[addMarker(loc2, carcass2, carcasses, mMap)] = carcass2
            val carcass3 =
                Carcass(
                    (i + 1) * 3L,
                    AnimalType("Squirrel"), "a dead squirrel", Date(), loc3
                )
            carcasses[addMarker(loc3, carcass3, carcasses, mMap)] = carcass3
            val carcass4 =
                Carcass(
                    (i + 1) * 4L,
                    AnimalType("Bird"), "a dead bird", Date(), loc4
                )
            carcasses[addMarker(loc4, carcass4, carcasses, mMap)] = carcass4
        }
    }

    fun addMarker(
        latLng: LatLng,
        carcass: Carcass,
        carcasses: java.util.HashMap<Marker, Carcass>,
        mMap: GoogleMap
    ): Marker {
        val marker = mMap.addMarker(MarkerOptions().position(latLng).title(carcass.type!!.name));
        carcasses[marker] = carcass
        return marker
    }


    companion object {
        var carcasses: HashMap<Marker, Carcass> = HashMap()
        var animalTypes: Set<AnimalType> = HashSet()
    }

}