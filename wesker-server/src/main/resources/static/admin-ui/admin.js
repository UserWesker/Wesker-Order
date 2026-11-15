const loginView = document.getElementById("login-view");
const adminView = document.getElementById("admin-view");
const loginForm = document.getElementById("login-form");
const employeeForm = document.getElementById("employee-form");
const loginError = document.getElementById("login-error");
const employeeError = document.getElementById("employee-error");
const notice = document.getElementById("notice");
const logout = document.getElementById("logout");
const navigation = document.getElementById("navigation");
const toggleNavigation = document.getElementById("toggle-navigation");
const links = [...navigation.querySelectorAll("a")];
const searchForm = document.getElementById("employee-search-form");
const searchInput = document.getElementById("employee-search");
const employeeView = document.getElementById("employee-view");
const employeeTable = document.getElementById("employee-table");
const employeeRecords = document.getElementById("employee-records");
const listMessage = document.getElementById("employee-list-message");
const pagination = document.getElementById("employee-pagination");
const previous = document.getElementById("employee-prev");
const next = document.getElementById("employee-next");
const pageSize = 10;
let page = 1;
let employeeName = "";
let listRequest;
const sessionKey = "wesker-admin-session-v2";
sessionStorage.removeItem("wesker-admin-session");
let session;

try {
    session = JSON.parse(sessionStorage.getItem(sessionKey));
} catch {
    sessionStorage.removeItem(sessionKey);
}

function showMessage(element, message = "") {
    element.textContent = message;
    element.hidden = !message;
}

function renderPage() {
    const signedIn = Boolean(session?.token);
    loginView.hidden = signedIn;
    adminView.hidden = !signedIn;
    if (!signedIn) return;

    const hash = location.hash;
    const activeLink = links.find(link => link.hash === hash) ?? links.at(-1);
    const adding = hash === "#employee/add";
    const employees = activeLink.hash === "#employee";
    const roleLabel = session.role === "ADMIN" ? "管理员" : "员工";
    document.getElementById("account-name").textContent = `${session.name || session.userName} · ${roleLabel}`;
    document.getElementById("page-title").textContent = adding ? "添加员工" : activeLink.textContent.slice(1).trim();
    document.getElementById("back").hidden = !adding;
    employeeView.hidden = !employees || adding;
    document.getElementById("add-view").hidden = !adding;
    document.getElementById("placeholder-view").hidden = employees;
    links.forEach(link => {
        if (link === activeLink) link.setAttribute("aria-current", "page");
        else link.removeAttribute("aria-current");
    });
    if (employees && !adding) loadEmployees(page);
    else listRequest?.abort();
}

function clearSession(message = "") {
    listRequest?.abort();
    page = 1;
    employeeName = "";
    searchForm.reset();
    employeeRecords.replaceChildren();
    session = null;
    sessionStorage.removeItem(sessionKey);
    employeeForm.reset();
    loginForm.reset();
    showMessage(notice);
    showMessage(employeeError);
    showMessage(loginError, message);
    location.hash = "login";
    renderPage();
}

async function request(path, { method = "GET", body, token, signal } = {}) {
    const headers = { Accept: "application/json" };
    if (body !== undefined) headers["Content-Type"] = "application/json";
    if (token) headers.Authorization = `Bearer ${token}`;

    let response;
    try {
        response = await fetch(path, {
            method,
            headers,
            signal,
            body: body === undefined ? undefined : JSON.stringify(body),
        });
    } catch (error) {
        if (signal?.aborted) throw error;
        throw new Error("无法连接服务，请稍后重试。");
    }
    const result = await response.json().catch(() => null);
    if (!response.ok || result?.code !== 1) {
        const message = result?.msg || (response.status === 400
            ? "请检查输入的字段和长度。"
            : `请求失败（HTTP ${response.status}）。`);
        throw Object.assign(new Error(message), { status: response.status });
    }
    return result.data;
}

function post(path, body, token) {
    return request(path, { method: "POST", body, token });
}

function employeeAction(label, onClick) {
    const button = document.createElement("button");
    button.type = "button";
    button.className = "row-action";
    button.textContent = label;
    button.disabled = !onClick;
    if (onClick) button.addEventListener("click", onClick);
    else button.title = "待开发";
    return button;
}

async function changeEmployeeStatus(button, employeeId, nextStatus) {
    const activeSession = session;
    const params = new URLSearchParams({ id: employeeId });
    button.disabled = true;
    showMessage(notice);
    try {
        await request(`/admin/employee/status/${nextStatus}?${params}`, {
            method: "PUT", token: activeSession.token,
        });
        if (session !== activeSession || employeeView.hidden) return;
        showMessage(notice, nextStatus === 1 ? "员工已启用。" : "员工已禁用。");
        await loadEmployees(page);
    } catch (error) {
        if (session !== activeSession) return;
        if (error.status === 401) clearSession("登录已失效，请重新登录。");
        else if (!employeeView.hidden) showMessage(notice, error.message);
    } finally {
        button.disabled = false;
    }
}

function employeeRow(employee) {
    const row = document.createElement("tr");
    for (const value of [employee.name, employee.username, employee.phone]) {
        row.insertCell().textContent = value || "—";
    }
    const enabled = employee.status === 1;
    const status = document.createElement("span");
    status.className = "employee-status";
    status.dataset.enabled = String(enabled);
    const dot = document.createElement("span");
    dot.className = "employee-status-dot";
    dot.setAttribute("aria-hidden", "true");
    status.append(dot, enabled ? "启用" : "已禁用");
    row.insertCell().append(status);
    for (const value of [employee.createTime, employee.updateTime]) {
        const cell = row.insertCell();
        cell.className = "employee-time";
        cell.textContent = value?.replace("T", " ").slice(0, 19) || "—";
    }
    const actions = row.insertCell();
    actions.append(
        employeeAction("修改"),
        employeeAction(enabled ? "禁用" : "启用", event =>
            changeEmployeeStatus(event.currentTarget, employee.id, enabled ? 0 : 1)),
        employeeAction("删除"),
    );
    return row;
}

async function loadEmployees(targetPage) {
    listRequest?.abort();
    employeeTable.hidden = true;
    pagination.hidden = true;

    const isAdmin = session?.role === "ADMIN";
    document.getElementById("employee-add-link").hidden = !isAdmin;
    searchInput.disabled = !isAdmin;
    searchForm.querySelector("button").disabled = !isAdmin;
    if (!isAdmin) {
        showMessage(listMessage, "没有管理员权限");
        return;
    }

    const controller = new AbortController();
    listRequest = controller;
    showMessage(listMessage, "正在查询员工…");
    const params = new URLSearchParams({ page: targetPage, pageSize });
    if (employeeName) params.set("name", employeeName);
    try {
        const { total, records } = await request(`/admin/employee/page?${params}`, {
            token: session.token, signal: controller.signal,
        });
        if (controller.signal.aborted) return;
        const pages = Math.max(1, Math.ceil(total / pageSize));
        if (targetPage > pages) {
            loadEmployees(pages);
            return;
        }
        page = targetPage;
        employeeRecords.replaceChildren(...records.map(employeeRow));
        employeeTable.hidden = records.length === 0;
        showMessage(listMessage, records.length ? "" : "没有符合条件的员工。");
        document.getElementById("employee-page-info").textContent = `共 ${total} 条 · 第 ${page} / ${pages} 页`;
        previous.disabled = page <= 1;
        next.disabled = page >= pages;
        pagination.hidden = false;
    } catch (error) {
        if (controller.signal.aborted) return;
        if (error.status === 401) clearSession("登录已失效，请重新登录。");
        else showMessage(listMessage, error.message);
    }
}

searchForm.addEventListener("submit", event => {
    event.preventDefault();
    employeeName = searchInput.value.trim();
    loadEmployees(1);
});
previous.addEventListener("click", () => loadEmployees(page - 1));
next.addEventListener("click", () => loadEmployees(page + 1));

loginForm.addEventListener("submit", async event => {
    event.preventDefault();
    const button = event.submitter;
    const fields = new FormData(loginForm);
    button.disabled = true;
    showMessage(loginError);
    try {
        session = await post("/admin/employee/login", {
            username: fields.get("username").trim(),
            password: fields.get("password"),
        });
        sessionStorage.setItem(sessionKey, JSON.stringify(session));
        loginForm.reset();
        location.hash = "employee";
        renderPage();
    } catch (error) {
        showMessage(loginError, error.message);
    } finally {
        button.disabled = false;
    }
});

employeeForm.addEventListener("submit", async event => {
    event.preventDefault();
    const buttons = [...employeeForm.querySelectorAll('button[type="submit"]')];
    if (buttons.some(button => button.disabled)) return;
    showMessage(employeeError);
    showMessage(notice);
    const fields = new FormData(employeeForm);
    const body = {
        username: fields.get("username").trim(),
        name: fields.get("name").trim(),
        phone: fields.get("phone").trim(),
        sex: fields.get("sex") || "",
        idNumber: fields.get("idNumber").trim().toUpperCase(),
        password: fields.get("password"),
    };
    if (Object.values(body).some(value => !value.trim())) {
        showMessage(employeeError, "请完整填写所有员工资料，并选择性别；内容不能只包含空格。");
        return;
    }
    const password = body.password;
    if (new TextEncoder().encode(password).length > 72) {
        showMessage(employeeError, "初始密码不能超过 72 个 UTF-8 字节。");
        return;
    }

    buttons.forEach(button => { button.disabled = true; });
    try {
        await post("/admin/employee", body, session?.token);
        employeeForm.reset();
        showMessage(notice, "员工添加成功。");
        if (event.submitter?.value === "continue") {
            document.getElementById("employee-username").focus();
        } else {
            location.hash = "employee";
        }
    } catch (error) {
        if (error.status === 401) clearSession("登录已失效，请重新登录。");
        else showMessage(employeeError, error.message);
    } finally {
        buttons.forEach(button => { button.disabled = false; });
    }
});

logout.addEventListener("click", async () => {
    logout.disabled = true;
    try {
        await post("/admin/employee/logout", undefined, session.token);
        clearSession();
    } catch (error) {
        if (error.status === 401) clearSession();
        else showMessage(notice, error.message);
    } finally {
        logout.disabled = false;
    }
});

toggleNavigation.addEventListener("click", () => {
    navigation.hidden = !navigation.hidden;
    toggleNavigation.setAttribute("aria-expanded", String(!navigation.hidden));
    document.getElementById("admin-layout").classList.toggle("menu-collapsed", navigation.hidden);
});

window.addEventListener("hashchange", renderPage);
renderPage();
