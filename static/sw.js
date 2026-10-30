const CACHE_NAME = 'nanoscholar-v6';

const PRECACHE_URLS = [
  '/style.css',
  '/manifest.json',
  '/offline',
  '/welcome',
  '/dashboard',
  '/formulas',
  '/privacy',
  '/terms',
  '/logo.svg',
  '/icon.svg',
  '/favicon.svg'
];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => {
      return cache.addAll(PRECACHE_URLS);
    }).then(() => self.skipWaiting())
  );
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((cacheNames) => {
      return Promise.all(
        cacheNames.map((name) => {
          if (name !== CACHE_NAME) {
            return caches.delete(name);
          }
        })
      );
    }).then(() => self.clients.claim())
  );
});

self.addEventListener('fetch', (event) => {
  const req = event.request;
  const url = new URL(req.url);

  // Skip non-GET requests (e.g. POST /login, POST /test, etc.)
  if (req.method !== 'GET') {
    return;
  }

  // Static assets (CSS, manifest, SVG icons): Cache-First
  if (url.pathname === '/style.css' || url.pathname === '/manifest.json' || url.pathname === '/logo.svg' || url.pathname === '/icon.svg' || url.pathname === '/favicon.svg' || url.pathname === '/favicon.ico') {
    event.respondWith(
      caches.match(req).then((cached) => {
        if (cached) return cached;
        return fetch(req).then((response) => {
          if (response && response.status === 200) {
            const clone = response.clone();
            caches.open(CACHE_NAME).then((cache) => cache.put(req, clone));
          }
          return response;
        });
      })
    );
    return;
  }

  // HTML Navigation: Network-First with Cache Fallback & Offline Page
  if (req.mode === 'navigate' || req.headers.get('accept')?.includes('text/html')) {
    event.respondWith(
      fetch(req)
        .then((response) => {
          if (response && response.status === 200) {
            const clone = response.clone();
            caches.open(CACHE_NAME).then((cache) => cache.put(req, clone));
          }
          return response;
        })
        .catch(() => {
          return caches.match(req).then((cached) => {
            if (cached) return cached;
            return caches.match('/offline').then((offline) => {
              return offline || caches.match('/formulas');
            });
          });
        })
    );
    return;
  }

  event.respondWith(
    fetch(req).catch(() => caches.match(req))
  );
});
