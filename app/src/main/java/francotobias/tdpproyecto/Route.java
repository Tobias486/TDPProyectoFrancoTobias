package francotobias.tdpproyecto;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Route {
	protected List<Section> routeSectionGo, routeSectionReturn;
	protected List<LatLng> routeGo, routeReturn;    // Se pueden computar
	protected Line line;
	protected boolean validStops = true;
	protected List<Stop> stops;     // Se puede computar
	protected static double MIN_DISTANCE_THRESHOLD = 250;   // Podria ser menor si la data fuera mejor


	public Route(Line l, List<LatLng> rGo, List<LatLng> rReturn) {
		line = l;
		l.setRoute(this);
		routeGo = rGo;
		routeReturn = rReturn;
		routeSectionGo = new LinkedList<>();
		routeSectionReturn = new LinkedList<>();

		Iterator<LatLng> routeIterator = rGo.iterator();
		LatLng end, start = routeIterator.next();

		while (routeIterator.hasNext()) {
			end = routeIterator.next();
			routeSectionGo.add(new Section( this, start, end, true));
			start = end;
		}

		routeIterator = rReturn.iterator();
		start = routeIterator.next();

		while (routeIterator.hasNext()) {
			end = routeIterator.next();
			routeSectionReturn.add(new Section( this, start, end, false));
			start = end;
		}
	}

	public Line getLine() {
		return line;
	}

	public void setLine(Line l) {
		line = l;
	}

	// Se puede computar para no tener que guardar la lista
	public List<Stop> getStops() {
		return stops;
	}


	/**
	 * La lista de entrada contiene las paradas de ida en el sentido del recorrido (de ida)
	 * y las de vuelta en sentido opuesto al recorrido (arrancan al final de trayecto de vuelta)
	 */
	public void setStops(List<Stop> stops) {
		if (stops.size() == 0) {
			Log.d("Sin paradas", line.lineID);
			validStops = false;
			return;
		}

		List<Stop> stopsGo = new LinkedList<>();
		List<Stop> stopsRet = new LinkedList<>();

		for (Stop stop : stops)
			if (stop.isGo)
				stopsGo.add(stop);
			else
				stopsRet.add(0, stop);      // Las paradas de vuelta vienen invertidas

		this.stops = new LinkedList<>();
		this.stops.addAll(stopsGo);
		this.stops.addAll(stopsRet);

		addStopsToSections(stopsGo);
		addStopsToSections(stopsRet);
	}


	private void addStopsToSections(List<Stop> stops) {
		List<Section> sections = stops.get(0).isGo ? routeSectionGo : routeSectionReturn;
		Iterator<Section> sectionIterator = sections.iterator();
		Iterator<Stop> stopIterator = stops.iterator();
		float distance, lastDistance = -1;
		double distanceToLine = -1;
		Section section = sections.get(0);
		Location stopLocation, epLocation = BusManager.latLngToLocation(section.endPoint, "");
		Stop stop;

		while (stopIterator.hasNext()) {
			stop = stopIterator.next();
			stopLocation = BusManager.latLngToLocation(stop.location, "");
			distance = stopLocation.distanceTo(epLocation);         // Mas barato

			if (distance > lastDistance)
				while (sectionIterator.hasNext()) {
					section = sectionIterator.next();
					distanceToLine = PolyUtil.distanceToLine(stop.location, section.startPoint, section.endPoint);

					if (distanceToLine <= MIN_DISTANCE_THRESHOLD) {
						epLocation = BusManager.latLngToLocation(section.endPoint, "");
						distance = stopLocation.distanceTo(epLocation);
						break;
					}
				}

				// Debugging
				if (distanceToLine > MIN_DISTANCE_THRESHOLD && validStops) {
					Log.d("Paradas defasadas", line.lineID);
					validStops = false;
					//return;
				}

			lastDistance = distance;
			section.addStop(stop);
		}
	}


	// Mover a Line?
	private Section[] getClosestSectctions(LatLng latLng) {
		double dist, minDist = 10000;
		Section[] toRetrun = new Section[2];

		for (Section section : routeSectionGo) {
			dist = PolyUtil.distanceToLine(latLng, section.startPoint, section.endPoint);
			if (dist < minDist) {
				minDist = dist;
				toRetrun[0] = section;
				if (dist < MIN_DISTANCE_THRESHOLD)
					break;
			}
		}

		minDist = 10000;
		for (Section section : routeSectionReturn) {
			dist = PolyUtil.distanceToLine(latLng, section.startPoint, section.endPoint);
			if (dist < minDist) {
				minDist = dist;
				toRetrun[1] = section;
				if (dist < MIN_DISTANCE_THRESHOLD)
					break;
			}
		}

		return toRetrun;
	}


	public List<Section> getSectionsGo() {
		return routeSectionGo;
	}


	public List<Section> getSectionsReturn() {
		return routeSectionReturn;
	}


	public List<LatLng> getGo() {
		return routeGo;
	}

	public List<LatLng> getReturn() {
		return routeReturn;
	}

	// Needed to comupte travel distance
	public boolean validStops() {
		return validStops && stops != null;
	}

}
