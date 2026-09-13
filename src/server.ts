import {
  AngularNodeAppEngine,
  createNodeRequestHandler,
  isMainModule,
  writeResponseToNodeResponse,
} from '@angular/ssr/node';
import express from 'express';
import { join } from 'node:path';

const browserDistFolder = join(import.meta.dirname, '../browser');

const app = express();
const angularApp = new AngularNodeAppEngine();

const properties = [
  { id: 1, title: 'Light-filled home in HSR', locality: 'HSR Layout', rent: '₹28K', type: 'Apartment', bedrooms: '2 BHK', position: { top: '0%', left: '0%' }, amenities: ['Parking', 'Furnished', 'Power backup'], verified: true, latitude: 12.9116, longitude: 77.6389, status: 'rented', availableFrom: '2026-11-01', rentedOn: '2026-04-15', negotiable: true, durationDays: 150, commissionEarned: 28000 },
  { id: 2, title: 'Quiet home near the park', locality: 'Indiranagar', rent: '₹32K', type: 'Independent house', bedrooms: '2 BHK', position: { top: '0%', left: '0%' }, amenities: ['Parking', 'Pet friendly'], verified: true, latitude: 12.9784, longitude: 77.6408, status: 'available', availableFrom: '2026-09-20', negotiable: true, durationDays: 38, commissionEarned: 0 },
  { id: 3, title: 'Sunny apartment for two', locality: 'Koramangala', rent: '₹24K', type: 'Apartment', bedrooms: '1 BHK', position: { top: '0%', left: '0%' }, amenities: ['Furnished', 'Gym'], verified: false, latitude: 12.9352, longitude: 77.6245, status: 'rented', availableFrom: '2026-10-15', rentedOn: '2026-05-09', negotiable: false, durationDays: 126, commissionEarned: 24000 },
  { id: 4, title: 'Room with a garden view', locality: 'Whitefield', rent: '₹18K', type: 'Room', bedrooms: '1 BHK', position: { top: '0%', left: '0%' }, amenities: ['Power backup', 'Pet friendly'], verified: true, latitude: 12.9698, longitude: 77.7499, status: 'available', availableFrom: '2026-09-28', negotiable: true, durationDays: 22, commissionEarned: 0 },
  { id: 5, title: 'Spacious family apartment', locality: 'Jayanagar', rent: '₹38K', type: 'Apartment', bedrooms: '3 BHK', position: { top: '0%', left: '0%' }, amenities: ['Parking', 'Furnished', 'Gym'], verified: true, latitude: 12.9250, longitude: 77.5938, status: 'rented', availableFrom: '2026-12-01', rentedOn: '2026-03-21', negotiable: false, durationDays: 177, commissionEarned: 38000 },
];

app.get('/api/properties', (req, res) => {
  const north = Number(req.query['north']);
  const south = Number(req.query['south']);
  const east = Number(req.query['east']);
  const west = Number(req.query['west']);
  const validBounds = [north, south, east, west].every(Number.isFinite);
  const visibleProperties = validBounds
    ? properties.filter((property) => property.latitude <= north && property.latitude >= south && property.longitude <= east && property.longitude >= west)
    : properties;

  res.json({ properties: visibleProperties, total: visibleProperties.length, viewport: { north, south, east, west, latitude: Number(req.query['latitude']), longitude: Number(req.query['longitude']), zoom: Number(req.query['zoom']) } });
});

app.get('/api/broker/dashboard', (_req, res) => {
  const rentedListings = properties.filter((property) => property.status === 'rented');
  const availableListings = properties.length - rentedListings.length;
  const commissionEarned = rentedListings.reduce((total, property) => total + property.commissionEarned, 0);
  res.json({
    properties,
    summary: {
      totalListings: properties.length,
      availableListings,
      rentedListings: rentedListings.length,
      occupancyRate: Math.round((rentedListings.length / properties.length) * 100),
      commissionEarned,
      commissionMonths: 1,
    },
  });
});

/**
 * Example Express Rest API endpoints can be defined here.
 * Uncomment and define endpoints as necessary.
 *
 * Example:
 * ```ts
 * app.get('/api/{*splat}', (req, res) => {
 *   // Handle API request
 * });
 * ```
 */

/**
 * Serve static files from /browser
 */
app.use(
  express.static(browserDistFolder, {
    maxAge: '1y',
    index: false,
    redirect: false,
  }),
);

/**
 * Handle all other requests by rendering the Angular application.
 */
app.use((req, res, next) => {
  angularApp
    .handle(req)
    .then((response) =>
      response ? writeResponseToNodeResponse(response, res) : next(),
    )
    .catch(next);
});

/**
 * Start the server if this module is the main entry point, or it is ran via PM2.
 * The server listens on the port defined by the `PORT` environment variable, or defaults to 4000.
 */
if (isMainModule(import.meta.url) || process.env['pm_id']) {
  const port = process.env['PORT'] || 4000;
  app.listen(port, (error) => {
    if (error) {
      throw error;
    }

    console.log(`Node Express server listening on http://localhost:${port}`);
  });
}

/**
 * Request handler used by the Angular CLI (for dev-server and during build) or Firebase Cloud Functions.
 */
export const reqHandler = createNodeRequestHandler(app);
