# Cerex — Product Roadmap & Evolution Plan

> Version: 1.0.0 | Last Updated: 2026-03-30

---

## Table of Contents

1. [Phase 1: MVP (0-3 months)](#phase-1-mvp-0-3-months)
2. [Phase 2: Growth (3-9 months)](#phase-2-growth-3-9-months)
3. [Phase 3: Scale (9-18 months)](#phase-3-scale-9-18-months)
4. [Phase 4: Global Dominance (18-36 months)](#phase-4-global-dominance-18-36-months)
5. [KPIs Per Phase](#kpis-per-phase)
6. [Technology Evolution](#technology-evolution)
7. [Team Structure Evolution](#team-structure-evolution)

---

## Phase 1: MVP (0-3 months)

**Theme: "Prove the Core Value"**
**Target Markets: France + UK + USA**

### 1.1 Must-Have Features

#### Core Recipe Platform
- [ ] Recipe creation, editing, and publishing (with rich text editor)
- [ ] Recipe browsing by continent, country, culture, category
- [ ] Recipe detail page with ingredients, steps, media, nutritional info
- [ ] Search (text-based) with filters (difficulty, time, dietary, cuisine)
- [ ] User registration and authentication (email + Google OAuth)
- [ ] User profiles and recipe collections

#### Basic Social Features
- [ ] Recipe likes and saves
- [ ] Recipe reviews and ratings
- [ ] User follow/unfollow
- [ ] Basic social feed (followed users' activity)

#### Restaurant & Ordering (Limited)
- [ ] Restaurant listing and profile pages
- [ ] Menu display with Cerex recipe links
- [ ] Basic ordering flow (delivery within pilot cities: Paris, London, NYC)
- [ ] Stripe payment integration (cards only)
- [ ] Order confirmation and status emails

#### Mobile Apps (React Native)
- [ ] iOS and Android apps (feature parity with web MVP)
- [ ] Push notifications (order updates, new followers)
- [ ] Camera integration (recipe photo upload)

#### Admin Panel (Internal)
- [ ] Recipe moderation dashboard
- [ ] User management (suspend, verify)
- [ ] Restaurant onboarding and approval
- [ ] Basic analytics (views, signups, orders)

### 1.2 Technical Foundations

- [ ] Spring Boot microservices (user, recipe, order, notification)
- [ ] PostgreSQL + Redis + basic Elasticsearch
- [ ] AWS deployment (ECS, not K8s yet — simpler for MVP)
- [ ] Stripe integration (payments)
- [ ] SendGrid (transactional emails)
- [ ] Firebase (push notifications)
- [ ] CloudFront CDN (media delivery)
- [ ] Basic monitoring (CloudWatch)
- [ ] GDPR consent on signup (cookie banner, privacy policy)

### 1.3 Content Goals

- [ ] Seed 5,000 high-quality recipes (100 per country, 50 countries)
- [ ] Manually verified cultural accuracy for each recipe
- [ ] Professional photography for 500 featured recipes
- [ ] 50 verified chef accounts with exclusive content
- [ ] 200 restaurant partners (across 3 cities)

### 1.4 MVP Milestones

```
Week 1-2:   Project setup, infrastructure, CI/CD pipeline
Week 3-4:   User Service + Recipe Service core CRUD
Week 5-6:   Frontend components (RecipeCard, RecipeDetail, Search)
Week 7-8:   Order Service + Stripe integration
Week 9-10:  Mobile apps (iOS + Android) beta
Week 11-12: Content seeding, beta testing with 500 users
Week 13:    Soft launch (invite-only)
```

---

## Phase 2: Growth (3-9 months)

**Theme: "Build the Community Engine"**
**Target Markets: +10 European countries, +5 African markets**

### 2.1 AI Features (Phase 2)

- [ ] AI Recipe Generation (GPT-4o integration)
  - Free: 2 generations/month, Explorer: 20, Chef Pro: unlimited
- [ ] Basic personalized recommendations (collaborative filtering v1)
- [ ] Automated recipe translation (French, Spanish, German, Arabic)
- [ ] AI-powered image quality scoring (reject bad recipe photos automatically)
- [ ] Nutritional analysis enhancement (USDA API integration)

### 2.2 Social & Community Features

- [ ] Social posts (recipe shares, cooking updates, questions)
  - Text, images, short videos (60 seconds)
- [ ] Comment system with threaded replies
- [ ] User stories (24-hour ephemeral cooking stories)
- [ ] Cooking challenges (#ChallengeWeek with prizes)
- [ ] Chef live sessions (scheduled live cooking with Q&A)
- [ ] Recipe collaboration (co-authored recipes with shared credit)
- [ ] Cultural Ambassador program (verified cultural experts)

### 2.3 Subscription Launch

- [ ] Explorer plan (€9.99/month) — full launch
- [ ] Chef Pro plan (€24.99/month) — full launch
- [ ] In-app purchase (iOS/Android)
- [ ] Annual billing option (25% discount)
- [ ] Team accounts (Chef Pro can add 2 collaborators)
- [ ] Free trial (7 days Explorer for all new signups)

### 2.4 Restaurant Platform Enhancement

- [ ] Restaurant analytics dashboard (orders, revenue, top dishes)
- [ ] Menu management system (CRUD with drag-and-drop ordering)
- [ ] Promotional campaign tools (discount codes, flash sales)
- [ ] Restaurant verification system (health cert upload, review)
- [ ] Expanded ordering: PayPal + Apple Pay + Google Pay
- [ ] Real-time GPS delivery tracking (WebSocket)
- [ ] Ingredient kit ordering (home delivery of fresh ingredients)
- [ ] Restaurant POS integration (Square, Lightspeed)

### 2.5 Content & Search

- [ ] Elasticsearch full upgrade (autocomplete, faceted search, spell correction)
- [ ] Video recipes (upload, transcode, stream)
- [ ] Recipe versioning (fork a recipe and customize it)
- [ ] Seasonal recipe collections (auto-curated by AI + editorial)
- [ ] Cultural spotlight pages (monthly feature on specific cultures)
- [ ] Recipe collections (user-curated, shareable)

### 2.6 Expansion to 10 New Languages

- [ ] French (fr), Spanish (es), German (de), Italian (it), Portuguese (pt)
- [ ] Arabic (ar), Swahili (sw), Yoruba (yo), Hausa (ha), Hindi (hi)
- [ ] Right-to-left (RTL) support for Arabic
- [ ] Localized payment methods (M-Pesa for East Africa, etc.)

### 2.7 Infrastructure Upgrades (Phase 2)

- [ ] Migrate to Kubernetes (EKS) for all services
- [ ] Implement service mesh (Istio) for mTLS and traffic management
- [ ] Kong API Gateway (replace ALB direct routing)
- [ ] Redis Cluster (replace single Redis instance)
- [ ] Kafka (replace synchronous notifications)
- [ ] Prometheus + Grafana monitoring
- [ ] ELK Stack for centralized logging
- [ ] Automated performance testing in CI/CD

### 2.8 Phase 2 Milestones (Monthly)

```
Month 4:  AI recipe generation beta (1,000 users)
Month 5:  Social posts + comments full launch
Month 6:  Subscription plans live (Explorer + Chef Pro)
Month 7:  10-language expansion, ingredient kits
Month 8:  Restaurant v2 (analytics, POS integration)
Month 9:  1M registered users milestone
```

---

## Phase 3: Scale (9-18 months)

**Theme: "Personalization at Scale"**
**Target Markets: Global (50+ countries)**

### 3.1 Advanced AI Features

- [ ] Hybrid recommendation engine (collaborative + content-based + contextual)
- [ ] Personalized weekly menu planner
- [ ] Waste optimization algorithm (use what's in your fridge)
- [ ] Culinary trend analysis dashboard (for restaurants and chefs)
- [ ] AI-powered video recipe summaries
- [ ] Voice-guided cooking mode (hands-free with Siri/Google integration)
- [ ] Smart scaling (auto-adjust recipe quantities and modify instructions)
- [ ] AI sous-chef chat (real-time cooking assistance via LLM)
- [ ] Flavor pairing engine (suggest complementary ingredients)

### 3.2 Marketplace Expansion

- [ ] Professional chef marketplace (hire chefs for events, classes)
- [ ] Culinary tourism integration (partner with travel platforms)
- [ ] Restaurant discovery by neighborhood/city (Google Maps integration)
- [ ] Grocery delivery integration (Instacart, Ocado, Amazon Fresh partnerships)
- [ ] B2B: Corporate meal planning for office catering
- [ ] Healthcare: Hospital nutrition planning module
- [ ] API commercial launch (Recipe API, Recommendation API, Nutrition API)

### 3.3 Creator Economy Features

- [ ] Chef monetization: sell premium recipes (€0.99-€4.99 each)
- [ ] Live cooking class platform (scheduled or on-demand)
- [ ] Recipe ebook creation tool (auto-format for PDF/EPUB)
- [ ] Merchandise store integration (chefs sell branded items)
- [ ] Cerex Creator Fund (paying top creators for exclusive content)
- [ ] Revenue dashboard (transparent earnings tracking for creators)

### 3.4 Platform Intelligence

- [ ] Real-time fraud detection (live scoring on all restaurant transactions)
- [ ] Content moderation AI (auto-detect inappropriate content)
- [ ] Anti-spam system (detect fake reviews, bot accounts)
- [ ] Dynamic pricing insights (for restaurants: when to promote)
- [ ] Competitive intelligence reports (for restaurant partners)

### 3.5 Mobile Native Features

- [ ] Offline-first architecture (full offline access with sync)
- [ ] AR recipe overlay (point camera at ingredients → get recipe suggestions)
- [ ] Smart shopping list with aisle organization
- [ ] Apple Watch / Wear OS app (timer, step navigation)
- [ ] Widget support (iOS/Android home screen recipe of the day)
- [ ] Siri/Google shortcuts ("Hey Cerex, what can I cook for dinner?")

### 3.6 Social Platform Maturity

- [ ] Recipe contest platform (branded competitions with prizes)
- [ ] Chef certification program (Cerex Certified Chef badge)
- [ ] Community groups (e.g., "Vegan African Cuisine" group)
- [ ] Recipe commenting with multimedia (photos in comments)
- [ ] Direct messaging between users (food-focused chat)
- [ ] Trending hashtags and discover feed

### 3.7 Infrastructure at Scale

- [ ] Multi-region deployment (us-east-1, eu-west-1, ap-southeast-1)
- [ ] Global database strategy (read replicas in each region)
- [ ] CDN optimization (serve from 50+ global PoPs)
- [ ] Chaos engineering (regular failure injection testing)
- [ ] Auto-scaling policies (handle 10x traffic spikes)
- [ ] Disaster recovery automation (1-click failover to backup region)
- [ ] Zero-downtime deployments (blue-green for all services)
- [ ] Security penetration testing (quarterly external audits)

### 3.8 Phase 3 Milestones

```
Month 10: Personalized menu planner live
Month 11: Chef marketplace beta (100 verified chefs)
Month 12: 5M registered users, €2M MRR
Month 13: API commercial launch (5 paying customers)
Month 14: Grocery delivery partnerships (Instacart, Ocado)
Month 15: AR features beta (iOS first)
Month 16: B2B corporate catering pilot (10 companies)
Month 17: Healthcare nutrition pilot (2 hospital networks)
Month 18: 10M registered users, Series A fundraise
```

---

## Phase 4: Global Dominance (18-36 months)

**Theme: "The World's Kitchen"**
**Target: Global #1 culinary platform**

### 4.1 Platform Ecosystem

- [ ] Cerex Open API (developer platform with App Store)
- [ ] Third-party app integrations (smart fridges, recipe apps, meal kits)
- [ ] Cerex OS (embedded culinary OS for smart kitchen appliances)
- [ ] White-label enterprise solution (hotels, airlines, healthcare)
- [ ] Cerex Data Insights (B2B trend data subscriptions for food industry)

### 4.2 Cultural Preservation Initiative

- [ ] UNESCO partnership: digital preservation of endangered cuisines
- [ ] Indigenous cookbook digitization program (100+ cultures)
- [ ] Chef heritage program: connect diaspora chefs with homeland traditions
- [ ] Culinary museum integration (virtual cooking demos from museums)
- [ ] Academic partnerships (food anthropology research data)

### 4.3 Physical World Integration

- [ ] Cerex Kitchen pop-up stores (cooking class spaces in major cities)
- [ ] Cerex Market concept (specialty ingredient stores linked to platform)
- [ ] Restaurant Cerex label program (quality-certified restaurants)
- [ ] Culinary Academy: online culinary school (accredited courses)
- [ ] Food festival ownership or major sponsorships

### 4.4 Advanced Technology

- [ ] Proprietary LLM fine-tuned on culinary data (vs. relying on OpenAI)
- [ ] Computer vision for recipe completion tracking (camera detects cooking progress)
- [ ] IoT integration: smart ovens auto-set temperature from Cerex recipe
- [ ] Blockchain for ingredient provenance tracking (farm-to-table transparency)
- [ ] Federated learning for privacy-preserving personalization
- [ ] Real-time inventory management for restaurant partners

### 4.5 Geographic Expansion Details

```
Year 3 Target Markets (active with local operations):
├── Europe:         France, UK, Germany, Spain, Italy, Netherlands, Belgium
├── Africa:         Nigeria, Ghana, Senegal, Kenya, South Africa, Ethiopia
├── Asia:           Japan, South Korea, India, Thailand, Indonesia, China
├── Americas:       USA, Canada, Brazil, Mexico, Colombia
├── Middle East:    UAE, Saudi Arabia, Lebanon, Turkey
└── Oceania:        Australia, New Zealand
```

### 4.6 M&A Strategy

Potential acquisition targets:
- **Recipe platforms** (acquire user base and content)
- **Food tech startups** (AR food apps, nutrition analysis)
- **Regional food delivery apps** (market entry acceleration)
- **Culinary data companies** (ingredient databases, nutrition data)
- **AI/ML talent** (acqui-hires in key ML specialties)

### 4.7 Phase 4 Milestones

```
Month 20: Proprietary LLM v1 beta testing
Month 22: IoT integration (Samsung smart fridge partnership)
Month 24: 35M registered users, €21M MRR, Series B
Month 26: Cerex Academy launches (first 5 courses)
Month 28: 50+ country presence, physical Cerex Kitchen (Paris)
Month 30: UNESCO partnership announcement
Month 32: Cerex OS embedded in first appliance model
Month 34: IPO preparation begins
Month 36: 50M+ registered users, €70M MRR, IPO or strategic exit
```

---

## KPIs Per Phase

### Phase 1 KPIs (Month 0-3)

| KPI | Target | Measurement |
|-----|--------|-------------|
| Registered Users | 50,000 | User Service DB |
| Monthly Active Users (MAU) | 15,000 | Analytics |
| Recipe Views/Day | 25,000 | Analytics |
| Recipes Published | 5,000 | Content DB |
| Restaurants Onboarded | 200 | Restaurant DB |
| Daily Orders | 500 | Order DB |
| App Store Rating | ≥ 4.0/5.0 | App Store |
| System Uptime | ≥ 99.5% | Monitoring |
| API P99 Latency | < 500ms | APM |
| NPS Score | ≥ 25 | User surveys |

---

### Phase 2 KPIs (Month 3-9)

| KPI | Target | Measurement |
|-----|--------|-------------|
| Registered Users | 1,000,000 | User Service DB |
| MAU | 450,000 | Analytics |
| Paying Subscribers | 65,000 | Subscription DB |
| Subscriber Conversion Rate | 7% of MAU | Analytics |
| Monthly Revenue (MRR) | €1.67M | Finance |
| Restaurant Partners | 3,500 | Restaurant DB |
| Daily Orders | 8,000 | Order DB |
| AI Recipes Generated | 10,000/month | AI Service |
| Average Session Duration | ≥ 8 minutes | Analytics |
| D30 Retention (new users) | ≥ 25% | Analytics |
| App Store Rating | ≥ 4.3/5.0 | App Store |
| NPS Score | ≥ 40 | User surveys |
| System Uptime | ≥ 99.9% | Monitoring |
| API P99 Latency | < 300ms | APM |

---

### Phase 3 KPIs (Month 9-18)

| KPI | Target | Measurement |
|-----|--------|-------------|
| Registered Users | 10,000,000 | User Service DB |
| MAU | 4,000,000 | Analytics |
| Paying Subscribers | 600,000 | Subscription DB |
| Explorer ARPU (monthly) | €9.99 | Finance |
| Chef Pro ARPU (monthly) | €24.99 | Finance |
| MRR | €12M | Finance |
| Restaurant Partners | 25,000 | Restaurant DB |
| Countries Active | 50+ | Geo Analytics |
| Languages Supported | 25+ | i18n |
| AI-Generated Recipes | 100K/month | AI Service |
| Recommendation CTR | ≥ 18% | Analytics |
| Recommendation Conversion | ≥ 3% (to order) | Analytics |
| Subscriber Churn Rate | ≤ 3%/month | Finance |
| LTV:CAC Ratio | ≥ 35x | Finance |
| D90 Retention | ≥ 40% | Analytics |
| NPS Score | ≥ 55 | User surveys |
| Fraud Detection Accuracy | ≥ 95% | ML Metrics |
| System Uptime | ≥ 99.95% | Monitoring |
| API P99 Latency | < 200ms globally | APM |

---

### Phase 4 KPIs (Month 18-36)

| KPI | Target | Measurement |
|-----|--------|-------------|
| Registered Users | 50,000,000 | User Service DB |
| MAU | 15,000,000 | Analytics |
| Paying Subscribers | 2,800,000 | Subscription DB |
| MRR | €70M | Finance |
| Annual Recurring Revenue (ARR) | €840M | Finance |
| Gross Margin | ≥ 65% | Finance |
| Net Revenue Retention | ≥ 110% | Finance |
| Restaurant Partners | 90,000 | Restaurant DB |
| Countries with Local Operations | 25 | Ops |
| API Customers (B2B) | 500+ | Sales |
| Recipe Database | 1,000,000+ | Content DB |
| Creator Revenue (total to chefs) | €10M/month | Finance |
| Culinary Cultures Documented | 500+ | Content DB |
| NPS Score | ≥ 65 | User surveys |
| Brand Awareness (aided recall) | ≥ 60% in top markets | Market Research |
| System Uptime | ≥ 99.99% | Monitoring |
| API P99 Latency | < 100ms globally | APM |

---

## Technology Evolution

### Infrastructure Maturity Model

| Aspect | Phase 1 | Phase 2 | Phase 3 | Phase 4 |
|--------|---------|---------|---------|---------|
| Container | Docker | Kubernetes | K8s + Istio | K8s + full platform eng |
| Database | RDS Single-AZ | RDS Multi-AZ | RDS + MongoDB Atlas | Global distributed |
| Caching | Redis Single | Redis Sentinel | Redis Cluster | Redis Global |
| Search | Basic Elasticsearch | Full ES cluster | ES + vector search | Proprietary search |
| Message Queue | Direct Kafka | Kafka MSK | Kafka + Schema Registry | Custom event platform |
| ML Platform | External APIs only | Basic ML service | MLflow + custom models | Proprietary LLM |
| Observability | CloudWatch | Prometheus + ELK | Full OTEL stack | SRE platform |
| CI/CD | GitHub Actions | Actions + ArgoCD | Full GitOps | Internal platform |

---

## Team Structure Evolution

| Role | Phase 1 | Phase 2 | Phase 3 | Phase 4 |
|------|---------|---------|---------|---------|
| Engineering Total | 8 | 25 | 65 | 200+ |
| Product | 2 | 6 | 15 | 40 |
| Design | 2 | 5 | 12 | 30 |
| Data/AI | 1 | 5 | 15 | 50 |
| DevOps/SRE | 1 | 3 | 8 | 25 |
| Content/Editorial | 3 | 10 | 25 | 60 |
| Marketing | 2 | 8 | 20 | 60 |
| Operations | 2 | 6 | 15 | 40 |
| Legal/Compliance | 0 | 1 | 3 | 8 |
| **Total Headcount** | **~25** | **~70** | **~180** | **~550** |

---

*Document Version: 1.0.0 | Next Review: 2026-06-30 | Owner: Cerex Product & Strategy Team*
