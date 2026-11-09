import { useEffect, useRef, useState } from 'react';
import { Circle, CircleMarker, MapContainer, Popup, TileLayer } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png'
});

let mapsApiPromise;

function loadGoogleMaps(apiKey) {
  if (window.google?.maps) return Promise.resolve(window.google.maps);
  if (!apiKey) return Promise.reject(new Error('Google Maps API key is not configured.'));

  if (!mapsApiPromise) {
    mapsApiPromise = new Promise((resolve, reject) => {
      const callbackName = '__lifeLinkGoogleMapsLoaded';
      const script = document.createElement('script');
      const previousAuthFailure = window.gm_authFailure;
      let settled = false;
      const restoreGlobals = () => {
        delete window[callbackName];
        if (window.gm_authFailure === authFailure) {
          if (previousAuthFailure) window.gm_authFailure = previousAuthFailure;
          else delete window.gm_authFailure;
        }
      };
      const fail = error => {
        if (settled) return;
        settled = true;
        restoreGlobals();
        script.remove();
        mapsApiPromise = null;
        reject(error);
      };
      const authFailure = () => {
        previousAuthFailure?.();
        fail(new Error('Google Maps rejected the API key.'));
      };

      window[callbackName] = () => {
        if (settled) return;
        const maps = window.google?.maps;
        if (!maps) {
          fail(new Error('Google Maps did not initialize.'));
          return;
        }
        settled = true;
        restoreGlobals();
        resolve(maps);
      };
      window.gm_authFailure = authFailure;
      script.async = true;
      script.onerror = () => fail(new Error('Google Maps could not be loaded.'));
      script.src = `https://maps.googleapis.com/maps/api/js?key=${encodeURIComponent(apiKey)}&loading=async&callback=${callbackName}`;
      document.head.appendChild(script);
    });
  }

  return mapsApiPromise;
}

const toLatLng = point => Array.isArray(point)
  ? { lat: Number(point[0]), lng: Number(point[1]) }
  : { lat: Number(point.lat), lng: Number(point.lng) };

export default function GoogleMapView({ center, markers = [], circles = [], zoom = 12, style }) {
  const mapElement = useRef(null);
  const mapInstance = useRef(null);
  const mapCenter = toLatLng(center);
  const apiKey = import.meta.env.VITE_GOOGLE_MAPS_API_KEY?.trim();
  const [mode, setMode] = useState(apiKey ? 'google-loading' : 'leaflet');

  useEffect(() => {
    let cancelled = false;
    if (!apiKey) {
      setMode('leaflet');
      return undefined;
    }

    setMode('google-loading');
    loadGoogleMaps(apiKey)
      .then(() => { if (!cancelled) setMode('google'); })
      .catch(() => { if (!cancelled) setMode('leaflet'); });
    return () => { cancelled = true; };
  }, [apiKey]);

  useEffect(() => {
    if (mode !== 'google' || !mapElement.current || !window.google?.maps) return undefined;
    const map = new window.google.maps.Map(mapElement.current, {
      center: mapCenter,
      zoom,
      mapTypeControl: false,
      streetViewControl: false,
      fullscreenControl: true
    });
    mapInstance.current = map;
    return () => { mapInstance.current = null; };
  }, [mapCenter.lat, mapCenter.lng, mode, zoom]);

  useEffect(() => {
    const map = mapInstance.current;
    if (mode !== 'google' || !map || !window.google?.maps) return undefined;
    const maps = window.google.maps;
    const renderedMarkers = markers.map(item => {
      const marker = new maps.Marker({
        map,
        position: toLatLng(item.position),
        title: item.title || '',
        icon: {
          path: maps.SymbolPath.CIRCLE,
          scale: 8,
          fillColor: item.color || '#e11d48',
          fillOpacity: 1,
          strokeColor: '#ffffff',
          strokeWeight: 2
        }
      });
      if (item.label) {
        const content = document.createElement('div');
        content.textContent = item.label;
        const info = new maps.InfoWindow({ content });
        marker.addListener('click', () => info.open({ map, anchor: marker }));
      }
      return marker;
    });
    const renderedCircles = circles.map(item => new maps.Circle({
      map,
      center: toLatLng(item.center),
      radius: item.radius,
      strokeColor: item.color || '#ef4444',
      strokeOpacity: 0.8,
      strokeWeight: 1,
      fillColor: item.color || '#ef4444',
      fillOpacity: 0.1
    }));

    return () => {
      renderedMarkers.forEach(marker => marker.setMap(null));
      renderedCircles.forEach(circle => circle.setMap(null));
    };
  }, [circles, markers, mode]);

  useEffect(() => {
    if (mapInstance.current && mode === 'google') mapInstance.current.setCenter(mapCenter);
  }, [mapCenter.lat, mapCenter.lng, mode]);

  return <div style={{ position: 'relative', width: '100%', height: '100%', minHeight: 260, ...style }}>
    {mode === 'google-loading'
      ? <div role="status" style={{ display: 'grid', placeItems: 'center', height: '100%', color: '#475569' }}>Loading map…</div>
      : mode === 'google'
        ? <div ref={mapElement} style={{ width: '100%', height: '100%' }} />
        : <MapContainer center={[mapCenter.lat, mapCenter.lng]} zoom={zoom} scrollWheelZoom style={{ width: '100%', height: '100%' }}>
          <TileLayer
            url="https://tile.openstreetmap.de/{z}/{x}/{y}.png"
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap contributors</a> · Tiles <a href="https://www.openstreetmap.de/">OpenStreetMap.de</a>'
          />
          {circles.map((circle, index) => <Circle
            key={`circle-${index}`}
            center={[toLatLng(circle.center).lat, toLatLng(circle.center).lng]}
            radius={circle.radius}
            pathOptions={{ color: circle.color || '#ef4444', fillOpacity: 0.1 }}
          />)}
          {markers.map((item, index) => <CircleMarker
            key={`${item.title || 'marker'}-${index}`}
            center={[toLatLng(item.position).lat, toLatLng(item.position).lng]}
            radius={9}
            pathOptions={{ color: '#fff', weight: 3, fillColor: item.color || '#e11d48', fillOpacity: 1 }}
          >
            {(item.label || item.title) && <Popup>{item.label || item.title}</Popup>}
          </CircleMarker>)}
        </MapContainer>}
  </div>;
}
