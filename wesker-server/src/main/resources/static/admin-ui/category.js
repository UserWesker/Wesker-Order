const categoryView = document.getElementById("category-view");
const categorySearch = document.getElementById("category-search-form");
const categoryTable = document.getElementById("category-table");
const categoryRecords = document.getElementById("category-records");
const categoryListMessage = document.getElementById("category-list-message");
const categoryPagination = document.getElementById("category-pagination");
const categoryPrevious = document.getElementById("category-prev");
const categoryNext = document.getElementById("category-next");
const categoryDialog = document.getElementById("category-dialog");
const categoryForm = document.getElementById("category-form");
const categoryFormError = document.getElementById("category-form-error");
const categorySave = document.getElementById("category-save");
const categoryCancel = document.getElementById("category-cancel");

const categoryTypes = { 1: "菜品分类", 2: "套餐分类" };
let categoryPage = 1;
let categoryFilters = {};
let categoryRequest;
let categoryDraft;

async function loadCategories(targetPage) {
    categoryRequest?.abort();
    const controller = new AbortController();
    categoryRequest = controller;
    categoryTable.hidden = true;
    categoryPagination.hidden = true;
    showMessage(categoryListMessage, "正在查询分类…");
    const params = new URLSearchParams({ page: targetPage, pageSize, ...categoryFilters });

    try {
        const { total, records } = await request(`/admin/category/page?${params}`, {
            token: session.token,
            signal: controller.signal,
        });
        if (controller.signal.aborted) return;
        const pages = Math.max(1, Math.ceil(total / pageSize));
        if (targetPage > pages) return loadCategories(pages);
        categoryPage = targetPage;
        categoryRecords.replaceChildren(...records.map(categoryRow));
        categoryTable.hidden = records.length === 0;
        showMessage(categoryListMessage, records.length ? "" : "没有符合条件的分类。");
        document.getElementById("category-page-info").textContent = `共 ${total} 条 · 第 ${categoryPage} / ${pages} 页`;
        categoryPrevious.disabled = categoryPage <= 1;
        categoryNext.disabled = categoryPage >= pages;
        categoryPagination.hidden = false;
    } catch (error) {
        if (controller.signal.aborted) return;
        if (error.status === 401) clearSession("登录已失效，请重新登录。");
        else showMessage(categoryListMessage, error.message);
    }
}

function categoryRow(category) {
    const row = document.createElement("tr");
    for (const value of [category.name, categoryTypes[category.type]]) {
        row.insertCell().textContent = value;
    }
    const enabled = category.status === 1;
    row.insertCell().append(renderRecordStatus(enabled));
    const lastOperation = category.updateTime || category.createTime;
    const timeCell = row.insertCell();
    timeCell.className = "employee-time";
    timeCell.textContent = lastOperation?.replace("T", " ").slice(0, 16) || "—";

    row.insertCell().append(
        rowAction("修改", () => openCategoryDialog(category)),
        rowAction("删除", event => deleteCategory(event.currentTarget, category)),
        rowAction(enabled ? "禁用" : "启用", event => changeCategoryStatus(event.currentTarget, category)),
    );
    return row;
}

async function deleteCategory(button, category) {
    if (!window.confirm(`确定删除「${category.name}」吗？删除后无法恢复。`)) return;

    const activeSession = session;
    const isCurrentPage = () => session === activeSession && location.hash === "#categories";
    button.disabled = true;
    showMessage(notice);
    try {
        await request(`/admin/category/${category.id}`, { method: "DELETE", token: activeSession.token });
        if (isCurrentPage()) {
            showMessage(notice, "分类已删除。");
            await loadCategories(categoryPage);
        }
    } catch (error) {
        if (session !== activeSession) return;
        if (error.status === 401) clearSession("登录已失效，请重新登录。");
        else if (isCurrentPage()) showMessage(notice, error.message);
    } finally {
        button.disabled = false;
    }
}

async function changeCategoryStatus(button, category) {
    const nextStatus = category.status === 1 ? 0 : 1;
    if (nextStatus === 0 && !window.confirm(`确定禁用「${category.name}」吗？`)) return;

    const activeSession = session;
    const isCurrentPage = () => session === activeSession && location.hash === "#categories";
    const params = new URLSearchParams({ id: category.id });
    button.disabled = true;
    showMessage(notice);
    try {
        await request(`/admin/category/status/${nextStatus}?${params}`, { method: "PUT", token: activeSession.token });
        if (isCurrentPage()) await loadCategories(categoryPage);
    } catch (error) {
        if (!isCurrentPage()) return;
        if (error.status === 401) clearSession("登录已失效，请重新登录。");
        else showMessage(notice, error.message);
    } finally {
        button.disabled = false;
    }
}

function openCategoryDialog(category) {
    categoryDraft = category;
    categoryForm.reset();
    categoryForm.elements.name.value = category.name ?? "";
    categorySave.disabled = false;
    categoryCancel.disabled = false;
    showMessage(categoryFormError);
    document.getElementById("category-dialog-title").textContent = category.id
        ? "修改分类" : `新增${categoryTypes[category.type]}`;
    categoryDialog.showModal();
}

function hideCategories() {
    categoryRequest?.abort();
    categoryDraft = undefined;
    if (categoryDialog.open) categoryDialog.close();
}

function resetCategories() {
    hideCategories();
    categorySearch.reset();
    categoryRecords.replaceChildren();
    categoryPage = 1;
    categoryFilters = {};
}

categorySearch.addEventListener("submit", event => {
    event.preventDefault();
    const fields = new FormData(categorySearch);
    categoryFilters = { name: fields.get("name").trim() };
    if (fields.get("type")) categoryFilters.type = fields.get("type");
    showMessage(notice);
    loadCategories(1);
});

categoryPrevious.addEventListener("click", () => loadCategories(categoryPage - 1));
categoryNext.addEventListener("click", () => loadCategories(categoryPage + 1));
categoryView.querySelectorAll("[data-category-type]").forEach(button => {
    button.addEventListener("click", () => openCategoryDialog({ type: Number(button.dataset.categoryType) }));
});

categoryCancel.addEventListener("click", () => categoryDialog.close());
categoryDialog.addEventListener("cancel", event => {
    if (categorySave.disabled) event.preventDefault();
});

categoryForm.addEventListener("submit", async event => {
    event.preventDefault();
    const draft = categoryDraft;
    if (!categoryDialog.open || !draft || categorySave.disabled) return;
    const fields = new FormData(categoryForm);
    const name = fields.get("name").trim();
    if (!name) {
        showMessage(categoryFormError, "请填写分类名称。");
        return;
    }

    const editing = draft.id !== undefined;
    if (editing && name === draft.name) {
        categoryDialog.close();
        return;
    }

    const activeSession = session;
    const isCurrentDialog = () => session === activeSession && location.hash === "#categories"
        && categoryDialog.open && categoryDraft === draft;
    const path = editing ? `/admin/category/${draft.id}` : "/admin/category";
    const body = editing ? { name } : { name, type: draft.type };
    categorySave.disabled = true;
    categoryCancel.disabled = true;
    showMessage(categoryFormError);
    showMessage(notice);
    try {
        await request(path, { method: editing ? "PUT" : "POST", token: activeSession.token, body });
        if (!isCurrentDialog()) return;
        categoryDialog.close();
        showMessage(notice, editing ? "分类已修改。" : "分类已新增。");
        await loadCategories(categoryPage);
    } catch (error) {
        if (!isCurrentDialog()) return;
        if (error.status === 401) clearSession("登录已失效，请重新登录。");
        else showMessage(categoryFormError, error.message);
    } finally {
        if (categoryDraft === draft) {
            categorySave.disabled = false;
            categoryCancel.disabled = false;
        }
    }
});
