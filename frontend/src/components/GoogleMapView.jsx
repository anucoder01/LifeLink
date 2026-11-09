import { useEffect, useRef, useState } from 'react';

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

export default function GoogleMapView({ center, markers = [], circles = [], zoom = 12, style, fallback }) {
  const mapElement = useRef(null);
  const mapInstance = useRef(null);
  const [mode, setMode] = useState(import.meta.env.VITE_GOOGLE_MAPS_API_KEY ? 'loading' : 'fallback');
  const apiKey = import.meta.env.VITE_GOOGLE_MAPS_API_KEY?.trim();

  useEffect(() => {
    let cancelled = false;
    if (!apiKey) {
      setMode('fallback');
      return undefined;
    }

    setMode('loading');
    loadGoogleMaps(apiKey)
      .then(() => { if (!cancelled) setMode('google'); })
      .catch(() => { if (!cancelled) setMode('fallback'); });
    return () => { cancelled = true; };
  }, [apiKey]);

  useEffect(() => {
    if (mode !== 'google' || !mapElement.current || !window.google?.maps) return;
    mapInstance.current = new window.google.maps.Map(mapElement.current, {
      center: toLatLng(center),
      zoom,
      mapTypeControl: false,
      streetViewControl: false,
      fullscreenControl: true
    });
  }, [center, mode, zoom]);

  useEffect(() => {
    const map = mapInstance.current;
    const maps = window.google?.maps;
    if (!map || !maps) return undefined;

    const renderedMarkers = markers.map(item => {
      const marker = new maps.Marker({
        map,
        position: toLatLng(item.position),
        title: item.title || ''
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
    if (mapInstance.current && mode === 'google') mapInstance.current.setCenter(toLatLng(center));
  }, [center, mode]);

  const statusText = mode === 'loading'
    ? 'Loading Google Maps…'
    : apiKey
      ? 'Google Maps is unavailable; showing the OpenStreetMap fallback.'
      : 'Showing OpenStreetMap. Add VITE_GOOGLE_MAPS_API_KEY to frontend/.env.local to enable Google Maps.';

  return <div style={{ position: 'relative', width: '100%', height: '100%', ...style }}>
    {mode === 'google'
      ? <div ref={mapElement} style={{ width: '100%', height: '100%' }} />
      : fallback}
    {mode !== 'google' && <div role="status" style={{
      position: 'absolute', left: 8, bottom: 8, zIndex: 500, maxWidth: 'calc(100% - 32px)',
      padding: '6px 10px', borderRadius: 6, background: 'rgba(15, 23, 42, .9)',
      color: '#fff', fontSize: 12
    }}>{statusText}</div>}
  </div>;
}
