package com.example.walkie.model

import com.google.android.gms.maps.model.LatLng
import java.sql.Date


class Walk (val date: Date, val checkPoints: Array<LatLng>, var visitedCheckPoints: Array<Boolean>, var length: Double){
}

