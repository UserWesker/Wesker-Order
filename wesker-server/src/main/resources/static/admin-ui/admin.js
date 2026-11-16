const loginView = document.getElementById("login-view");
const adminView = document.getElementById("admin-view");
const loginForm = document.getElementById("login-form");
const loginPassword = document.getElementById("login-password");
const passwordToggle = document.getElementById("toggle-login-password");
const employeeForm = document.getElementById("employee-form");
const employeeFields = employeeForm.querySelector(".employee-fields");
const addButtons = employeeForm.querySelectorAll('button[type="submit"][value]');
const editSave = document.getElementById("employee-edit-save");
const editMessage = document.getElementById("employee-edit-message");
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
let detailRequest;
let editingEmployeeId;
let originalEmployeeForm;
const sessionKey = "wesker-admin-session-v3";
sessionStorage.removeItem("wesker-admin-session");
sessionStorage.removeItem("wesker-admin-session-v2");
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
    const editId = /^#employee\/edit\/([1-9]\d*)$/.exec(hash)?.[1];
    const showingForm = adding || Boolean(editId);
    const employees = activeLink.hash === "#employee";
    document.getElementById("account-name").textContent = `${session.name || session.userName} · 管理员`;
    const pageTitle = editId ? "编辑员工" : adding ? "添加员工" : activeLink.textContent.slice(1).trim();
    document.getElementById("page-title").textContent = pageTitle;
    document.getElementById("back").hidden = !showingForm;
    employeeView.hidden = !employees || showingForm;
    document.getElementById("add-view").hidden = !showingForm;
    document.getElementById("placeholder-view").hidden = employees;
    links.forEach(link => {
        if (link === activeLink) link.setAttribute("aria-current", "page");
        else link.removeAttribute("aria-current");
    });
    if (editId !== editingEmployeeId) {
        detailRequest?.abort();
        editingEmployeeId = editId;
        originalEmployeeForm = undefined;
        employeeForm.noValidate = Boolean(editId);
        employeeForm.reset();
        showMessage(employeeError);
        showMessage(editMessage);
        employeeFields.hidden = Boolean(editId);
        addButtons.forEach(button => { button.hidden = Boolean(editId); });
        editSave.hidden = !editId;
        editSave.disabled = true;
        if (editId) {
            showMessage(notice);
            loadEmployeeDetail(editId);
        }
    }
    if (employees && !showingForm) loadEmployees(page);
    else listRequest?.abort();
}

function clearSession(message = "") {
    listRequest?.abort();
    detailRequest?.abort();
    detailRequest = undefined;
    editingEmployeeId = undefined;
    originalEmployeeForm = undefined;
    employeeForm.noValidate = false;
    employeeFields.hidden = false;
    addButtons.forEach(button => { button.hidden = false; });
    editSave.hidden = true;
    editSave.disabled = true;
    showMessage(editMessage);
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

function readEmployeeForm() {
    const fields = new FormData(employeeForm);
    return {
        username: fields.get("username").trim(),
        name: fields.get("name").trim(),
        phone: fields.get("phone").trim(),
        sex: fields.get("sex") || "",
        idNumber: fields.get("idNumber").trim().toUpperCase(),
    };
}

async function loadEmployeeDetail(employeeId) {
    const controller = new AbortController();
    detailRequest = controller;
    showMessage(editMessage, "正在查询员工资料…");
    try {
        const employee = await request(`/admin/employee/${employeeId}`, {
            token: session.token,
            signal: controller.signal,
        });
        if (controller.signal.aborted) return;
        if (!employee) throw new Error("员工不存在");
        for (const field of ["username", "name", "phone", "idNumber", "sex"]) {
            employeeForm.elements.namedItem(field).value = employee[field] ?? "";
        }
        originalEmployeeForm = JSON.stringify(readEmployeeForm());
        employeeFields.hidden = false;
        editSave.disabled = false;
        showMessage(editMessage);
    } catch (error) {
        if (controller.signal.aborted) return;
        if (error.status === 401) clearSession("登录已失效，请重新登录。");
        else showMessage(editMessage, error.message);
    }
}

async function changeEmployeeStatus(button, employeeId, nextStatus) {
    if (nextStatus === 0 && !window.confirm("确定禁用这名员工吗？")) return;
    const activeSession = session;
    const params = new URLSearchParams({ id: employeeId });
    button.disabled = true;
    showMessage(notice);
    try {
        await request(`/admin/employee/status/${nextStatus}?${params}`, {
            method: "PUT", token: activeSession.token,
        });
        if (session !== activeSession || employeeView.hidden) return;
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
    const lastOperation = employee.updateTime || employee.createTime;
    const timeCell = row.insertCell();
    timeCell.className = "employee-time";
    timeCell.textContent = lastOperation?.replace("T", " ").slice(0, 16) || "—";
    const actions = row.insertCell();
    actions.append(
        employeeAction("编辑", () => { location.hash = `employee/edit/${employee.id}`; }),
        employeeAction(enabled ? "禁用" : "启用", event =>
            changeEmployeeStatus(event.currentTarget, employee.id, enabled ? 0 : 1)),
        employeeAction("删除", event => deleteEmployee(event.currentTarget, employee.id)),
    );
    return row;
}

async function deleteEmployee(button, employeeId) {
    if (!window.confirm("确定删除这名员工吗？删除后无法恢复。")) return;
    const activeSession = session;
    button.disabled = true;
    showMessage(notice);
    try {
        await request(`/admin/employee/${employeeId}`, {
            method: "DELETE",
            token: activeSession.token,
        });
        if (session === activeSession && !employeeView.hidden) await loadEmployees(page);
    } catch (error) {
        if (session !== activeSession) return;
        if (error.status === 401) clearSession("登录已失效，请重新登录。");
        else if (!employeeView.hidden) showMessage(notice, error.message);
    } finally {
        button.disabled = false;
    }
}

async function loadEmployees(targetPage) {
    listRequest?.abort();
    employeeTable.hidden = true;
    pagination.hidden = true;

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

function setPasswordVisible(visible) {
    loginPassword.type = visible ? "text" : "password";
    passwordToggle.setAttribute("aria-pressed", String(visible));
    passwordToggle.title = visible ? "隐藏密码" : "显示密码";
}

passwordToggle.addEventListener("click", () => setPasswordVisible(loginPassword.type === "password"));
loginForm.addEventListener("reset", () => setPasswordVisible(false));

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
        showMessage(loginError, error.status === 401 ? "用户名或密码错误" : error.message);
    } finally {
        button.disabled = false;
    }
});

employeeForm.addEventListener("submit", async event => {
    event.preventDefault();
    const editId = editingEmployeeId;
    const formHash = location.hash;
    if (editId ? formHash !== `#employee/edit/${editId}` : formHash !== "#employee/add") return;
    const activeSession = session;
    if (!activeSession) return;
    const activeDetail = detailRequest;
    const isCurrentForm = () => (
        session === activeSession && location.hash === formHash && (!editId || detailRequest === activeDetail)
    );
    const buttons = editId ? [editSave] : [...addButtons];
    if (buttons.some(button => button.disabled)) return;
    const body = readEmployeeForm();
    if (editId && JSON.stringify(body) === originalEmployeeForm) {
        location.hash = "employee";
        return;
    }
    if (editId && !employeeForm.reportValidity()) return;
    showMessage(employeeError);
    if (Object.values(body).some(value => !value.trim())) {
        showMessage(employeeError, "请完整填写所有员工资料，并选择性别；内容不能只包含空格。");
        return;
    }
    if (editId && !window.confirm("确定保存对这名员工的修改吗？")) return;
    showMessage(notice);
    buttons.forEach(button => { button.disabled = true; });
    try {
        if (editId) {
            await request(`/admin/employee/${editId}`, {
                method: "PUT",
                body,
                token: activeSession.token,
            });
        } else {
            await post("/admin/employee", body, activeSession.token);
        }
        if (!isCurrentForm()) return;
        employeeForm.reset();
        showMessage(notice, editId ? "员工信息已保存。" : "员工添加成功。");
        if (!editId && event.submitter?.value === "continue") {
            document.getElementById("employee-username").focus();
        } else {
            location.hash = "employee";
        }
    } catch (error) {
        if (!isCurrentForm()) return;
        if (error.status === 401) clearSession("登录已失效，请重新登录。");
        else showMessage(employeeError, error.message);
    } finally {
        if (!editId || isCurrentForm()) buttons.forEach(button => { button.disabled = false; });
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
