/**
 * Smart Lost and Found Management System
 * Frontend Application Controller
 */

const API_BASE = '/api/v1';

const App = {
    state: {
        currentUser: null,
        token: localStorage.getItem('campus_jwt_token') || null,
        currentView: 'home',
        unreadNotifCount: 0,
        activeTab: 'all',
        selectedCategory: null,
        searchQuery: '',
        locationQuery: '',
        activeHandoverClaim: null,
        activeClaimFoundItem: null,
        recentItemsCache: []
    },

    init() {
        this.bindEvents();
        this.checkAuthSession();
        this.handleHashChange();
        this.loadStats();
    },

    // --- API HELPER ---
    async api(endpoint, method = 'GET', data = null, isFormData = false) {
        const headers = {};
        if (this.state.token) {
            headers['Authorization'] = `Bearer ${this.state.token}`;
        }
        if (!isFormData && data) {
            headers['Content-Type'] = 'application/json';
        }

        const config = {
            method,
            headers
        };

        if (data) {
            config.body = isFormData ? data : JSON.stringify(data);
        }

        try {
            const res = await fetch(`${API_BASE}${endpoint}`, config);
            const json = await res.json();
            if (!res.ok) {
                throw new Error(json.message || `Request failed with status ${res.status}`);
            }
            return json;
        } catch (err) {
            console.error('API Error:', err);
            throw err;
        }
    },

    // --- AUTHENTICATION ---
    async checkAuthSession() {
        if (!this.state.token) {
            this.renderAuthState();
            return;
        }
        try {
            const res = await this.api('/auth/me');
            if (res.success && res.data) {
                this.state.currentUser = res.data;
                this.loadNotificationCount();
            } else {
                this.logout();
            }
        } catch (e) {
            this.logout();
        }
        this.renderAuthState();
    },

    async login(email, password) {
        try {
            const res = await this.api('/auth/login', 'POST', { collegeEmail: email, password });
            if (res.success && res.data) {
                this.state.token = res.data.token;
                localStorage.setItem('campus_jwt_token', res.data.token);
                this.state.currentUser = {
                    userId: res.data.userId,
                    fullName: res.data.fullName,
                    collegeEmail: res.data.collegeEmail,
                    campusId: res.data.campusId,
                    department: res.data.department,
                    role: res.data.role
                };
                this.showToast(`Welcome back, ${res.data.fullName}!`, 'success');
                this.closeModal('loginModal');
                this.renderAuthState();
                this.loadNotificationCount();
                this.refreshCurrentView();
            }
        } catch (err) {
            this.showToast(err.message || 'Login failed. Please verify credentials.', 'error');
        }
    },

    async quickDemoLogin(type) {
        if (type === 'student') {
            await this.login('rahul.sharma@college.edu', 'Student@123');
        } else if (type === 'staff') {
            await this.login('anil.verma@college.edu', 'Staff@123');
        } else if (type === 'admin') {
            await this.login('admin@college.edu', 'Admin@123');
        }
    },

    async register(formData) {
        try {
            const res = await this.api('/auth/register', 'POST', formData);
            if (res.success && res.data) {
                this.state.token = res.data.token;
                localStorage.setItem('campus_jwt_token', res.data.token);
                this.state.currentUser = {
                    userId: res.data.userId,
                    fullName: res.data.fullName,
                    collegeEmail: res.data.collegeEmail,
                    campusId: res.data.campusId,
                    department: res.data.department,
                    role: res.data.role
                };
                this.showToast('Account registered successfully! Welcome to CampusFind.', 'success');
                this.closeModal('registerModal');
                this.renderAuthState();
                this.loadNotificationCount();
                this.refreshCurrentView();
            }
        } catch (err) {
            this.showToast(err.message || 'Registration failed. Check your details.', 'error');
        }
    },

    logout() {
        this.state.token = null;
        this.state.currentUser = null;
        localStorage.removeItem('campus_jwt_token');
        this.renderAuthState();
        this.showToast('You have signed out.', 'info');
        if (this.state.currentView === 'user-dashboard' || this.state.currentView === 'admin-dashboard') {
            this.navigate('home');
        }
    },

    renderAuthState() {
        const user = this.state.currentUser;
        const authActions = document.getElementById('navAuthActions');
        const userDashBtn = document.getElementById('navUserDashLink');
        const adminDashBtn = document.getElementById('navAdminDashLink');
        const mobileUserDashLink = document.getElementById('mobileUserDashLink');
        const mobileAdminDashLink = document.getElementById('mobileAdminDashLink');

        if (user) {
            if (userDashBtn) userDashBtn.style.display = 'flex';
            if (adminDashBtn) adminDashBtn.style.display = (user.role === 'ADMIN') ? 'flex' : 'none';
            if (mobileUserDashLink) mobileUserDashLink.style.display = 'flex';
            if (mobileAdminDashLink) mobileAdminDashLink.style.display = (user.role === 'ADMIN') ? 'flex' : 'none';

            authActions.innerHTML = `
                <div style="display: flex; align-items: center; gap: 0.75rem;">
                    <div style="display: flex; flex-direction: column; text-align: right; line-height: 1.2;">
                        <span style="font-weight: 700; font-size: 0.85rem; color: var(--slate-900);">${this.escape(user.fullName)}</span>
                        <span style="font-size: 0.7rem; color: var(--purple-700); font-weight: 600;">${user.role} (${user.campusId})</span>
                    </div>
                    <button class="btn btn-sm btn-outline" onclick="App.logout()">
                        <i class="fas fa-sign-out-alt"></i> Sign Out
                    </button>
                </div>
            `;
        } else {
            if (userDashBtn) userDashBtn.style.display = 'none';
            if (adminDashBtn) adminDashBtn.style.display = 'none';
            if (mobileUserDashLink) mobileUserDashLink.style.display = 'none';
            if (mobileAdminDashLink) mobileAdminDashLink.style.display = 'none';

            authActions.innerHTML = `
                <button class="btn btn-sm btn-outline" onclick="App.openModal('loginModal')">
                    <i class="fas fa-sign-in-alt"></i> Sign In
                </button>
                <button class="btn btn-sm btn-primary" onclick="App.openModal('registerModal')">
                    <i class="fas fa-user-plus"></i> Register
                </button>
            `;
        }
    },

    toggleMobileMenu() {
        const drawer = document.getElementById('mobileNavDrawer');
        if (drawer) {
            drawer.classList.toggle('open');
        }
    },

    // --- NAVIGATION ---
    handleHashChange() {
        const hash = window.location.hash.replace('#', '') || 'home';
        this.navigate(hash);
    },

    navigate(viewName) {
        this.state.currentView = viewName;
        window.location.hash = viewName;

        // Update nav active class
        document.querySelectorAll('.nav-link').forEach(link => {
            const target = link.getAttribute('data-view');
            link.classList.toggle('active', target === viewName);
        });

        // Hide all views
        document.querySelectorAll('.view-section').forEach(sec => {
            sec.style.display = 'none';
        });

        // Show target view
        const targetView = document.getElementById(`view-${viewName}`);
        if (targetView) {
            targetView.style.display = 'block';
            window.scrollTo({ top: 0, behavior: 'smooth' });
        }

        // View specific loaders
        switch (viewName) {
            case 'home':
                this.loadHomeData();
                break;
            case 'lost':
                this.loadLostItems();
                break;
            case 'found':
                this.loadFoundItems();
                break;
            case 'matches':
                this.loadMatches();
                break;
            case 'report':
                this.initReportForm();
                break;
            case 'user-dashboard':
                if (!this.state.currentUser) {
                    this.openModal('loginModal');
                    this.navigate('home');
                } else {
                    this.loadUserDashboard();
                }
                break;
            case 'admin-dashboard':
                if (!this.state.currentUser || this.state.currentUser.role !== 'ADMIN') {
                    this.showToast('Administrator privileges required to access Admin Console.', 'error');
                    this.navigate('home');
                } else {
                    this.loadAdminDashboard();
                }
                break;
            case 'about':
                break;
        }
    },

    refreshCurrentView() {
        this.navigate(this.state.currentView);
    },

    // --- HOME VIEW DATA ---
    async loadStats() {
        try {
            const res = await this.api('/stats/summary');
            if (res.success && res.data) {
                const s = res.data;
                const setEl = (id, val) => {
                    const el = document.getElementById(id);
                    if (el) el.innerText = val;
                };
                setEl('statTotalLost', s.totalLost);
                setEl('statTotalFound', s.totalFound);
                setEl('statTotalReturned', s.totalReturned);
                setEl('statPendingClaims', s.pendingClaims);

                // Update admin counters if visible
                setEl('adminStatLost', s.totalLost);
                setEl('adminStatFound', s.totalFound);
                setEl('adminStatReturned', s.totalReturned);
                setEl('adminStatClaims', s.pendingClaims);
                setEl('adminStatUsers', s.totalUsers);
            }
        } catch (e) {
            console.warn('Could not load statistics:', e);
        }
    },

    async loadHomeData() {
        this.loadStats();
        // Load recent lost & found items
        try {
            const [lostRes, foundRes] = await Promise.all([
                this.api('/items/lost'),
                this.api('/items/found')
            ]);
            const recentLost = (lostRes.data || []).slice(0, 4);
            const recentFound = (foundRes.data || []).slice(0, 4);

            this.state.recentItemsCache = [...recentLost, ...recentFound];
            this.filterHomeRecent('all');
        } catch (e) {
            console.error('Error loading recent home items:', e);
        }
    },

    filterHomeRecent(type) {
        document.querySelectorAll('#homeTabAll, #homeTabLost, #homeTabFound').forEach(btn => btn.classList.remove('active'));
        if (type === 'all') document.getElementById('homeTabAll')?.classList.add('active');
        if (type === 'lost') document.getElementById('homeTabLost')?.classList.add('active');
        if (type === 'found') document.getElementById('homeTabFound')?.classList.add('active');

        let filtered = this.state.recentItemsCache;
        if (type === 'lost') {
            filtered = this.state.recentItemsCache.filter(item => item.lostId !== undefined);
        } else if (type === 'found') {
            filtered = this.state.recentItemsCache.filter(item => item.foundId !== undefined);
        }

        this.renderRecentCards(filtered);
    },

    renderRecentCards(items) {
        const container = document.getElementById('homeRecentGrid');
        if (!container) return;
        if (!items || items.length === 0) {
            container.innerHTML = `<div style="grid-column: 1/-1; text-align: center; padding: 2.5rem; color: var(--slate-500); background: var(--white); border-radius: var(--radius-lg); border: 1px dashed var(--slate-300);">No items reported in this category.</div>`;
            return;
        }

        container.innerHTML = items.map(item => this.createItemCardHtml(item)).join('');
    },

    // --- ITEM CARD HTML GENERATOR ---
    createItemCardHtml(item) {
        const isLost = item.lostId !== undefined;
        const id = isLost ? item.lostId : item.foundId;
        const type = isLost ? 'lost' : 'found';
        const date = isLost ? item.dateLost : item.dateFound;
        const loc = isLost ? item.locationLost : item.locationFound;
        const image = item.imageUrl || '/images/items/default.svg';

        let badgeClass = 'badge-available';
        let statusLabel = item.status;
        if (item.status === 'REPORTED') badgeClass = 'badge-lost';
        else if (item.status === 'RETURNED' || item.status === 'RESOLVED') badgeClass = 'badge-returned';
        else if (item.status === 'CLAIM_PENDING') badgeClass = 'badge-pending';

        return `
            <div class="item-card">
                <div class="item-card-media">
                    <img src="${this.escape(image)}" alt="${this.escape(item.itemName)}" onerror="this.src='/images/items/default.svg'">
                    <span class="badge ${isLost ? 'badge-lost' : 'badge-available'} item-badge-top">
                        <i class="fas ${isLost ? 'fa-search' : 'fa-check-circle'}"></i> ${isLost ? 'LOST' : 'FOUND'}
                    </span>
                </div>
                <div class="item-card-body">
                    <div class="item-meta-header">
                        <span class="badge badge-cat">
                            <i class="fas fa-tag"></i> ${this.formatCategory(item.category)}
                        </span>
                        <span style="font-size: 0.775rem; color: var(--slate-400); font-weight: 500;">
                            ${this.formatDate(date)}
                        </span>
                    </div>
                    <h3 class="item-title">${this.escape(item.itemName)}</h3>
                    <p class="item-description">${this.escape(item.description)}</p>
                    <div class="item-meta-list">
                        <div class="item-meta-item">
                            <i class="fas fa-map-marker-alt" style="color: var(--primary-600);"></i>
                            <span><strong>Location:</strong> ${this.escape(loc)}</span>
                        </div>
                        <div class="item-meta-item">
                            <i class="fas fa-palette" style="color: var(--slate-500);"></i>
                            <span><strong>Features:</strong> ${this.escape(item.colorFeatures)}</span>
                        </div>
                        ${!isLost ? `
                        <div class="item-meta-item" style="color: var(--primary-700);">
                            <i class="fas fa-shield-alt"></i>
                            <span><strong>Custody:</strong> ${this.escape(item.storageLocation)}</span>
                        </div>
                        ` : ''}
                    </div>
                    <div class="item-card-footer">
                        <span class="badge ${badgeClass}">${statusLabel}</span>
                        <div style="display: flex; gap: 0.4rem;">
                            <button class="btn btn-sm btn-outline-primary" onclick="App.viewItemDetails('${type}', ${id})">
                                <i class="fas fa-eye"></i> Details
                            </button>
                            ${!isLost && item.status !== 'RETURNED' ? `
                                <button class="btn btn-sm btn-purple" onclick="App.openClaimModal(${id}, '${this.escape(item.itemName)}')">
                                    <i class="fas fa-hand-holding-heart"></i> Claim
                                </button>
                            ` : ''}
                        </div>
                    </div>
                </div>
            </div>
        `;
    },

    // --- ITEM DETAILS MODAL ---
    async viewItemDetails(type, id) {
        try {
            const res = await this.api(`/items/${type}/${id}`);
            if (!res.success || !res.data) {
                this.showToast('Item details could not be loaded.', 'error');
                return;
            }

            const item = res.data;
            const isLost = type === 'lost';
            const date = isLost ? item.dateLost : item.dateFound;
            const loc = isLost ? item.locationLost : item.locationFound;
            const image = item.imageUrl || '/images/items/default.svg';

            document.getElementById('itemDetailModalTitle').innerText = item.itemName;
            document.getElementById('itemDetailModalSubtitle').innerText = `${this.formatCategory(item.category)} • Reported ${this.formatDate(item.createdAt)}`;

            const body = document.getElementById('itemDetailModalBody');
            body.innerHTML = `
                <div style="display: grid; grid-template-columns: 200px 1fr; gap: 1.5rem; margin-bottom: 1.5rem; align-items: start;">
                    <div style="width: 200px; height: 180px; border-radius: var(--radius-lg); overflow: hidden; background: var(--slate-900);">
                        <img src="${this.escape(image)}" style="width: 100%; height: 100%; object-fit: cover;" onerror="this.src='/images/items/default.svg'">
                    </div>
                    <div>
                        <div style="display: flex; gap: 0.5rem; margin-bottom: 0.75rem; flex-wrap: wrap;">
                            <span class="badge ${isLost ? 'badge-lost' : 'badge-available'}">${isLost ? 'REPORTED LOST' : 'DISCOVERED ON CAMPUS'}</span>
                            <span class="badge badge-cat">${this.formatCategory(item.category)}</span>
                            <span class="badge ${item.status === 'RETURNED' || item.status === 'RESOLVED' ? 'badge-returned' : 'badge-pending'}">${item.status}</span>
                        </div>
                        <h3 style="font-size: 1.35rem; color: var(--slate-900); margin-bottom: 0.5rem;">${this.escape(item.itemName)}</h3>
                        <p style="font-size: 0.925rem; color: var(--slate-600); line-height: 1.6;">${this.escape(item.description)}</p>
                    </div>
                </div>

                <div style="background: var(--slate-50); border: 1px solid var(--slate-200); border-radius: var(--radius-lg); padding: 1.25rem; display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; font-size: 0.875rem;">
                    <div>
                        <p style="color: var(--slate-500); margin-bottom: 0.2rem;"><i class="fas fa-map-marker-alt" style="color: var(--primary-600);"></i> <strong>Campus Location:</strong></p>
                        <p style="color: var(--slate-800); font-weight: 600;">${this.escape(loc)}</p>
                    </div>
                    <div>
                        <p style="color: var(--slate-500); margin-bottom: 0.2rem;"><i class="fas fa-calendar-alt" style="color: var(--purple-600);"></i> <strong>Date of Occurrence:</strong></p>
                        <p style="color: var(--slate-800); font-weight: 600;">${this.formatDate(date)}</p>
                    </div>
                    <div>
                        <p style="color: var(--slate-500); margin-bottom: 0.2rem;"><i class="fas fa-palette"></i> <strong>Color &amp; Features:</strong></p>
                        <p style="color: var(--slate-800); font-weight: 600;">${this.escape(item.colorFeatures)}</p>
                    </div>
                    <div>
                        <p style="color: var(--slate-500); margin-bottom: 0.2rem;"><i class="fas fa-shield-alt" style="color: var(--success);"></i> <strong>${isLost ? 'Reporter Contact:' : 'Safe Storage Desk:'}</strong></p>
                        <p style="color: var(--slate-800); font-weight: 600;">${isLost ? this.escape(item.contactPhone) : this.escape(item.storageLocation)}</p>
                    </div>
                </div>
            `;

            const footer = document.getElementById('itemDetailModalFooter');
            footer.innerHTML = `
                <button class="btn btn-outline" onclick="App.closeModal('itemDetailsModal')">Close</button>
                ${!isLost && item.status !== 'RETURNED' ? `
                    <button class="btn btn-purple" onclick="App.closeModal('itemDetailsModal'); App.openClaimModal(${item.foundId}, '${this.escape(item.itemName)}')">
                        <i class="fas fa-hand-holding-heart"></i> Claim This Item
                    </button>
                ` : isLost ? `
                    <button class="btn btn-primary" onclick="App.closeModal('itemDetailsModal'); App.navigate('report'); App.switchReportTab('found');">
                        <i class="fas fa-plus"></i> Found This Item?
                    </button>
                ` : ''}
            `;

            this.openModal('itemDetailsModal');
        } catch (e) {
            this.showToast('Error loading details: ' + e.message, 'error');
        }
    },

    // --- LOST ITEMS LIST & FILTER ---
    async loadLostItems() {
        const container = document.getElementById('lostItemsGrid');
        if (!container) return;
        container.innerHTML = `<div style="grid-column: 1/-1; text-align: center; padding: 3rem;"><i class="fas fa-spinner fa-spin fa-2x" style="color: var(--primary-600);"></i></div>`;

        const category = document.getElementById('lostFilterCategory')?.value || '';
        const query = document.getElementById('lostFilterSearch')?.value || '';
        const location = document.getElementById('lostFilterLocation')?.value || '';

        let url = '/items/lost?';
        const params = [];
        if (category) params.push(`category=${encodeURIComponent(category)}`);
        if (query) params.push(`query=${encodeURIComponent(query)}`);
        if (location) params.push(`location=${encodeURIComponent(location)}`);
        url += params.join('&');

        try {
            const res = await this.api(url);
            const items = res.data || [];
            document.getElementById('lostItemsCount').innerText = `${items.length} Reports Found`;

            if (items.length === 0) {
                container.innerHTML = `
                    <div style="grid-column: 1/-1; text-align: center; padding: 4rem 1rem; background: var(--white); border-radius: var(--radius-lg); border: 1px dashed var(--slate-300);">
                        <i class="fas fa-clipboard-list fa-3x" style="color: var(--slate-400); margin-bottom: 1rem;"></i>
                        <h3>No matching lost item reports</h3>
                        <p style="color: var(--slate-500); margin-bottom: 1.5rem;">Try adjusting your filters or submit a new missing item report.</p>
                        <button class="btn btn-primary" onclick="App.navigate('report')">Report Lost Item</button>
                    </div>
                `;
                return;
            }

            container.innerHTML = items.map(item => this.createItemCardHtml(item)).join('');
        } catch (e) {
            container.innerHTML = `<div style="grid-column: 1/-1; color: var(--danger); text-align: center;">Error loading items: ${e.message}</div>`;
        }
    },

    // --- FOUND ITEMS LIST & FILTER ---
    async loadFoundItems() {
        const container = document.getElementById('foundItemsGrid');
        if (!container) return;
        container.innerHTML = `<div style="grid-column: 1/-1; text-align: center; padding: 3rem;"><i class="fas fa-spinner fa-spin fa-2x" style="color: var(--primary-600);"></i></div>`;

        const category = document.getElementById('foundFilterCategory')?.value || '';
        const query = document.getElementById('foundFilterSearch')?.value || '';
        const location = document.getElementById('foundFilterLocation')?.value || '';

        let url = '/items/found?';
        const params = [];
        if (category) params.push(`category=${encodeURIComponent(category)}`);
        if (query) params.push(`query=${encodeURIComponent(query)}`);
        if (location) params.push(`location=${encodeURIComponent(location)}`);
        url += params.join('&');

        try {
            const res = await this.api(url);
            const items = res.data || [];
            document.getElementById('foundItemsCount').innerText = `${items.length} Items Listed`;

            if (items.length === 0) {
                container.innerHTML = `
                    <div style="grid-column: 1/-1; text-align: center; padding: 4rem 1rem; background: var(--white); border-radius: var(--radius-lg); border: 1px dashed var(--slate-300);">
                        <i class="fas fa-box-open fa-3x" style="color: var(--slate-400); margin-bottom: 1rem;"></i>
                        <h3>No discovered items matching criteria</h3>
                        <p style="color: var(--slate-500); margin-bottom: 1.5rem;">Have you discovered an unattended item on campus? Report it safely.</p>
                        <button class="btn btn-purple" onclick="App.navigate('report')">Report Found Item</button>
                    </div>
                `;
                return;
            }

            container.innerHTML = items.map(item => this.createItemCardHtml(item)).join('');
        } catch (e) {
            container.innerHTML = `<div style="grid-column: 1/-1; color: var(--danger); text-align: center;">Error loading items: ${e.message}</div>`;
        }
    },

    onPillClick(type, cat) {
        const sel = document.getElementById(`${type}FilterCategory`);
        if (sel) sel.value = cat;
        this.updatePillsUi(type, cat);
        if (type === 'lost') this.loadLostItems();
        else this.loadFoundItems();
    },

    onCategorySelect(type, cat) {
        this.updatePillsUi(type, cat);
        if (type === 'lost') this.loadLostItems();
        else this.loadFoundItems();
    },

    updatePillsUi(type, cat) {
        const container = document.getElementById(`${type}CategoryPills`);
        if (!container) return;
        container.querySelectorAll('.category-pill').forEach(pill => {
            const onclickAttr = pill.getAttribute('onclick') || '';
            if (cat === '' && onclickAttr.includes("''")) {
                pill.classList.add('active');
            } else if (cat && onclickAttr.includes(`'${cat}'`)) {
                pill.classList.add('active');
            } else {
                pill.classList.remove('active');
            }
        });
    },

    resetFilters(type) {
        document.getElementById(`${type}FilterSearch`).value = '';
        document.getElementById(`${type}FilterCategory`).value = '';
        document.getElementById(`${type}FilterLocation`).value = '';
        this.updatePillsUi(type, '');
        if (type === 'lost') this.loadLostItems();
        else this.loadFoundItems();
    },

    // --- SMART MATCHES VIEW ---
    async loadMatches() {
        const container = document.getElementById('matchesListContainer');
        if (!container) return;
        container.innerHTML = `<div style="text-align: center; padding: 3rem;"><i class="fas fa-spinner fa-spin fa-2x" style="color: var(--purple-600);"></i></div>`;

        const url = this.state.currentUser ? '/matches/my' : '/matches/public';
        try {
            const res = await this.api(url);
            const matches = res.data || [];

            if (matches.length === 0) {
                container.innerHTML = `
                    <div style="text-align: center; padding: 4rem 1rem; background: var(--white); border-radius: var(--radius-xl); border: 1px dashed var(--slate-300);">
                        <div style="width: 70px; height: 70px; border-radius: 50%; background: var(--purple-50); color: var(--purple-600); display: flex; align-items: center; justify-content: center; font-size: 2rem; margin: 0 auto 1.25rem;">
                            <i class="fas fa-satellite-dish"></i>
                        </div>
                        <h3>No High-Confidence Matches Detected Yet</h3>
                        <p style="color: var(--slate-500); max-width: 500px; margin: 0 auto 1.5rem;">The Smart Matching Engine continuously scans newly reported lost belongings against found inventories. As soon as a similarity score exceeds 60%, notifications are issued immediately.</p>
                        <button class="btn btn-primary" onclick="App.navigate('report')">File a Report</button>
                    </div>
                `;
                return;
            }

            container.innerHTML = matches.map(m => `
                <div class="match-pair-card">
                    <!-- Lost Side -->
                    <div class="match-side">
                        <img src="${this.escape(m.lostItem.imageUrl || '/images/items/default.svg')}" class="match-side-img" alt="Lost">
                        <div class="match-side-info">
                            <span class="badge badge-lost" style="margin-bottom: 0.35rem;">Reported Lost</span>
                            <h4>${this.escape(m.lostItem.itemName)}</h4>
                            <p style="font-size: 0.8rem; color: var(--slate-600);"><i class="fas fa-map-marker-alt"></i> ${this.escape(m.lostItem.locationLost)}</p>
                            <p style="font-size: 0.8rem; color: var(--slate-500);"><i class="fas fa-calendar-alt"></i> ${this.formatDate(m.lostItem.dateLost)}</p>
                            <p style="font-size: 0.8rem; color: var(--slate-500);"><i class="fas fa-palette"></i> ${this.escape(m.lostItem.colorFeatures)}</p>
                        </div>
                    </div>

                    <!-- Match Center -->
                    <div class="match-center">
                        <div class="score-circle">
                            <span class="num">${m.matchScore}%</span>
                            <span class="lbl">${m.matchStatus} MATCH</span>
                        </div>
                        <button class="btn btn-sm btn-purple" onclick="App.openClaimModal(${m.foundItem.foundId}, '${this.escape(m.foundItem.itemName)}')">
                            Claim Item
                        </button>
                    </div>

                    <!-- Found Side -->
                    <div class="match-side">
                        <img src="${this.escape(m.foundItem.imageUrl || '/images/items/default.svg')}" class="match-side-img" alt="Found">
                        <div class="match-side-info">
                            <span class="badge badge-available" style="margin-bottom: 0.35rem;">Discovered Item</span>
                            <h4>${this.escape(m.foundItem.itemName)}</h4>
                            <p style="font-size: 0.8rem; color: var(--slate-600);"><i class="fas fa-map-marker-alt"></i> ${this.escape(m.foundItem.locationFound)}</p>
                            <p style="font-size: 0.8rem; color: var(--slate-500);"><i class="fas fa-calendar-alt"></i> ${this.formatDate(m.foundItem.dateFound)}</p>
                            <p style="font-size: 0.8rem; color: var(--primary-700);"><i class="fas fa-shield-alt"></i> At: ${this.escape(m.foundItem.storageLocation)}</p>
                        </div>
                    </div>

                    <!-- Comparison breakdown footer -->
                    <div class="match-breakdown-tags">
                        <div>
                            <strong>Algorithm Breakdown:</strong> ${this.escape(m.matchReason)}
                        </div>
                        <div style="display: flex; gap: 0.75rem;">
                            <span>Name: <strong>${m.nameSimilarity}%</strong></span>
                            <span>Location: <strong>${m.locationSimilarity}%</strong></span>
                            <span>Date: <strong>${m.dateScore}%</strong></span>
                            <span>Features: <strong>${m.featureScore}%</strong></span>
                        </div>
                    </div>
                </div>
            `).join('');
        } catch (e) {
            container.innerHTML = `<div style="color: var(--danger); text-align: center;">Error loading matches: ${e.message}</div>`;
        }
    },

    // --- REPORTING WIZARD ---
    initReportForm() {
        if (!this.state.currentUser) {
            this.showToast('Please sign in or register to submit a lost or found report.', 'info');
            this.openModal('loginModal');
        }
        // Set today's date default
        const today = new Date().toISOString().split('T')[0];
        const lostDate = document.getElementById('reportLostDate');
        const foundDate = document.getElementById('reportFoundDate');
        if (lostDate) lostDate.value = today;
        if (foundDate) foundDate.value = today;

        if (this.state.currentUser) {
            const phoneInput = document.getElementById('reportLostContact');
            if (phoneInput && this.state.currentUser.phoneNumber) {
                phoneInput.value = this.state.currentUser.phoneNumber;
            }
        }
    },

    switchReportTab(type) {
        const lostTab = document.getElementById('tabReportLost');
        const foundTab = document.getElementById('tabReportFound');
        const lostForm = document.getElementById('formReportLost');
        const foundForm = document.getElementById('formReportFound');

        if (type === 'lost') {
            lostTab.className = 'btn btn-sm btn-primary';
            foundTab.className = 'btn btn-sm btn-outline';
            lostForm.style.display = 'block';
            foundForm.style.display = 'none';
        } else {
            lostTab.className = 'btn btn-sm btn-outline';
            foundTab.className = 'btn btn-sm btn-purple';
            lostForm.style.display = 'none';
            foundForm.style.display = 'block';
        }
    },

    async handleFileUpload(fileInputId, previewImgId) {
        const input = document.getElementById(fileInputId);
        if (!input || !input.files || !input.files[0]) return null;

        const file = input.files[0];
        const formData = new FormData();
        formData.append('file', file);

        try {
            const res = await this.api('/upload', 'POST', formData, true);
            if (res.success && res.data && res.data.url) {
                const preview = document.getElementById(previewImgId);
                if (preview) {
                    preview.src = res.data.url;
                    preview.style.display = 'block';
                }
                return res.data.url;
            }
        } catch (e) {
            this.showToast('Image upload failed: ' + e.message, 'error');
        }
        return null;
    },

    async submitLostReport() {
        if (!this.state.currentUser) {
            this.openModal('loginModal');
            return;
        }

        const name = document.getElementById('reportLostName').value.trim();
        const category = document.getElementById('reportLostCategory').value;
        const description = document.getElementById('reportLostDesc').value.trim();
        const dateLost = document.getElementById('reportLostDate').value;
        const locationLost = document.getElementById('reportLostLocation').value.trim();
        const colorFeatures = document.getElementById('reportLostFeatures').value.trim();
        const contactPhone = document.getElementById('reportLostContact').value.trim();

        if (!name || !category || !description || !dateLost || !locationLost || !colorFeatures || !contactPhone) {
            this.showToast('Please fill in all required fields.', 'error');
            return;
        }

        let imageUrl = document.getElementById('previewLostImg')?.src || null;
        if (imageUrl && imageUrl.startsWith('data:')) imageUrl = null;

        // Check if new file selected
        const fileInput = document.getElementById('reportLostImageFile');
        if (fileInput && fileInput.files && fileInput.files[0]) {
            const uploadedUrl = await this.handleFileUpload('reportLostImageFile', 'previewLostImg');
            if (uploadedUrl) imageUrl = uploadedUrl;
        }

        if (!imageUrl || imageUrl.includes('default.svg')) {
            imageUrl = this.getDefaultCategoryImage(category);
        }

        const payload = {
            itemName: name,
            category,
            description,
            dateLost,
            locationLost,
            colorFeatures,
            contactPhone,
            imageUrl
        };

        try {
            const res = await this.api('/items/lost', 'POST', payload);
            if (res.success) {
                this.showToast('Lost item reported! Smart matching scan completed.', 'success');
                this.loadStats();
                this.navigate('lost');
            }
        } catch (e) {
            this.showToast('Failed to submit report: ' + e.message, 'error');
        }
    },

    async submitFoundReport() {
        if (!this.state.currentUser) {
            this.openModal('loginModal');
            return;
        }

        const name = document.getElementById('reportFoundName').value.trim();
        const category = document.getElementById('reportFoundCategory').value;
        const description = document.getElementById('reportFoundDesc').value.trim();
        const dateFound = document.getElementById('reportFoundDate').value;
        const locationFound = document.getElementById('reportFoundLocation').value.trim();
        const colorFeatures = document.getElementById('reportFoundFeatures').value.trim();
        const storageLocation = document.getElementById('reportFoundStorage').value.trim();

        if (!name || !category || !description || !dateFound || !locationFound || !colorFeatures || !storageLocation) {
            this.showToast('Please fill in all required fields.', 'error');
            return;
        }

        let imageUrl = document.getElementById('previewFoundImg')?.src || null;
        if (imageUrl && imageUrl.startsWith('data:')) imageUrl = null;

        const fileInput = document.getElementById('reportFoundImageFile');
        if (fileInput && fileInput.files && fileInput.files[0]) {
            const uploadedUrl = await this.handleFileUpload('reportFoundImageFile', 'previewFoundImg');
            if (uploadedUrl) imageUrl = uploadedUrl;
        }

        if (!imageUrl || imageUrl.includes('default.svg')) {
            imageUrl = this.getDefaultCategoryImage(category);
        }

        const payload = {
            itemName: name,
            category,
            description,
            dateFound,
            locationFound,
            colorFeatures,
            storageLocation,
            imageUrl
        };

        try {
            const res = await this.api('/items/found', 'POST', payload);
            if (res.success) {
                this.showToast('Found item reported! Thank you for cataloging campus items.', 'success');
                this.loadStats();
                this.navigate('found');
            }
        } catch (e) {
            this.showToast('Failed to submit report: ' + e.message, 'error');
        }
    },

    getDefaultCategoryImage(cat) {
        switch (cat) {
            case 'WALLETS': return '/images/items/wallet.svg';
            case 'ID_CARD': return '/images/items/idcard.svg';
            case 'ELECTRONICS': return '/images/items/mouse.svg';
            case 'BAGS': return '/images/items/backpack.svg';
            case 'KEYS': return '/images/items/keys.svg';
            default: return '/images/items/default.svg';
        }
    },

    // --- CLAIM SYSTEM ---
    openClaimModal(foundId, itemName) {
        if (!this.state.currentUser) {
            this.showToast('Please sign in to submit an ownership claim.', 'info');
            this.openModal('loginModal');
            return;
        }

        document.getElementById('claimFoundId').value = foundId;
        document.getElementById('claimItemNameDisplay').innerText = itemName;
        const dateInput = document.getElementById('claimApproxDate');
        if (dateInput) dateInput.value = new Date().toISOString().split('T')[0];

        this.openModal('claimModal');
    },

    async submitClaim() {
        const foundId = document.getElementById('claimFoundId').value;
        const descriptionProof = document.getElementById('claimDescProof').value.trim();
        const uniqueFeatures = document.getElementById('claimUniqueFeatures').value.trim();
        const approxDateLost = document.getElementById('claimApproxDate').value;
        const approxLocationLost = document.getElementById('claimApproxLoc').value.trim();
        const supportingProof = document.getElementById('claimSupportingProof').value.trim();

        if (!descriptionProof || !uniqueFeatures || !approxDateLost || !approxLocationLost) {
            this.showToast('Please provide all required identification details.', 'error');
            return;
        }

        const payload = {
            foundId: parseInt(foundId),
            itemDescriptionProof: descriptionProof,
            uniqueIdentifyingFeatures: uniqueFeatures,
            approxDateLost,
            approxLocationLost,
            supportingProofDetails: supportingProof
        };

        try {
            const res = await this.api('/claims', 'POST', payload);
            if (res.success) {
                this.showToast('Ownership claim submitted! Campus Administration will verify.', 'success');
                this.closeModal('claimModal');
                this.loadStats();
                this.navigate('user-dashboard');
            }
        } catch (e) {
            this.showToast(e.message || 'Error submitting claim.', 'error');
        }
    },

    // --- USER DASHBOARD ---
    async loadUserDashboard() {
        const user = this.state.currentUser;
        if (!user) return;

        document.getElementById('userDashName').innerText = user.fullName;
        document.getElementById('userDashDetails').innerText = `${user.role} • ${user.department} • ID: ${user.campusId}`;

        this.switchUserDashTab('my-lost');
    },

    async switchUserDashTab(tabName) {
        document.querySelectorAll('.user-dash-tab').forEach(b => {
            b.classList.toggle('active', b.getAttribute('data-tab') === tabName);
        });

        const container = document.getElementById('userDashContent');
        if (!container) return;
        container.innerHTML = `<div style="text-align: center; padding: 2rem;"><i class="fas fa-spinner fa-spin fa-2x"></i></div>`;

        switch (tabName) {
            case 'my-lost':
                this.renderUserLost(container);
                break;
            case 'my-found':
                this.renderUserFound(container);
                break;
            case 'my-claims':
                this.renderUserClaims(container);
                break;
            case 'my-matches':
                this.renderUserMatches(container);
                break;
            case 'my-notifs':
                this.renderUserNotifications(container);
                break;
        }
    },

    async renderUserLost(container) {
        try {
            const res = await this.api('/items/my/lost');
            const items = res.data || [];
            if (items.length === 0) {
                container.innerHTML = `<div style="text-align: center; padding: 3rem; background: var(--white); border-radius: var(--radius-lg); border: 1px dashed var(--slate-300);">You haven't reported any lost items.</div>`;
                return;
            }

            container.innerHTML = `
                <div class="items-grid">
                    ${items.map(item => `
                        <div class="item-card">
                            <div class="item-card-media">
                                <img src="${this.escape(item.imageUrl || '/images/items/default.svg')}" alt="">
                                <span class="badge badge-lost item-badge-top">LOST REPORT</span>
                            </div>
                            <div class="item-card-body">
                                <div class="item-meta-header">
                                    <span class="badge badge-cat">${this.formatCategory(item.category)}</span>
                                    <span style="font-size: 0.775rem; color: var(--slate-400);">${this.formatDate(item.dateLost)}</span>
                                </div>
                                <h3 class="item-title">${this.escape(item.itemName)}</h3>
                                <p class="item-description">${this.escape(item.description)}</p>
                                <div class="item-meta-list">
                                    <div class="item-meta-item"><span><strong>Location:</strong> ${this.escape(item.locationLost)}</span></div>
                                    <div class="item-meta-item"><span><strong>Features:</strong> ${this.escape(item.colorFeatures)}</span></div>
                                </div>
                                <div class="item-card-footer">
                                    <span class="badge ${item.status === 'RESOLVED' ? 'badge-returned' : 'badge-lost'}">${item.status}</span>
                                    <button class="btn btn-sm btn-outline" onclick="App.deleteUserLost(${item.lostId})">
                                        <i class="fas fa-trash"></i> Delete
                                    </button>
                                </div>
                            </div>
                        </div>
                    `).join('')}
                </div>
            `;
        } catch (e) {
            container.innerHTML = `<div style="color: var(--danger);">Failed to load: ${e.message}</div>`;
        }
    },

    async renderUserFound(container) {
        try {
            const res = await this.api('/items/my/found');
            const items = res.data || [];
            if (items.length === 0) {
                container.innerHTML = `<div style="text-align: center; padding: 3rem; background: var(--white); border-radius: var(--radius-lg); border: 1px dashed var(--slate-300);">You haven't reported any found items.</div>`;
                return;
            }

            container.innerHTML = `
                <div class="items-grid">
                    ${items.map(item => `
                        <div class="item-card">
                            <div class="item-card-media">
                                <img src="${this.escape(item.imageUrl || '/images/items/default.svg')}" alt="">
                                <span class="badge badge-available item-badge-top">FOUND ITEM</span>
                            </div>
                            <div class="item-card-body">
                                <div class="item-meta-header">
                                    <span class="badge badge-cat">${this.formatCategory(item.category)}</span>
                                    <span style="font-size: 0.775rem; color: var(--slate-400);">${this.formatDate(item.dateFound)}</span>
                                </div>
                                <h3 class="item-title">${this.escape(item.itemName)}</h3>
                                <p class="item-description">${this.escape(item.description)}</p>
                                <div class="item-meta-list">
                                    <div class="item-meta-item"><span><strong>Custody:</strong> ${this.escape(item.storageLocation)}</span></div>
                                    <div class="item-meta-item"><span><strong>Location:</strong> ${this.escape(item.locationFound)}</span></div>
                                </div>
                                <div class="item-card-footer">
                                    <span class="badge ${item.status === 'RETURNED' ? 'badge-returned' : 'badge-available'}">${item.status}</span>
                                    <button class="btn btn-sm btn-outline" onclick="App.deleteUserFound(${item.foundId})">
                                        <i class="fas fa-trash"></i> Delete
                                    </button>
                                </div>
                            </div>
                        </div>
                    `).join('')}
                </div>
            `;
        } catch (e) {
            container.innerHTML = `<div style="color: var(--danger);">Failed to load: ${e.message}</div>`;
        }
    },

    async renderUserClaims(container) {
        try {
            const res = await this.api('/claims/my');
            const claims = res.data || [];
            if (claims.length === 0) {
                container.innerHTML = `<div style="text-align: center; padding: 3rem; background: var(--white); border-radius: var(--radius-lg); border: 1px dashed var(--slate-300);">No ownership claims submitted yet.</div>`;
                return;
            }

            container.innerHTML = `
                <div style="display: flex; flex-direction: column; gap: 1.25rem;">
                    ${claims.map(c => {
                        let badgeClass = 'badge-pending';
                        if (c.status === 'APPROVED') badgeClass = 'badge-approved';
                        if (c.status === 'RETURNED') badgeClass = 'badge-returned';
                        if (c.status === 'REJECTED') badgeClass = 'badge-rejected';

                        return `
                            <div style="background: var(--white); border-radius: var(--radius-lg); padding: 1.5rem; border: 1px solid var(--slate-200); box-shadow: var(--shadow-sm);">
                                <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1rem; flex-wrap: wrap; gap: 0.5rem;">
                                    <div>
                                        <span class="badge ${badgeClass}" style="margin-bottom: 0.5rem;">${c.status}</span>
                                        <h3 style="font-size: 1.2rem; color: var(--slate-900);">${this.escape(c.foundItem.itemName)}</h3>
                                        <p style="font-size: 0.85rem; color: var(--slate-500);">Claim Ref #${c.claimId} • Submitted ${this.formatDate(c.submittedAt)}</p>
                                    </div>
                                    <div style="text-align: right;">
                                        <span style="font-size: 0.85rem; color: var(--primary-700); font-weight: 600;"><i class="fas fa-shield-alt"></i> Safe Collection Point:</span><br>
                                        <span style="font-size: 0.85rem; color: var(--slate-700); font-weight: 600;">${this.escape(c.foundItem.storageLocation)}</span>
                                    </div>
                                </div>

                                <div style="background: var(--slate-50); padding: 1rem; border-radius: var(--radius-md); margin-bottom: 1rem; font-size: 0.875rem;">
                                    <p><strong>Your Provided Identifying Details:</strong> ${this.escape(c.uniqueIdentifyingFeatures)}</p>
                                    <p><strong>Estimated Loss Date &amp; Place:</strong> ${this.formatDate(c.approxDateLost)} at ${this.escape(c.approxLocationLost)}</p>
                                    ${c.adminRemarks ? `<p style="margin-top: 0.5rem; color: var(--purple-800);"><strong>Campus Administration Notes:</strong> ${this.escape(c.adminRemarks)}</p>` : ''}
                                </div>

                                <!-- Recovery Progress Stepper -->
                                <div style="margin-top: 1rem; border-top: 1px solid var(--slate-100); padding-top: 1rem;">
                                    <div style="display: flex; justify-content: space-between; font-size: 0.75rem; font-weight: 700; color: var(--slate-500); margin-bottom: 0.4rem; flex-wrap: wrap; gap: 0.5rem;">
                                        <span style="${c.status !== 'REJECTED' ? 'color: var(--primary-600);' : ''}"><i class="fas fa-check-circle"></i> 1. Claim Filed</span>
                                        <span style="${c.status === 'UNDER_VERIFICATION' || c.status === 'APPROVED' || c.status === 'RETURNED' ? 'color: var(--primary-600);' : ''}"><i class="fas fa-check-circle"></i> 2. Security Audit</span>
                                        <span style="${c.status === 'APPROVED' || c.status === 'RETURNED' ? 'color: var(--primary-600);' : ''}"><i class="fas fa-check-circle"></i> 3. Approved for Pickup</span>
                                        <span style="${c.status === 'RETURNED' ? 'color: var(--success);' : ''}"><i class="fas fa-check-circle"></i> 4. Handover Complete</span>
                                    </div>
                                </div>
                            </div>
                        `;
                    }).join('')}
                </div>
            `;
        } catch (e) {
            container.innerHTML = `<div style="color: var(--danger);">Failed to load claims: ${e.message}</div>`;
        }
    },

    async renderUserMatches(container) {
        try {
            const res = await this.api('/matches/my');
            const matches = res.data || [];
            if (matches.length === 0) {
                container.innerHTML = `<div style="text-align: center; padding: 3rem; background: var(--white); border-radius: var(--radius-lg); border: 1px dashed var(--slate-300);">No matching items found for your reports yet.</div>`;
                return;
            }

            container.innerHTML = `
                <div style="display: flex; flex-direction: column; gap: 1.25rem;">
                    ${matches.map(m => `
                        <div style="background: var(--white); border-radius: var(--radius-xl); padding: 1.5rem; border: 1px solid var(--purple-200); box-shadow: var(--shadow-sm); display: flex; align-items: center; justify-content: space-between; gap: 1.5rem; flex-wrap: wrap;">
                            <div style="display: flex; align-items: center; gap: 1rem;">
                                <div class="score-circle" style="width: 70px; height: 70px; margin: 0;">
                                    <span class="num" style="font-size: 1.2rem;">${m.matchScore}%</span>
                                    <span class="lbl" style="font-size: 0.6rem;">CONFIDENCE</span>
                                </div>
                                <div>
                                    <h4 style="font-size: 1.15rem; color: var(--slate-900);">Matching Item: ${this.escape(m.foundItem.itemName)}</h4>
                                    <p style="font-size: 0.85rem; color: var(--slate-600);"><i class="fas fa-map-marker-alt"></i> Discovered at: ${this.escape(m.foundItem.locationFound)}</p>
                                    <p style="font-size: 0.85rem; color: var(--primary-700);"><i class="fas fa-shield-alt"></i> Safe Collection: ${this.escape(m.foundItem.storageLocation)}</p>
                                    <p style="font-size: 0.8rem; color: var(--purple-700); margin-top: 0.25rem;"><strong>Match Reason:</strong> ${this.escape(m.matchReason)}</p>
                                </div>
                            </div>
                            <div>
                                <button class="btn btn-purple" onclick="App.openClaimModal(${m.foundItem.foundId}, '${this.escape(m.foundItem.itemName)}')">
                                    <i class="fas fa-hand-holding-heart"></i> Claim This Item
                                </button>
                            </div>
                        </div>
                    `).join('')}
                </div>
            `;
        } catch (e) {
            container.innerHTML = `<div style="color: var(--danger);">Failed to load matches: ${e.message}</div>`;
        }
    },

    async renderUserNotifications(container) {
        try {
            const res = await this.api('/notifications');
            const notifs = res.data || [];
            if (notifs.length === 0) {
                container.innerHTML = `<div style="text-align: center; padding: 3rem; background: var(--white); border-radius: var(--radius-lg); border: 1px dashed var(--slate-300);">You have no notifications.</div>`;
                return;
            }

            container.innerHTML = `
                <div style="display: flex; justify-content: flex-end; margin-bottom: 1rem;">
                    <button class="btn btn-sm btn-outline" onclick="App.markAllNotifsRead()">
                        <i class="fas fa-check-double"></i> Mark All as Read
                    </button>
                </div>
                <div style="display: flex; flex-direction: column; gap: 0.75rem;">
                    ${notifs.map(n => `
                        <div style="background: ${n.isRead ? 'var(--white)' : 'var(--purple-50)'}; border-left: 4px solid ${n.isRead ? 'var(--slate-300)' : 'var(--purple-600)'}; padding: 1rem 1.25rem; border-radius: var(--radius-md); box-shadow: var(--shadow-sm); display: flex; justify-content: space-between; align-items: center; gap: 1rem;">
                            <div>
                                <h4 style="font-size: 0.95rem; font-weight: 700; color: var(--slate-900); margin-bottom: 0.25rem;">${this.escape(n.title)}</h4>
                                <p style="font-size: 0.85rem; color: var(--slate-600);">${this.escape(n.message)}</p>
                                <span style="font-size: 0.75rem; color: var(--slate-400);">${this.formatDate(n.createdAt)}</span>
                            </div>
                            ${!n.isRead ? `
                                <button class="btn btn-sm btn-outline" onclick="App.markNotifRead(${n.notificationId})">
                                    <i class="fas fa-check"></i> Read
                                </button>
                            ` : ''}
                        </div>
                    `).join('')}
                </div>
            `;
        } catch (e) {
            container.innerHTML = `<div style="color: var(--danger);">Failed to load notifications: ${e.message}</div>`;
        }
    },

    async deleteUserLost(id) {
        if (!confirm('Are you sure you want to delete this lost item report?')) return;
        try {
            await this.api(`/items/lost/${id}`, 'DELETE');
            this.showToast('Report deleted successfully', 'success');
            this.switchUserDashTab('my-lost');
        } catch (e) {
            this.showToast(e.message, 'error');
        }
    },

    async deleteUserFound(id) {
        if (!confirm('Are you sure you want to delete this found item report?')) return;
        try {
            await this.api(`/items/found/${id}`, 'DELETE');
            this.showToast('Report deleted successfully', 'success');
            this.switchUserDashTab('my-found');
        } catch (e) {
            this.showToast(e.message, 'error');
        }
    },

    // --- NOTIFICATIONS ---
    async loadNotificationCount() {
        if (!this.state.token) return;
        try {
            const res = await this.api('/notifications/unread-count');
            if (res.success && res.data) {
                const count = res.data.unreadCount || 0;
                this.state.unreadNotifCount = count;
                const badge = document.getElementById('notifBadge');
                if (badge) {
                    badge.innerText = count;
                    badge.style.display = count > 0 ? 'flex' : 'none';
                }
            }
        } catch (e) {
            console.warn('Notification count check failed:', e);
        }
    },

    async markNotifRead(id) {
        try {
            await this.api(`/notifications/${id}/read`, 'PUT');
            this.loadNotificationCount();
            this.switchUserDashTab('my-notifs');
        } catch (e) {
            this.showToast(e.message, 'error');
        }
    },

    async markAllNotifsRead() {
        try {
            await this.api('/notifications/read-all', 'PUT');
            this.loadNotificationCount();
            this.switchUserDashTab('my-notifs');
            this.showToast('All notifications marked as read', 'success');
        } catch (e) {
            this.showToast(e.message, 'error');
        }
    },

    openNotificationsModal() {
        if (!this.state.currentUser) {
            this.openModal('loginModal');
            return;
        }
        this.navigate('user-dashboard');
        this.switchUserDashTab('my-notifs');
    },

    // --- ADMIN DASHBOARD ---
    async loadAdminDashboard() {
        if (!this.state.currentUser || this.state.currentUser.role !== 'ADMIN') return;
        this.loadStats();
        this.switchAdminTab('claims');
    },

    async switchAdminTab(tabName) {
        document.querySelectorAll('.admin-tab-btn').forEach(b => {
            b.classList.toggle('active', b.getAttribute('data-tab') === tabName);
        });

        const container = document.getElementById('adminTabContent');
        if (!container) return;
        container.innerHTML = `<div style="text-align: center; padding: 2rem;"><i class="fas fa-spinner fa-spin fa-2x"></i></div>`;

        switch (tabName) {
            case 'claims':
                this.renderAdminClaims(container);
                break;
            case 'reports':
                this.renderAdminReports(container);
                break;
            case 'returns':
                this.renderAdminReturns(container);
                break;
            case 'users':
                this.renderAdminUsers(container);
                break;
        }
    },

    async renderAdminClaims(container) {
        try {
            const res = await this.api('/admin/claims');
            const claims = res.data || [];
            if (claims.length === 0) {
                container.innerHTML = `<div style="text-align: center; padding: 3rem; background: var(--white); border-radius: var(--radius-lg); border: 1px dashed var(--slate-300);">No ownership claims pending review.</div>`;
                return;
            }

            container.innerHTML = `
                <div class="table-responsive">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Claim ID</th>
                                <th>Item Name</th>
                                <th>Claimant Details</th>
                                <th>Secret Proof / Identifying Marks</th>
                                <th>Status</th>
                                <th>Verification Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${claims.map(c => `
                                <tr>
                                    <td><strong>#${c.claimId}</strong><br><small style="color: var(--slate-400);">${this.formatDate(c.submittedAt)}</small></td>
                                    <td>
                                        <strong>${this.escape(c.foundItem.itemName)}</strong><br>
                                        <small style="color: var(--slate-500);">Found at: ${this.escape(c.foundItem.locationFound)}</small>
                                    </td>
                                    <td>
                                        <strong>${this.escape(c.claimant.fullName)}</strong> (${c.claimant.role})<br>
                                        <small>ID: ${this.escape(c.claimant.campusId)} • Dept: ${this.escape(c.claimant.department)}</small><br>
                                        <small><i class="fas fa-phone"></i> ${this.escape(c.claimant.phoneNumber)}</small>
                                    </td>
                                    <td style="max-width: 320px;">
                                        <div style="background: var(--purple-50); padding: 0.65rem; border-radius: var(--radius-sm); font-size: 0.8rem; border: 1px solid var(--purple-100);">
                                            <strong>Identifying Details:</strong> ${this.escape(c.uniqueIdentifyingFeatures)}<br>
                                            <strong>Loss Date &amp; Place:</strong> ${this.formatDate(c.approxDateLost)} at ${this.escape(c.approxLocationLost)}<br>
                                            ${c.supportingProofDetails ? `<strong>Supporting:</strong> ${this.escape(c.supportingProofDetails)}` : ''}
                                        </div>
                                    </td>
                                    <td><span class="badge ${c.status === 'APPROVED' ? 'badge-approved' : c.status === 'RETURNED' ? 'badge-returned' : c.status === 'REJECTED' ? 'badge-rejected' : 'badge-pending'}">${c.status}</span></td>
                                    <td>
                                        <div style="display: flex; flex-direction: column; gap: 0.4rem;">
                                            ${c.status !== 'APPROVED' && c.status !== 'RETURNED' ? `
                                                <button class="btn btn-sm btn-success" onclick="App.adminReviewClaim(${c.claimId}, 'APPROVED')">
                                                    <i class="fas fa-check"></i> Approve Claim
                                                </button>
                                                <button class="btn btn-sm btn-outline" onclick="App.adminReviewClaim(${c.claimId}, 'UNDER_VERIFICATION')">
                                                    <i class="fas fa-search"></i> Verify
                                                </button>
                                                <button class="btn btn-sm btn-danger" onclick="App.adminReviewClaim(${c.claimId}, 'REJECTED')">
                                                    <i class="fas fa-times"></i> Reject
                                                </button>
                                            ` : ''}
                                            ${c.status === 'APPROVED' ? `
                                                <button class="btn btn-sm btn-purple" onclick="App.openHandoverModal(${c.claimId}, '${this.escape(c.foundItem.itemName)}', '${this.escape(c.claimant.fullName)}')">
                                                    <i class="fas fa-handshake"></i> Complete Handover
                                                </button>
                                            ` : ''}
                                        </div>
                                    </td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                </div>
            `;
        } catch (e) {
            container.innerHTML = `<div style="color: var(--danger);">Failed to load admin claims: ${e.message}</div>`;
        }
    },

    async renderAdminReports(container) {
        try {
            const [lostRes, foundRes] = await Promise.all([
                this.api('/items/lost'),
                this.api('/items/found')
            ]);
            const lost = lostRes.data || [];
            const found = foundRes.data || [];

            container.innerHTML = `
                <div style="display: flex; flex-direction: column; gap: 2rem;">
                    <div>
                        <h3 style="font-size: 1.15rem; margin-bottom: 0.75rem;">Discovered / Found Items Inventory (${found.length})</h3>
                        <div class="table-responsive">
                            <table class="data-table">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Item Name</th>
                                        <th>Category</th>
                                        <th>Found Location</th>
                                        <th>Safe Storage Point</th>
                                        <th>Status</th>
                                        <th>Action</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    ${found.map(f => `
                                        <tr>
                                            <td>#${f.foundId}</td>
                                            <td><strong>${this.escape(f.itemName)}</strong></td>
                                            <td><span class="badge badge-cat">${this.formatCategory(f.category)}</span></td>
                                            <td>${this.escape(f.locationFound)}</td>
                                            <td>${this.escape(f.storageLocation)}</td>
                                            <td><span class="badge ${f.status === 'RETURNED' ? 'badge-returned' : 'badge-available'}">${f.status}</span></td>
                                            <td>
                                                <button class="btn btn-sm btn-outline" style="color: var(--danger);" onclick="App.adminDeleteFound(${f.foundId})">
                                                    <i class="fas fa-trash"></i> Remove
                                                </button>
                                            </td>
                                        </tr>
                                    `).join('')}
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <div>
                        <h3 style="font-size: 1.15rem; margin-bottom: 0.75rem;">Reported Lost Belongings (${lost.length})</h3>
                        <div class="table-responsive">
                            <table class="data-table">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Item Name</th>
                                        <th>Category</th>
                                        <th>Location Lost</th>
                                        <th>Contact Phone</th>
                                        <th>Status</th>
                                        <th>Action</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    ${lost.map(l => `
                                        <tr>
                                            <td>#${l.lostId}</td>
                                            <td><strong>${this.escape(l.itemName)}</strong></td>
                                            <td><span class="badge badge-cat">${this.formatCategory(l.category)}</span></td>
                                            <td>${this.escape(l.locationLost)}</td>
                                            <td>${this.escape(l.contactPhone)}</td>
                                            <td><span class="badge ${l.status === 'RESOLVED' ? 'badge-returned' : 'badge-lost'}">${l.status}</span></td>
                                            <td>
                                                <button class="btn btn-sm btn-outline" style="color: var(--danger);" onclick="App.adminDeleteLost(${l.lostId})">
                                                    <i class="fas fa-trash"></i> Remove
                                                </button>
                                            </td>
                                        </tr>
                                    `).join('')}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            `;
        } catch (e) {
            container.innerHTML = `<div style="color: var(--danger);">Failed to load reports: ${e.message}</div>`;
        }
    },

    async renderAdminReturns(container) {
        try {
            const res = await this.api('/admin/returns');
            const returns = res.data || [];
            if (returns.length === 0) {
                container.innerHTML = `<div style="text-align: center; padding: 3rem; background: var(--white); border-radius: var(--radius-lg); border: 1px dashed var(--slate-300);">No items returned yet.</div>`;
                return;
            }

            container.innerHTML = `
                <div class="table-responsive">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Return ID</th>
                                <th>Item Returned</th>
                                <th>Returned To (Owner)</th>
                                <th>Verified By Admin</th>
                                <th>Handover Timestamp</th>
                                <th>Handover Notes</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${returns.map(r => `
                                <tr>
                                    <td><strong>#RET-${r.returnId}</strong></td>
                                    <td>
                                        <strong>${this.escape(r.foundItem.itemName)}</strong><br>
                                        <span class="badge badge-returned">SUCCESSFULLY RETURNED</span>
                                    </td>
                                    <td>
                                        <strong>${this.escape(r.returnedToUser.fullName)}</strong><br>
                                        <small>ID: ${this.escape(r.returnedToUser.campusId)} • Phone: ${this.escape(r.returnedToUser.phoneNumber)}</small>
                                    </td>
                                    <td>
                                        <strong>${this.escape(r.processedByAdmin.fullName)}</strong><br>
                                        <small>${this.escape(r.processedByAdmin.department)}</small>
                                    </td>
                                    <td>${this.formatDate(r.handoverTimestamp)}</td>
                                    <td style="max-width: 280px; font-size: 0.825rem;">${this.escape(r.handoverNotes)}</td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                </div>
            `;
        } catch (e) {
            container.innerHTML = `<div style="color: var(--danger);">Failed to load returns: ${e.message}</div>`;
        }
    },

    async renderAdminUsers(container) {
        try {
            const res = await this.api('/admin/users');
            const users = res.data || [];

            container.innerHTML = `
                <div class="table-responsive">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>User ID</th>
                                <th>Full Name</th>
                                <th>Email</th>
                                <th>Campus ID</th>
                                <th>Department</th>
                                <th>Role</th>
                                <th>Phone</th>
                                <th>Joined</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${users.map(u => `
                                <tr>
                                    <td>#${u.userId}</td>
                                    <td><strong>${this.escape(u.fullName)}</strong></td>
                                    <td>${this.escape(u.collegeEmail)}</td>
                                    <td><code>${this.escape(u.campusId)}</code></td>
                                    <td>${this.escape(u.department)}</td>
                                    <td><span class="badge ${u.role === 'ADMIN' ? 'badge-cat' : u.role === 'STAFF' ? 'badge-available' : 'badge-pending'}">${u.role}</span></td>
                                    <td>${this.escape(u.phoneNumber)}</td>
                                    <td>${this.formatDate(u.createdAt)}</td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                </div>
            `;
        } catch (e) {
            container.innerHTML = `<div style="color: var(--danger);">Failed to load users: ${e.message}</div>`;
        }
    },

    async adminReviewClaim(claimId, status) {
        let remarks = prompt(`Enter administration remarks for marking this claim as ${status}:`, status === 'APPROVED' ? 'Proof verified against records.' : 'Additional proof required.');
        if (remarks === null) return;

        try {
            await this.api(`/admin/claims/${claimId}/review`, 'PUT', { status, adminRemarks: remarks });
            this.showToast(`Claim marked as ${status}.`, 'success');
            this.loadStats();
            this.switchAdminTab('claims');
        } catch (e) {
            this.showToast(e.message, 'error');
        }
    },

    openHandoverModal(claimId, itemName, claimantName) {
        document.getElementById('handoverClaimId').value = claimId;
        document.getElementById('handoverItemNameDisplay').innerText = itemName;
        document.getElementById('handoverClaimantDisplay').innerText = claimantName;
        this.openModal('handoverModal');
    },

    async submitHandover() {
        const claimId = document.getElementById('handoverClaimId').value;
        const notes = document.getElementById('handoverNotesInput').value.trim();

        if (!notes) {
            this.showToast('Please enter handover notes (e.g. Identity ID card verified).', 'error');
            return;
        }

        try {
            await this.api('/admin/returns', 'POST', { claimId: parseInt(claimId), handoverNotes: notes });
            this.showToast('Item physical handover confirmed! Marked as RETURNED.', 'success');
            this.closeModal('handoverModal');
            this.loadStats();
            this.switchAdminTab('claims');
        } catch (e) {
            this.showToast(e.message, 'error');
        }
    },

    async adminDeleteLost(id) {
        if (!confirm('Remove this lost item report?')) return;
        try {
            await this.api(`/admin/items/lost/${id}`, 'DELETE');
            this.showToast('Lost item removed.', 'success');
            this.switchAdminTab('reports');
        } catch (e) {
            this.showToast(e.message, 'error');
        }
    },

    async adminDeleteFound(id) {
        if (!confirm('Remove this found item report?')) return;
        try {
            await this.api(`/admin/items/found/${id}`, 'DELETE');
            this.showToast('Found item removed.', 'success');
            this.switchAdminTab('reports');
        } catch (e) {
            this.showToast(e.message, 'error');
        }
    },

    // --- MODAL & UI HELPERS ---
    openModal(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) modal.classList.add('show');
    },

    closeModal(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) modal.classList.remove('show');
    },

    showToast(message, type = 'info') {
        const container = document.getElementById('toastContainer');
        if (!container) return;

        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        const icon = type === 'success' ? 'fa-check-circle' : type === 'error' ? 'fa-exclamation-circle' : 'fa-info-circle';
        toast.innerHTML = `<i class="fas ${icon}"></i> <span>${this.escape(message)}</span>`;
        container.appendChild(toast);

        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateY(10px)';
            toast.style.transition = 'all 0.3s ease';
            setTimeout(() => toast.remove(), 300);
        }, 4000);
    },

    formatCategory(cat) {
        if (!cat) return 'General';
        return cat.replace('_', ' ');
    },

    formatDate(str) {
        if (!str) return 'N/A';
        try {
            const d = new Date(str);
            return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
        } catch (e) {
            return str;
        }
    },

    escape(str) {
        if (str === null || str === undefined) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    },

    bindEvents() {
        window.addEventListener('hashchange', () => this.handleHashChange());

        // Close modal on click outside modal-card
        document.querySelectorAll('.modal-backdrop').forEach(modal => {
            modal.addEventListener('click', (e) => {
                if (e.target === modal) {
                    modal.classList.remove('show');
                }
            });
        });

        // Hero quick search
        const heroSearchBtn = document.getElementById('heroSearchBtn');
        if (heroSearchBtn) {
            heroSearchBtn.addEventListener('click', () => {
                const query = document.getElementById('heroSearchInput')?.value || '';
                const type = document.getElementById('heroSearchType')?.value || 'found';
                const cat = document.getElementById('heroSearchCategory')?.value || '';
                const loc = document.getElementById('heroSearchLocation')?.value || '';

                if (type === 'lost') {
                    this.navigate('lost');
                    if (document.getElementById('lostFilterSearch')) document.getElementById('lostFilterSearch').value = query;
                    if (document.getElementById('lostFilterCategory')) document.getElementById('lostFilterCategory').value = cat;
                    if (document.getElementById('lostFilterLocation')) document.getElementById('lostFilterLocation').value = loc;
                    this.updatePillsUi('lost', cat);
                    this.loadLostItems();
                } else {
                    this.navigate('found');
                    if (document.getElementById('foundFilterSearch')) document.getElementById('foundFilterSearch').value = query;
                    if (document.getElementById('foundFilterCategory')) document.getElementById('foundFilterCategory').value = cat;
                    if (document.getElementById('foundFilterLocation')) document.getElementById('foundFilterLocation').value = loc;
                    this.updatePillsUi('found', cat);
                    this.loadFoundItems();
                }
            });
        }
    }
};

document.addEventListener('DOMContentLoaded', () => {
    App.init();
});
