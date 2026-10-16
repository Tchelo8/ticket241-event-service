# Event Service Ticket241 — Guide d'intégration front utilisateur

## 1. Accès

| | |
|---|---|
| Nom Eureka | `event-service-ticket241` |
| Port direct (dev) | `http://localhost:9001` |
| Préfixe | `/api/events` |
| Format | JSON, UTF-8 |
| Auth | **aucune** sur les routes listées en §3 (accessibles sans compte via la gateway) |

> ⚠️ Le service ne configure **pas de CORS**. Le navigateur doit passer par la **gateway** (qui gère CORS + JWT), pas appeler `:9001` directement.

> ℹ️ Les détails d'un événement (`/{uuid}`, `/slug/{slug}`) ne renvoient que les événements **visibles publiquement** : un brouillon ou un événement en attente de validation répond `404`. Un événement annulé ou reporté reste consultable.

Test rapide : `GET /hello` → texte brut de bienvenue.

---

## 2. Conventions

**Dates** : ISO-8601 **sans fuseau** (`LocalDateTime`), ex. `"2026-12-20T20:00:00"`. Heure locale du serveur.
**Prix** : nombres décimaux (`minPrice`, `maxPrice`, `price`). La devise n'est pas renvoyée.
**Identifiants** : chaque ressource a un `id` (Long) **et** un `uuid` (String). Attention, chaque route attend l'un ou l'autre (voir tableaux).

**Pagination** (routes marquées *Page*) — paramètres query :

| Param | Défaut | Note |
|---|---|---|
| `page` | `0` | commence à 0 |
| `size` | `10` (`5` pour `/featured`) | |

Réponse paginée (format Spring standard) :
```json
{
  "content": [ /* EventSummary[] */ ],
  "totalElements": 42,
  "totalPages": 5,
  "number": 0,          // page courante
  "size": 10,
  "numberOfElements": 10,
  "first": true,
  "last": false,
  "empty": false,
  "pageable": { ... }, "sort": { ... }   // ignorables
}
```

**Erreurs** :
```json
{ "status": 404, "message": "Événement non trouvé avec le slug : xxx", "timestamp": "2026-10-09T14:00:00", "path": "/api/events/slug/xxx" }
```
Codes : `400` validation / données invalides · `403` accès refusé · `404` introuvable · `409` places insuffisantes · `500` erreur interne (y compris un `sortBy` invalide).

---

## 3. Routes publiques

### 3.1 Événements

Toutes les **listes** ne renvoient que les événements **`PUBLISHED` + publics + actifs**.

| Méthode | Route | Retour | Description |
|---|---|---|---|
| GET | `/api/events/all?page&size&sortBy&sortDirection` | Page | Tous les événements visibles. `sortBy` = n'importe quel champ de l'événement (`startDate` par défaut, ex. `minPrice`, `viewCount`, `createdAt`, `publishedAt`), `sortDirection` = `asc` (défaut) \| `desc` |
| GET | `/api/events/upcoming?page&size` | Page | Événements dont `startDate >= maintenant`, triés par date croissante |
| GET | `/api/events/featured?page&size` | Page | Événements mis en avant (`isFeatured`), du plus récemment publié au plus ancien. `size` défaut **5** |
| GET | `/api/events/category/{category}?page&size` | Page | Par catégorie. `{category}` = **nom technique** (`CONCERT`, `SPORT`…, insensible à la casse), **pas** l'id ni l'uuid. Tri date croissante |
| GET | `/api/events/city/{cityId}?page&size` | Page | Par ville. `{cityId}` = **id numérique** de la ville. Tri date croissante |
| GET | `/api/events/search?keyword=…&page&size` | Page | Recherche (insensible à la casse, « contient ») dans **nom**, **description** et **nom de ville**. `keyword` obligatoire |
| GET | `/api/events/{uuid}` | EventDetail | Détail par uuid. **+1 vue** |
| GET | `/api/events/slug/{slug}` | EventDetail | Détail par slug (URL SEO). **+1 vue** |
| POST | `/api/events/{uuid}/view` | `200` vide | +1 vue seulement (si le détail est déjà affiché depuis une liste sans refetch) |

> ⚠️ Les routes **détail** (`/{uuid}`, `/slug/{slug}`) renvoient l'événement **quel que soit son statut** (brouillon, annulé…). Le front doit vérifier `eventStatus === "PUBLISHED"` / `isCancelled` avant d'afficher ou de vendre.
> Pas de filtre combiné (ville + catégorie, plage de dates, prix) côté API pour l'instant : à faire côté front ou à demander.

### 3.2 Villes

| Méthode | Route | Retour | Description |
|---|---|---|---|
| GET | `/api/events/cities/get/all/active` | CitySummary[] | Villes actives, triées par nom (non paginé) |
| GET | `/api/events/cities/{uuid}` | CityDetail | Détail par uuid |
| GET | `/api/events/cities/id/{id}` | CityDetail | Détail par id numérique |
| GET | `/api/events/cities/country/{country}` | CitySummary[] | Villes actives d'un pays (nom du pays, insensible à la casse, ex. `Gabon`) |
| GET | `/api/events/cities/search?keyword=…` | CitySummary[] | Recherche dans nom de ville **ou** pays |

Flux typique : `get/all/active` → l'utilisateur choisit → `GET /api/events/city/{city.id}`.

### 3.3 Catégories d'événement

| Méthode | Route | Retour | Description |
|---|---|---|---|
| GET | `/api/events/categories/get/all` | CategorySummary[] | Catégories actives |
| GET | `/api/events/categories/{uuid}` | CategoryDetail | Détail |

Flux typique : `get/all` → `GET /api/events/category/{cat.name}` (utiliser `name`, afficher `displayName`).

### 3.4 Disponibilité d'une catégorie de billet

| Méthode | Route | Retour |
|---|---|---|
| GET | `/api/events/{eventId}/categories/{ticketCategoryId}` | TicketCategory |
| GET | `/api/events/{eventId}/categories/{ticketCategoryId}/availability?quantity=N` | `true` / `false` |

(ids numériques ; utile pour vérifier avant d'envoyer l'utilisateur vers la réservation.)

---

## 4. Modèles de données

### EventSummary (éléments des listes) / EventDetail (détail)
Même structure pour les deux. **Différences** :

| Champ | Dans les listes | Dans le détail |
|---|---|---|
| `ticketCategories` | toujours `[]` | rempli |
| `structureId`, `structureName` | `null` | rempli |

Champs :

| Groupe | Champs |
|---|---|
| Identité | `id`, `uuid`, `name`, `slug`, `fullDescription`, `coverImageUrl` |
| Catégorie | `categoryId`, `category` (nom technique), `categoryDisplayName`, `subCategory` |
| Dates | `startDate`, `endDate`, `doorsOpenTime`, `saleStartDate`, `saleEndDate` |
| Lieu | `venueName`, `venueAddress`, `venueAccessInfo`, `cityId`, `cityName`, `cityCountry`, `isOnline`, `onlineMeetingUrl` |
| Places | `totalSeats`, `availableSeats`, `reservedSeats`, `soldSeats` |
| Prix | `minPrice`, `maxPrice` |
| Organisateur | `structureId`, `structureName`, `organizerName`, `organizerPhone` |
| Statut | `eventStatus`, `isFeatured`, `isPromoted`, `isPublic`, `isActive`, `isCancelled`, `cancelledAt`, `cancellationReason`, `publishedAt` |
| Conditions | `minAge`, `termsAndConditions`, `cancellationPolicy`, `allowRefund`, `refundDeadlineDays`, `maxTicketsPerUser` |
| Stats | `viewCount`, `favoriteCount`, `shareCount`, `averageRating`, `reviewCount` |
| Interne (ignorer) | `validationMode`, `createdBy`, `updatedBy`, `approvedBy`, `approvedAt`, `createdAt`, `updatedAt` |
| **Calculés** | `isSaleOpen`, `isPastEvent`, `isSoldOut`, `ticketCategories[]` |

Champs calculés :
- `isSaleOpen` = publié **et** public **et** actif **et** maintenant dans [`saleStartDate`, `saleEndDate`] (bornes nulles = pas de limite) **et** `availableSeats > 0` → **c'est ce booléen qui doit piloter le bouton « Acheter »**.
- `isPastEvent` = `startDate` dépassée.
- `isSoldOut` = `availableSeats == 0`.

`eventStatus` : `DRAFT` · `PENDING_APPROVAL` · `PUBLISHED` · `CANCELLED` · `COMPLETED` · `POSTPONED` · `SOLD_OUT`

### TicketCategory (dans `ticketCategories`)
`id`, `uuid`, `name` (ex. VIP, Standard), `description`, `price`, `totalSeats`, `availableSeats`, `reservedSeats`, `soldSeats`, `displayOrder` (ordre d'affichage), `isActive`, `maxTicketsPerOrder`, `benefits`, `colorCode` (ex. `#FFD700`), `hasAvailableSeats` (calculé).

### CitySummary
`id`, `uuid`, `name`, `country`, `countryCode`, `eventCount`
### CityDetail
CitySummary + `region`, `postalCode`, `latitude`, `longitude`, `isActive`, `createdAt`, `updatedAt`

> `eventCount` compte **tous** les événements créés dans la ville (brouillons inclus), pas seulement ceux visibles. Ne pas l'utiliser comme « N événements disponibles ».

### CategorySummary
`id`, `uuid`, `name` (technique, à passer dans les routes), `displayName` (à afficher), `iconUrl`
### CategoryDetail
CategorySummary + `description`, `isActive`, `createdAt`, `updatedAt`

Catégories par défaut : `CONCERT`, `SPORT`, `THEATRE`, `CONFERENCE`, `FESTIVAL`, `CINEMA`, `EXPOSITION`, `DANSE`, `COMEDIE`, `OPERA`, `CIRQUE`, `FAMILLE`, `GASTRONOMIE`, `FORMATION`, `NETWORKING`, `CHARITY`, `RELIGION`, `AUTRE`.

---

## 5. Hors périmètre du front utilisateur

Ces routes existent mais ne concernent pas un front public :
- **Création / modification / suppression / publication / annulation** d'événements, villes, catégories → headers `X-User-Id` + `X-User-Roles` injectés par la gateway, réservées organisateurs/admins.
- `GET /api/events/my-events`, `GET /api/events/structure/{id}`, `GET /api/events/admin/slug/{slug}` → back-office.
- `/api/events/structures/**` → gestion des structures.
- `GET /api/events/id/{id}` → **service ticket uniquement**, ne jamais appeler depuis le front.
- Les places (réservation, vente, libération) ne se modifient plus par REST : elles sont gérées par les messages Kafka échangés avec ticket-service (`ticket-reserved`, `ticket-paid`, `ticket-cancelled`, `reservation-expired`).