package fr.umontpellier.campus.service;

import fr.umontpellier.campus.domain.Batiment;
import org.springframework.stereotype.Service;

@Service
public class DistanceService {
  private static final double EARTH_RADIUS_KM = 6371.0;

  public Double distanceKm(Batiment a, Batiment b) {
    if (a == null || b == null) {
      return null;
    }
    if (a.getLatitude() == null || a.getLongitude() == null
        || b.getLatitude() == null || b.getLongitude() == null) {
      return null;
    }

    return distanceKm(a.getLatitude(), a.getLongitude(), b.getLatitude(), b.getLongitude());
  }

  public Double distanceKm(double lat1, double lon1, double lat2, double lon2) {
    double rLat1 = Math.toRadians(lat1);
    double rLon1 = Math.toRadians(lon1);
    double rLat2 = Math.toRadians(lat2);
    double rLon2 = Math.toRadians(lon2);

    double dLat = rLat2 - rLat1;
    double dLon = rLon2 - rLon1;

    double sinLat = Math.sin(dLat / 2.0);
    double sinLon = Math.sin(dLon / 2.0);
    double h = sinLat * sinLat + Math.cos(rLat1) * Math.cos(rLat2) * sinLon * sinLon;
    double c = 2.0 * Math.atan2(Math.sqrt(h), Math.sqrt(1.0 - h));

    return EARTH_RADIUS_KM * c;
  }
}
