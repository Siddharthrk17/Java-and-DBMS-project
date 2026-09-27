// AERO•SYS 66 - Aeroponic Farm Management & Nutrient Injection System
const API_BASE = '/api';

let currentTab = 'dashboard';
let towersCache = [];
let productsCache = [];
let contactsCache = [];

document.addEventListener('DOMContentLoaded', () => {
    initNavigation();
    loadAllData();
});

// Navigation Handling
function initNavigation() {
    const navItems = document.querySelectorAll('.nav-item');
    navItems.forEach(item => {
        item.addEventListener('click', () => {
            const tabId = item.getAttribute('data-tab');
            switchTab(tabId);
        });
    });
}

function switchTab(tabId) {
    currentTab = tabId;
    document.querySelectorAll('.nav-item').forEach(i => i.classList.remove('active'));
    document.querySelectorAll('.tab-pane').forEach(p => p.classList.remove('active'));

    const activeNav = document.querySelector(`.nav-item[data-tab="${tabId}"]`);
    if (activeNav) activeNav.classList.add('active');

    const activePane = document.getElementById(`pane-${tabId}`);
    if (activePane) activePane.classList.add('active');

    // Update Topbar
    const titleMap = {
        'dashboard': ['Overview & Telemetry', 'Real-time root-zone humidity, automated mist nutrient cycles & farm telemetry'],
        'towers': ['Aeroponic Towers Fleet', 'Monitor and configure modular vertical aeroponic production towers'],
        'mist-cycles': ['Nutrient Misting Cycles Log', 'Autonomous root-zone mist injection logs with nozzle activation timestamps'],
        'harvests': ['Harvest & Crop Yield', 'Harvested organic produce inventory and batch records for grocery chains'],
        'procurement': ['Purchase Orders & Bills', 'Order concentrated mineral liquid nutrients from supplier and convert to bills'],
        'sales': ['Sales & Grocery Billing', 'Issue harvest supply orders and bill supermarket clients with customer invoices'],
        'journals': ['Double-Entry General Ledger', 'Strict balancing double-entry ledger records (Produce Sales, Nutrients, Electricity)'],
        'reports': ['Financial Statements (P&L & Balance Sheet)', 'Produce revenues vs mineral/power expenses & asset/liability statements'],
        'budgets': ['Zone Budget Performance', 'Allocates operational funds against actual power/nutrient costs per zone'],
        'master-data': ['Master Data & Chart of Accounts', 'Manage Supermarket contacts, Nutrient suppliers, Products & COA'],
        'api-tester': ['REST API Console', 'Live interactive test console for all specified aeroponic microservice endpoints']
    };

    if (titleMap[tabId]) {
        document.getElementById('page-title').innerText = titleMap[tabId][0];
        document.getElementById('page-desc').innerText = titleMap[tabId][1];
    }

    refreshCurrentTab();
}

function refreshCurrentTab() {
    loadOverviewKpis();
    switch (currentTab) {
        case 'dashboard':
            loadDashboard();
            break;
        case 'towers':
            loadTowers();
            break;
        case 'mist-cycles':
            loadMistCycles();
            break;
        case 'harvests':
            loadHarvests();
            break;
        case 'procurement':
            loadProcurement();
            break;
        case 'sales':
            loadSales();
            break;
        case 'journals':
            loadJournals();
            break;
        case 'reports':
            loadReports();
            break;
        case 'budgets':
            loadBudgets();
            break;
        case 'master-data':
            loadMasterData();
            break;
    }
}

function loadAllData() {
    loadOverviewKpis();
    loadDashboard();
    fetchTowersCache();
    fetchProductsCache();
    fetchContactsCache();
}

// Overview KPIs
async function loadOverviewKpis() {
    try {
        const res = await fetch(`${API_BASE}/reports/overview`);
        if (!res.ok) return;
        const stats = await res.json();
        
        document.getElementById('kpi-towers').innerText = `${stats.activeTowers || 0} / ${stats.totalTowers || 0}`;
        document.getElementById('kpi-mists').innerText = stats.totalMistCycles || 0;
        document.getElementById('kpi-harvests').innerText = `${stats.totalHarvestKg || 0} kg`;
        
        const netProfit = Number(stats.netProfit || 0);
        document.getElementById('kpi-profit').innerText = `$${netProfit.toLocaleString('en-US', { minimumFractionDigits: 2 })}`;
    } catch (e) {
        console.error('Failed to load KPIs:', e);
    }
}

// 1. Dashboard View
async function loadDashboard() {
    try {
        const [towersRes, telemetryRes, mistsRes, journalsRes] = await Promise.all([
            fetch(`${API_BASE}/aeroponics/towers`),
            fetch(`${API_BASE}/aeroponics/telemetry/latest`),
            fetch(`${API_BASE}/aeroponics/mist-cycles`),
            fetch(`${API_BASE}/journal-entries`)
        ]);

        const towers = await towersRes.json();
        const telemetryList = await telemetryRes.json();
        const mists = await mistsRes.json();
        const journals = await journalsRes.json();

        // Build telemetry map: towerId -> latest telemetry
        const telMap = {};
        telemetryList.forEach(t => {
            if (t.tower && t.tower.towerId) {
                telMap[t.tower.towerId] = t;
            }
        });

        // Render Tower Cards
        const cardsGrid = document.getElementById('dashboard-tower-cards');
        cardsGrid.innerHTML = '';

        towers.forEach(tw => {
            const tel = telMap[tw.towerId] || {};
            const hum = tel.rootHumidityPct != null ? Number(tel.rootHumidityPct).toFixed(1) : '--';
            const ppm = tel.nutrientPpm != null ? Number(tel.nutrientPpm).toFixed(0) : '--';
            
            const isHumLow = tel.rootHumidityPct != null && tel.rootHumidityPct < 70;
            const isPpmLow = tel.nutrientPpm != null && tel.nutrientPpm < 800;

            const card = document.createElement('div');
            card.className = 'tower-card';
            card.innerHTML = `
                <div class="tower-top">
                    <span class="tower-code">${tw.towerCode}</span>
                    <span class="badge badge-${tw.status.toLowerCase()}">${tw.status}</span>
                </div>
                <div class="tower-crop">🌱 ${tw.cropType}</div>
                <div class="tower-location">📍 ${tw.location}</div>
                <div class="tower-metrics">
                    <div class="metric-box">
                        <span class="metric-label">Root Humidity</span>
                        <span class="metric-val ${isHumLow ? 'alert' : ''}">${hum}%</span>
                    </div>
                    <div class="metric-box">
                        <span class="metric-label">Nutrient PPM</span>
                        <span class="metric-val ${isPpmLow ? 'alert' : ''}">${ppm}</span>
                    </div>
                </div>
                <div class="tower-actions">
                    <button class="btn btn-sm btn-primary" onclick="triggerMistForTower(${tw.towerId})">
                        💧 Manual Mist
                    </button>
                    <button class="btn btn-sm btn-secondary" onclick="openSimulateModalForTower(${tw.towerId})">
                        📡 Telemetry
                    </button>
                </div>
            `;
            cardsGrid.appendChild(card);
        });

        // Render Recent Mists
        const mistsTbody = document.querySelector('#table-recent-mists tbody');
        mistsTbody.innerHTML = '';
        mists.slice(0, 6).forEach(m => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><strong>${m.tower ? m.tower.towerCode : 'TWR'}</strong></td>
                <td><span class="badge badge-${m.triggeredBy === 'AUTO' ? 'success' : 'warning'}">${m.triggeredBy}</span></td>
                <td>${m.durationSeconds}s</td>
                <td>${m.nutrientMl} ml</td>
                <td><small>${formatDate(m.triggeredAt)}</small></td>
            `;
            mistsTbody.appendChild(tr);
        });

        // Render Recent Journals
        const jourTbody = document.querySelector('#table-recent-journals tbody');
        jourTbody.innerHTML = '';
        journals.slice(0, 6).forEach(j => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${j.entryDate}</td>
                <td><span class="tag">${j.journalType}</span></td>
                <td><strong>${j.reference}</strong></td>
                <td>${j.description || '--'}</td>
            `;
            jourTbody.appendChild(tr);
        });

    } catch (e) {
        console.error('Error loading dashboard:', e);
    }
}

// 2. Towers Fleet View
async function loadTowers() {
    try {
        const [towersRes, telemetryRes] = await Promise.all([
            fetch(`${API_BASE}/aeroponics/towers`),
            fetch(`${API_BASE}/aeroponics/telemetry/latest`)
        ]);
        const towers = await towersRes.json();
        const telemetryList = await telemetryRes.json();

        const telMap = {};
        telemetryList.forEach(t => {
            if (t.tower) telMap[t.tower.towerId] = t;
        });

        const tbody = document.querySelector('#table-towers tbody');
        tbody.innerHTML = '';

        towers.forEach(t => {
            const tel = telMap[t.towerId] || {};
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><strong class="font-mono text-cyan">${t.towerCode}</strong></td>
                <td>${t.location}</td>
                <td>🌱 ${t.cropType}</td>
                <td><span class="badge badge-${t.status.toLowerCase()}">${t.status}</span></td>
                <td>${tel.rootHumidityPct ? tel.rootHumidityPct + '%' : '--'}</td>
                <td>${tel.nutrientPpm ? tel.nutrientPpm + ' PPM' : '--'}</td>
                <td>
                    <button class="btn btn-sm btn-primary" onclick="triggerMistForTower(${t.towerId})">💧 Mist</button>
                    <button class="btn btn-sm btn-secondary" onclick="openSimulateModalForTower(${t.towerId})">Ingest</button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    } catch (e) {
        console.error('Error loading towers:', e);
    }
}

// 3. Mist Cycles View
async function loadMistCycles() {
    try {
        const res = await fetch(`${API_BASE}/aeroponics/mist-cycles`);
        const cycles = await res.json();
        const tbody = document.querySelector('#table-all-mists tbody');
        tbody.innerHTML = '';

        cycles.forEach(c => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>#${c.cycleId}</td>
                <td><strong>${c.tower ? c.tower.towerCode : '--'}</strong></td>
                <td><span class="badge badge-${c.triggeredBy === 'AUTO' ? 'success' : 'warning'}">${c.triggeredBy}</span></td>
                <td>${c.durationSeconds} sec</td>
                <td>${c.nutrientMl} ml</td>
                <td>${formatDate(c.triggeredAt)}</td>
            `;
            tbody.appendChild(tr);
        });
    } catch (e) {
        console.error('Error loading mist cycles:', e);
    }
}

// 4. Harvests View
async function loadHarvests() {
    try {
        const res = await fetch(`${API_BASE}/aeroponics/harvests`);
        const harvests = await res.json();
        const tbody = document.querySelector('#table-harvests tbody');
        tbody.innerHTML = '';

        harvests.forEach(h => {
            const unitPrice = h.product ? Number(h.product.unitPrice) : 450;
            const estVal = Number(h.quantityKg) * unitPrice;

            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>#${h.yieldId}</td>
                <td><strong>${h.tower ? h.tower.towerCode : '--'}</strong></td>
                <td>${h.product ? h.product.name : '--'}</td>
                <td>${h.harvestDate}</td>
                <td><strong class="text-success">${Number(h.quantityKg).toFixed(2)} kg</strong></td>
                <td>$${estVal.toLocaleString('en-US', { minimumFractionDigits: 2 })}</td>
            `;
            tbody.appendChild(tr);
        });
    } catch (e) {
        console.error('Error loading harvests:', e);
    }
}

// 5. Procurement View (POs & Bills)
async function loadProcurement() {
    try {
        const [poRes, billsRes] = await Promise.all([
            fetch(`${API_BASE}/purchase-orders`),
            fetch(`${API_BASE}/vendor-bills`)
        ]);
        const pos = await poRes.json();
        const bills = await billsRes.json();

        // POs Table
        const poTbody = document.querySelector('#table-pos tbody');
        poTbody.innerHTML = '';
        pos.forEach(po => {
            const tr = document.createElement('tr');
            let actionBtn = '';
            if (po.status === 'CONFIRMED' || po.status === 'DRAFT') {
                actionBtn = `<button class="btn btn-sm btn-primary" onclick="convertPoToBill(${po.poId})">Convert to Bill</button>`;
            } else {
                actionBtn = `<span class="badge badge-success">Billed</span>`;
            }

            tr.innerHTML = `
                <td><strong>${po.poNumber}</strong></td>
                <td>${po.vendor ? po.vendor.name : '--'}</td>
                <td>${po.orderDate}</td>
                <td>$${Number(po.totalAmount).toLocaleString('en-US', { minimumFractionDigits: 2 })}</td>
                <td><span class="badge badge-active">${po.status}</span></td>
                <td>${actionBtn}</td>
            `;
            poTbody.appendChild(tr);
        });

        // Bills Table
        const billTbody = document.querySelector('#table-bills tbody');
        billTbody.innerHTML = '';
        bills.forEach(bill => {
            const tr = document.createElement('tr');
            let actionBtn = '';
            if (bill.status === 'POSTED') {
                actionBtn = `<button class="btn btn-sm btn-secondary" onclick="payVendorBill(${bill.billId})">Pay via Bank</button>`;
            } else if (bill.status === 'PAID') {
                actionBtn = `<span class="badge badge-success">Settled</span>`;
            }

            tr.innerHTML = `
                <td><strong>${bill.billNumber}</strong></td>
                <td>${bill.vendor ? bill.vendor.name : '--'}</td>
                <td>${bill.billDate}</td>
                <td>$${Number(bill.totalAmount).toLocaleString('en-US', { minimumFractionDigits: 2 })}</td>
                <td><span class="badge badge-${bill.status === 'PAID' ? 'success' : 'warning'}">${bill.status}</span></td>
                <td>${actionBtn}</td>
            `;
            billTbody.appendChild(tr);
        });

    } catch (e) {
        console.error('Error loading procurement:', e);
    }
}

// 6. Sales View (SOs & Invoices)
async function loadSales() {
    try {
        const [soRes, invRes] = await Promise.all([
            fetch(`${API_BASE}/sales-orders`),
            fetch(`${API_BASE}/customer-invoices`)
        ]);
        const sos = await soRes.json();
        const invoices = await invRes.json();

        // SOs Table
        const soTbody = document.querySelector('#table-sos tbody');
        soTbody.innerHTML = '';
        sos.forEach(so => {
            const tr = document.createElement('tr');
            let actionBtn = '';
            if (so.status === 'CONFIRMED' || so.status === 'DRAFT') {
                actionBtn = `<button class="btn btn-sm btn-primary" onclick="convertSoToInvoice(${so.soId})">Convert to Invoice</button>`;
            } else {
                actionBtn = `<span class="badge badge-success">Invoiced</span>`;
            }

            tr.innerHTML = `
                <td><strong>${so.soNumber}</strong></td>
                <td>${so.customer ? so.customer.name : '--'}</td>
                <td>${so.orderDate}</td>
                <td>$${Number(so.totalAmount).toLocaleString('en-US', { minimumFractionDigits: 2 })}</td>
                <td><span class="badge badge-active">${so.status}</span></td>
                <td>${actionBtn}</td>
            `;
            soTbody.appendChild(tr);
        });

        // Invoices Table
        const invTbody = document.querySelector('#table-invoices tbody');
        invTbody.innerHTML = '';
        invoices.forEach(inv => {
            const tr = document.createElement('tr');
            let actionBtn = '';
            if (inv.status === 'POSTED') {
                actionBtn = `<button class="btn btn-sm btn-primary" onclick="payCustomerInvoice(${inv.invoiceId})">Receive Payment</button>`;
            } else if (inv.status === 'PAID') {
                actionBtn = `<span class="badge badge-success">Paid via Bank</span>`;
            }

            tr.innerHTML = `
                <td><strong>${inv.invoiceNumber}</strong></td>
                <td>${inv.customer ? inv.customer.name : '--'}</td>
                <td>${inv.invoiceDate}</td>
                <td>$${Number(inv.totalAmount).toLocaleString('en-US', { minimumFractionDigits: 2 })}</td>
                <td><span class="badge badge-${inv.status === 'PAID' ? 'success' : 'warning'}">${inv.status}</span></td>
                <td>${actionBtn}</td>
            `;
            invTbody.appendChild(tr);
        });

    } catch (e) {
        console.error('Error loading sales:', e);
    }
}

// 7. Double-Entry Journals View
async function loadJournals() {
    try {
        const res = await fetch(`${API_BASE}/journal-entries`);
        const entries = await res.json();
        const tbody = document.querySelector('#table-all-journals tbody');
        tbody.innerHTML = '';

        entries.forEach(e => {
            const linesHtml = (e.lines || []).map(l => {
                const accName = l.account ? `${l.account.accountCode} ${l.account.accountName}` : 'Account';
                const isDebit = Number(l.debit) > 0;
                return `
                    <div style="font-family: var(--font-mono); font-size: 11.5px; margin-bottom: 2px;">
                        ${isDebit ? '🔵 Dr: ' : '🟢 Cr: '} 
                        <strong>${accName}</strong> 
                        &nbsp;&nbsp; $${Number(isDebit ? l.debit : l.credit).toLocaleString('en-US', { minimumFractionDigits: 2 })}
                    </div>
                `;
            }).join('');

            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>#${e.entryId}</td>
                <td>${e.entryDate}</td>
                <td><span class="tag">${e.journalType}</span></td>
                <td><strong>${e.reference}</strong></td>
                <td>${e.description}</td>
                <td>${linesHtml || '--'}</td>
            `;
            tbody.appendChild(tr);
        });
    } catch (e) {
        console.error('Error loading journals:', e);
    }
}

// 8. Financial Reports View (P&L and Balance Sheet)
async function loadReports() {
    try {
        const [plRes, bsRes] = await Promise.all([
            fetch(`${API_BASE}/reports/profit-loss`),
            fetch(`${API_BASE}/reports/balance-sheet`)
        ]);
        const pl = await plRes.json();
        const bs = await bsRes.json();

        // Render P&L
        const plContainer = document.getElementById('pl-container');
        let plHtml = `
            <div class="report-section">
                <div class="report-section-title">Income & Farm Revenue</div>
        `;
        for (const [k, v] of Object.entries(pl.incomeAccounts || {})) {
            plHtml += `
                <div class="report-row">
                    <span>${k}</span>
                    <span class="report-val text-success">$${Number(v).toLocaleString('en-US', { minimumFractionDigits: 2 })}</span>
                </div>
            `;
        }
        plHtml += `
            <div class="report-row total">
                <span>Total Income</span>
                <span class="report-val text-success">$${Number(pl.totalIncome).toLocaleString('en-US', { minimumFractionDigits: 2 })}</span>
            </div>
            </div>
            
            <div class="report-section" style="margin-top: 20px;">
                <div class="report-section-title">Operating Costs & Expenses</div>
        `;
        for (const [k, v] of Object.entries(pl.expenseAccounts || {})) {
            plHtml += `
                <div class="report-row">
                    <span>${k}</span>
                    <span class="report-val">$${Number(v).toLocaleString('en-US', { minimumFractionDigits: 2 })}</span>
                </div>
            `;
        }
        plHtml += `
            <div class="report-row total">
                <span>Total Operating Expenses</span>
                <span class="report-val">$${Number(pl.totalExpenses).toLocaleString('en-US', { minimumFractionDigits: 2 })}</span>
            </div>
            </div>

            <div class="report-row total" style="background: rgba(0, 230, 118, 0.08); padding: 12px; border-radius: 6px;">
                <span>NET FARM OPERATING PROFIT</span>
                <span class="report-val text-success" style="font-size: 18px;">$${Number(pl.netProfit).toLocaleString('en-US', { minimumFractionDigits: 2 })}</span>
            </div>
        `;
        plContainer.innerHTML = plHtml;

        // Render Balance Sheet
        const bsContainer = document.getElementById('bs-container');
        let bsHtml = `
            <div class="report-section">
                <div class="report-section-title">Assets (Infrastructure & Cash)</div>
        `;
        for (const [k, v] of Object.entries(bs.assetAccounts || {})) {
            bsHtml += `
                <div class="report-row">
                    <span>${k}</span>
                    <span class="report-val">$${Number(v).toLocaleString('en-US', { minimumFractionDigits: 2 })}</span>
                </div>
            `;
        }
        bsHtml += `
            <div class="report-row total">
                <span>Total Assets</span>
                <span class="report-val text-success">$${Number(bs.totalAssets).toLocaleString('en-US', { minimumFractionDigits: 2 })}</span>
            </div>
            </div>

            <div class="report-section" style="margin-top: 20px;">
                <div class="report-section-title">Liabilities & Creditors</div>
        `;
        for (const [k, v] of Object.entries(bs.liabilityAccounts || {})) {
            bsHtml += `
                <div class="report-row">
                    <span>${k}</span>
                    <span class="report-val">$${Number(v).toLocaleString('en-US', { minimumFractionDigits: 2 })}</span>
                </div>
            `;
        }
        bsHtml += `
            <div class="report-row total">
                <span>Total Liabilities</span>
                <span class="report-val">$${Number(bs.totalLiabilities).toLocaleString('en-US', { minimumFractionDigits: 2 })}</span>
            </div>
            </div>

            <div class="report-section" style="margin-top: 20px;">
                <div class="report-section-title">Equity & Retained Earnings</div>
        `;
        for (const [k, v] of Object.entries(bs.equityAccounts || {})) {
            bsHtml += `
                <div class="report-row">
                    <span>${k}</span>
                    <span class="report-val">$${Number(v).toLocaleString('en-US', { minimumFractionDigits: 2 })}</span>
                </div>
            `;
        }
        bsHtml += `
            <div class="report-row total" style="background: rgba(0, 240, 255, 0.08); padding: 12px; border-radius: 6px;">
                <span>Total Liabilities & Equity</span>
                <span class="report-val" style="font-size: 18px; color: var(--cyan);">$${Number(bs.totalLiabilitiesAndEquity).toLocaleString('en-US', { minimumFractionDigits: 2 })}</span>
            </div>
        `;
        bsContainer.innerHTML = bsHtml;

    } catch (e) {
        console.error('Error loading reports:', e);
    }
}

// 9. Zone Budgets View
async function loadBudgets() {
    try {
        const res = await fetch(`${API_BASE}/reports/budget`);
        const list = await res.json();
        const tbody = document.querySelector('#table-budgets tbody');
        tbody.innerHTML = '';

        list.forEach(b => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><strong class="font-mono text-cyan">${b.zoneCode}</strong></td>
                <td>${b.zoneName}</td>
                <td>${b.assignedTower}</td>
                <td>$${Number(b.budgetedAmount).toLocaleString('en-US', { minimumFractionDigits: 2 })}</td>
                <td>$${Number(b.actualSpent).toLocaleString('en-US', { minimumFractionDigits: 2 })}</td>
                <td>$${Number(b.variance).toLocaleString('en-US', { minimumFractionDigits: 2 })}</td>
                <td><strong>${b.utilizationPct}%</strong></td>
                <td><span class="badge badge-${b.status === 'HEALTHY' ? 'success' : 'warning'}">${b.status}</span></td>
            `;
            tbody.appendChild(tr);
        });
    } catch (e) {
        console.error('Error loading budgets:', e);
    }
}

// 10. Master Data View
async function loadMasterData() {
    try {
        const [cRes, pRes, aRes] = await Promise.all([
            fetch(`${API_BASE}/contacts`),
            fetch(`${API_BASE}/products`),
            fetch(`${API_BASE}/accounts`)
        ]);
        const contacts = await cRes.json();
        const products = await pRes.json();
        const accounts = await aRes.json();

        // Contacts Table
        const cTbody = document.querySelector('#table-contacts tbody');
        cTbody.innerHTML = '';
        contacts.forEach(c => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><strong>${c.name}</strong></td>
                <td><span class="badge badge-${c.type === 'CUSTOMER' ? 'active' : 'idle'}">${c.type}</span></td>
                <td><small>${c.email || '--'}</small></td>
            `;
            cTbody.appendChild(tr);
        });

        // Products Table
        const pTbody = document.querySelector('#table-products tbody');
        pTbody.innerHTML = '';
        products.forEach(p => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${p.name}</td>
                <td><span class="tag">${p.uom}</span></td>
                <td>$${Number(p.unitPrice).toFixed(2)}</td>
            `;
            pTbody.appendChild(tr);
        });

        // Chart of Accounts Table
        const aTbody = document.querySelector('#table-coa tbody');
        aTbody.innerHTML = '';
        accounts.forEach(a => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><strong class="font-mono">${a.accountCode}</strong></td>
                <td>${a.accountName}</td>
                <td><span class="tag">${a.accountType}</span></td>
            `;
            aTbody.appendChild(tr);
        });

    } catch (e) {
        console.error('Error loading master data:', e);
    }
}

// Cache helpers for dropdown selects
async function fetchTowersCache() {
    try {
        const res = await fetch(`${API_BASE}/aeroponics/towers`);
        towersCache = await res.json();
        populateSelect('telemetry-tower-select', towersCache, 'towerId', t => `${t.towerCode} (${t.cropType})`);
        populateSelect('harvest-tower-select', towersCache, 'towerId', t => `${t.towerCode} (${t.cropType})`);
    } catch (e) {}
}

async function fetchProductsCache() {
    try {
        const res = await fetch(`${API_BASE}/products`);
        productsCache = await res.json();
        populateSelect('harvest-product-select', productsCache, 'productId', p => `${p.name} ($${p.unitPrice}/${p.uom})`);
        populateSelect('po-product-select', productsCache, 'productId', p => `${p.name} ($${p.unitPrice}/${p.uom})`);
        populateSelect('so-product-select', productsCache, 'productId', p => `${p.name} ($${p.unitPrice}/${p.uom})`);
    } catch (e) {}
}

async function fetchContactsCache() {
    try {
        const res = await fetch(`${API_BASE}/contacts`);
        contactsCache = await res.json();
        const vendors = contactsCache.filter(c => c.type === 'VENDOR');
        const customers = contactsCache.filter(c => c.type === 'CUSTOMER');
        populateSelect('po-vendor-select', vendors, 'contactId', c => `${c.name}`);
        populateSelect('so-customer-select', customers, 'contactId', c => `${c.name}`);
    } catch (e) {}
}

function populateSelect(elemId, items, valKey, labelFn) {
    const sel = document.getElementById(elemId);
    if (!sel) return;
    sel.innerHTML = '';
    items.forEach(item => {
        const opt = document.createElement('option');
        opt.value = item[valKey];
        opt.innerText = labelFn(item);
        sel.appendChild(opt);
    });
}

// Action Handlers
async function triggerMistForTower(towerId) {
    try {
        const res = await fetch(`${API_BASE}/aeroponics/towers/${towerId}/mist`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ durationSeconds: 30, nutrientMl: 20.0 })
        });
        if (res.ok) {
            const data = await res.json();
            showToast(`💧 Manual mist triggered on Tower #${towerId}! Duration: 30s`);
            refreshCurrentTab();
        }
    } catch (e) {
        showToast('Failed to trigger mist cycle', true);
    }
}

async function convertPoToBill(poId) {
    try {
        const res = await fetch(`${API_BASE}/purchase-orders/${poId}/convert-to-bill`, { method: 'POST' });
        if (res.ok) {
            const bill = await res.json();
            showToast(`📦 Converted PO to Vendor Bill ${bill.billNumber}! Double-entry posted.`);
            loadProcurement();
            loadOverviewKpis();
        }
    } catch (e) {
        showToast('Failed to convert PO to bill', true);
    }
}

async function payVendorBill(billId) {
    try {
        const res = await fetch(`${API_BASE}/vendor-bills/${billId}/pay`, { method: 'POST' });
        if (res.ok) {
            showToast(`✅ Vendor Bill paid via Bank transfer! Ledger updated.`);
            loadProcurement();
            loadOverviewKpis();
        }
    } catch (e) {
        showToast('Failed to process payment', true);
    }
}

async function convertSoToInvoice(soId) {
    try {
        const res = await fetch(`${API_BASE}/sales-orders/${soId}/convert-to-invoice`, { method: 'POST' });
        if (res.ok) {
            const inv = await res.json();
            showToast(`🛒 Converted SO to Customer Invoice ${inv.invoiceNumber}! Double-entry posted.`);
            loadSales();
            loadOverviewKpis();
        }
    } catch (e) {
        showToast('Failed to convert SO to invoice', true);
    }
}

async function payCustomerInvoice(invoiceId) {
    try {
        const res = await fetch(`${API_BASE}/customer-invoices/${invoiceId}/pay`, { method: 'POST' });
        if (res.ok) {
            showToast(`💰 Payment received from grocery chain via Bank transfer!`);
            loadSales();
            loadOverviewKpis();
        }
    } catch (e) {
        showToast('Failed to record receipt', true);
    }
}

// Modal Form Submissions
async function submitTelemetry(e) {
    e.preventDefault();
    const towerId = document.getElementById('telemetry-tower-select').value;
    const humidity = parseFloat(document.getElementById('telemetry-humidity').value);
    const ppm = parseFloat(document.getElementById('telemetry-ppm').value);

    try {
        const res = await fetch(`${API_BASE}/aeroponics/telemetry`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ towerId: parseInt(towerId), rootHumidityPct: humidity, nutrientPpm: ppm })
        });
        const data = await res.json();
        closeModal('modal-telemetry');
        
        if (data.mistCycleTriggered) {
            showToast(`📡 Ingested! 💧 AUTO-MIST ACTIVATED: ${data.triggerReason}`);
        } else {
            showToast(`📡 Ingested reading: ${humidity}% humidity, ${ppm} PPM. Normal.`);
        }
        refreshCurrentTab();
    } catch (err) {
        showToast('Failed to transmit telemetry', true);
    }
}

async function submitNewTower(e) {
    e.preventDefault();
    const code = document.getElementById('tower-code').value;
    const location = document.getElementById('tower-location').value;
    const crop = document.getElementById('tower-crop').value;
    const status = document.getElementById('tower-status').value;

    try {
        const res = await fetch(`${API_BASE}/aeroponics/towers`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ towerCode: code, location: location, cropType: crop, status: status })
        });
        if (res.ok) {
            showToast(`🌿 Aeroponic Tower ${code} added successfully!`);
            closeModal('modal-tower');
            fetchTowersCache();
            loadTowers();
        }
    } catch (err) {
        showToast('Failed to add tower', true);
    }
}

async function submitHarvest(e) {
    e.preventDefault();
    const towerId = document.getElementById('harvest-tower-select').value;
    const productId = document.getElementById('harvest-product-select').value;
    const qty = parseFloat(document.getElementById('harvest-qty').value);

    try {
        const res = await fetch(`${API_BASE}/aeroponics/harvests`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ towerId: parseInt(towerId), productId: parseInt(productId), quantityKg: qty })
        });
        if (res.ok) {
            showToast(`🌾 Harvest yield logged: ${qty} kg!`);
            closeModal('modal-harvest');
            loadHarvests();
            loadOverviewKpis();
        }
    } catch (err) {
        showToast('Failed to record harvest', true);
    }
}

async function submitPo(e) {
    e.preventDefault();
    const vendorId = parseInt(document.getElementById('po-vendor-select').value);
    const productId = parseInt(document.getElementById('po-product-select').value);
    const qty = parseFloat(document.getElementById('po-qty').value);
    const notes = document.getElementById('po-notes').value;

    try {
        const res = await fetch(`${API_BASE}/purchase-orders`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                vendorId: vendorId,
                notes: notes,
                lines: [{ productId: productId, quantity: qty }]
            })
        });
        if (res.ok) {
            showToast(`📦 Purchase Order created successfully!`);
            closeModal('modal-po');
            loadProcurement();
        }
    } catch (err) {
        showToast('Failed to create PO', true);
    }
}

async function submitSo(e) {
    e.preventDefault();
    const customerId = parseInt(document.getElementById('so-customer-select').value);
    const productId = parseInt(document.getElementById('so-product-select').value);
    const qty = parseFloat(document.getElementById('so-qty').value);

    try {
        const res = await fetch(`${API_BASE}/sales-orders`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                customerId: customerId,
                lines: [{ productId: productId, quantityKg: qty }]
            })
        });
        if (res.ok) {
            showToast(`🛒 Sales Order for Supermarket created!`);
            closeModal('modal-so');
            loadSales();
        }
    } catch (err) {
        showToast('Failed to create SO', true);
    }
}

// REST API Tester Helpers
async function testCreateTowerApi() {
    const payload = {
        towerCode: `TWR-EXP-${Math.floor(Math.random() * 900 + 100)}`,
        location: "Experimental Bay - Alpha Wing",
        cropType: "Organic Sweet Basil",
        status: "ACTIVE"
    };
    executeApiCall('POST', '/api/aeroponics/towers', payload);
}

async function testTelemetryApi() {
    const payload = {
        towerId: towersCache.length > 0 ? towersCache[0].towerId : 1,
        rootHumidityPct: 61.4,
        nutrientPpm: 750.0
    };
    executeApiCall('POST', '/api/aeroponics/telemetry', payload);
}

async function testPlReportApi() {
    executeApiCall('GET', '/api/reports/profit-loss', null);
}

async function testBalanceSheetApi() {
    executeApiCall('GET', '/api/reports/balance-sheet', null);
}

async function testBudgetReportApi() {
    executeApiCall('GET', '/api/reports/budget', null);
}

async function executeApiCall(method, endpoint, body) {
    const outputEl = document.getElementById('api-output');
    outputEl.innerText = `Executing ${method} ${endpoint}...\n`;

    try {
        const options = {
            method: method,
            headers: { 'Content-Type': 'application/json' }
        };
        if (body) options.body = JSON.stringify(body);

        const startTime = performance.now();
        const res = await fetch(endpoint, options);
        const data = await res.json();
        const duration = Math.round(performance.now() - startTime);

        outputEl.innerText = `// HTTP Status: ${res.status} OK (${duration}ms)\n// Endpoint: ${method} ${endpoint}\n\n` + JSON.stringify(data, null, 2);
    } catch (err) {
        outputEl.innerText = `// Request Failed: ${err.message}`;
    }
}

function clearApiOutput() {
    document.getElementById('api-output').innerText = '// Console cleared.';
}

// Modal Helpers
function openSimulateModal() {
    document.getElementById('modal-telemetry').classList.add('open');
}

function openSimulateModalForTower(towerId) {
    document.getElementById('telemetry-tower-select').value = towerId;
    document.getElementById('modal-telemetry').classList.add('open');
}

function openAddTowerModal() {
    document.getElementById('modal-tower').classList.add('open');
}

function openHarvestModal() {
    document.getElementById('modal-harvest').classList.add('open');
}

function openCreatePoModal() {
    document.getElementById('modal-po').classList.add('open');
}

function openCreateSoModal() {
    document.getElementById('modal-so').classList.add('open');
}

function closeModal(id) {
    document.getElementById(id).classList.remove('open');
}

function showToast(msg, isError = false) {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${isError ? 'error' : ''}`;
    toast.innerText = msg;
    container.appendChild(toast);
    setTimeout(() => {
        toast.remove();
    }, 4500);
}

function formatDate(isoStr) {
    if (!isoStr) return '--';
    const d = new Date(isoStr);
    return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' }) + ' ' + d.toLocaleDateString();
}
