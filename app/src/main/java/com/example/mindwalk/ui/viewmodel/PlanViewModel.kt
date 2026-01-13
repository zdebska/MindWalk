import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import org.osmdroid.util.GeoPoint
import kotlin.math.cos
import kotlin.math.sin

class PlanViewModel : ViewModel() {

    var previewRoutePoints by mutableStateOf<List<GeoPoint>>(emptyList())
        private set

    fun generateFakePreviewRoute() {
        previewRoutePoints = fakeLoopRoute(
            center = GeoPoint(49.1951, 16.6068), // pick any center
            radiusMeters = 180.0,
            points = 42
        )
    }

    private fun fakeLoopRoute(center: GeoPoint, radiusMeters: Double, points: Int): List<GeoPoint> {
        val metersPerDegLat = 111_320.0
        val metersPerDegLon = 111_320.0 * cos(Math.toRadians(center.latitude))

        val dLat = radiusMeters / metersPerDegLat
        val dLon = radiusMeters / metersPerDegLon

        val out = ArrayList<GeoPoint>(points + 1)
        for (i in 0 until points) {
            val a = 2.0 * Math.PI * i / points
            val lat = center.latitude + dLat * sin(a)
            val lon = center.longitude + dLon * cos(a)
            out.add(GeoPoint(lat, lon))
        }
        out.add(out.first())
        return out
    }
}
