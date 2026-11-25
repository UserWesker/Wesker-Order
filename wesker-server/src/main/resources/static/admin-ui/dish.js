const dishView = document.getElementById("dish-view");
const dishSearchForm = document.getElementById("dish-search-form");
const dishSearchInput = document.getElementById("dish-search");
const dishAddView = document.getElementById("dish-add-view");
const dishTable = document.getElementById("dish-table");
const dishRecords = document.getElementById("dish-records");
const dishListMessage = document.getElementById("dish-list-message");
const dishPagination = document.getElementById("dish-pagination");
const dishPrevious = document.getElementById("dish-prev");
const dishNext = document.getElementById("dish-next");
const dishForm = document.getElementById("dish-form");
const dishFields = document.getElementById("dish-fields");
const dishCategory = document.getElementById("dish-category");
const dishCategoryMessage = document.getElementById("dish-category-message");
const dishCategoryRetry = document.getElementById("dish-category-retry");
const dishAddFlavor = document.getElementById("dish-add-flavor");
const dishFlavorKind = document.getElementById("dish-flavor-kind");
const dishFlavorGroups = document.getElementById("dish-flavor-groups");
const dishImage = document.getElementById("dish-image");
const dishPreview = document.getElementById("dish-image-preview");
const dishFormError = document.getElementById("dish-form-error");
const dishFormStatus = document.getElementById("dish-form-status");
const dishSubmitButtons = dishForm.querySelectorAll('button[type="submit"]');

const dishFlavorOptions = {
    "温度": ["多冰", "正常冰", "少冰", "去冰", "热"],
    "糖度": ["全糖", "七分糖", "半糖", "少糖", "无糖"],
};
const maxDishImageBytes = 5 * 1024 * 1024;
let dishPage = 1;
let dishSearchKeyword = "";
let dishListRequest;
let dishCategoryRequest;
let dishSaveRequest;
let dishPreviewUrl;
let uploadedDishImageKey;

function dishRow(dish) {
    const row = document.createElement("tr");
    const imageCell = row.insertCell();
    imageCell.className = "dish-image-cell";
    const imageSource = dish.thumbnailUrl || dish.imageUrl;
    if (imageSource) {
        const thumbnail = document.createElement("img");
        thumbnail.className = "dish-thumbnail";
        thumbnail.alt = dish.name + "的图片";
        thumbnail.loading = "lazy";
        thumbnail.decoding = "async";
        thumbnail.referrerPolicy = "no-referrer";
        thumbnail.addEventListener("error", () => {
            if (dish.imageUrl && thumbnail.getAttribute("src") !== dish.imageUrl) {
                thumbnail.src = dish.imageUrl;
            } else {
                imageCell.textContent = "图片不可用";
            }
        });
        thumbnail.src = imageSource;
        imageCell.append(thumbnail);
    } else {
        imageCell.textContent = "无图片";
    }
    for (const value of [dish.name, dish.categoryName, "¥" + Number(dish.price).toFixed(2)]) {
        row.insertCell().textContent = value;
    }
    const enabled = dish.status === 1;
    row.insertCell().append(renderRecordStatus(enabled, enabled ? "起售" : "停售"));
    const timeCell = row.insertCell();
    timeCell.className = "employee-time";
    timeCell.textContent = dish.updateTime?.replace("T", " ").slice(0, 16) || "—";
    const actions = row.insertCell();
    actions.append(
        rowAction("修改"),
        rowAction(enabled ? "禁用" : "启用", event =>
            changeDishStatus(event.currentTarget, dish.id, enabled ? 0 : 1)),
        rowAction("删除", event => deleteDish(event.currentTarget, dish.id)),
    );
    return row;
}

async function changeDishStatus(button, dishId, nextStatus) {
    if (nextStatus === 0 && !window.confirm("确定禁用这道菜品吗？")) return;
    const activeSession = session;
    const params = new URLSearchParams({ id: dishId });
    button.disabled = true;
    showMessage(notice);
    try {
        await request(`/admin/dish/status/${nextStatus}?${params}`, {
            method: "PUT", token: activeSession.token,
        });
        if (session === activeSession && !dishView.hidden) await loadDishes(dishPage);
    } catch (error) {
        if (session !== activeSession) return;
        if (error.status === 401) clearSession("登录已失效，请重新登录。");
        else if (!dishView.hidden) showMessage(notice, error.message);
    } finally {
        button.disabled = false;
    }
}

async function deleteDish(button, dishId) {
    if (!window.confirm("确定删除这道菜品吗？删除后无法恢复。")) return;
    const activeSession = session;
    const params = new URLSearchParams({ ids: dishId });
    button.disabled = true;
    showMessage(notice);
    try {
        await request(`/admin/dish?${params}`, {
            method: "DELETE",
            token: activeSession.token,
        });
        if (session === activeSession && !dishView.hidden) await loadDishes(dishPage);
    } catch (error) {
        if (session !== activeSession) return;
        if (error.status === 401) clearSession("登录已失效，请重新登录。");
        else if (!dishView.hidden) showMessage(notice, error.message);
    } finally {
        button.disabled = false;
    }
}

async function loadDishes(targetPage) {
    dishListRequest?.abort();
    const controller = new AbortController();
    dishListRequest = controller;
    const params = new URLSearchParams({ page: targetPage, pageSize });
    if (dishSearchKeyword) params.set("name", dishSearchKeyword);
    dishTable.hidden = true;
    dishPagination.hidden = true;
    showMessage(dishListMessage, "正在查询菜品…");
    try {
        const { total, records } = await request("/admin/dish/page?" + params, {
            token: session.token, signal: controller.signal,
        });
        if (controller.signal.aborted) return;
        const pages = Math.max(1, Math.ceil(total / pageSize));
        if (targetPage > pages) return loadDishes(pages);
        dishPage = targetPage;
        dishRecords.replaceChildren(...records.map(dishRow));
        dishTable.hidden = records.length === 0;
        const emptyMessage = dishSearchKeyword ? "没有符合条件的菜品。" : "暂无菜品。";
        showMessage(dishListMessage, records.length ? "" : emptyMessage);
        document.getElementById("dish-page-info").textContent = "共 " + total + " 条 · 第 " + dishPage + " / " + pages + " 页";
        dishPrevious.disabled = dishPage === 1;
        dishNext.disabled = dishPage === pages;
        dishPagination.hidden = false;
    } catch (error) {
        if (controller.signal.aborted) return;
        if (error.status === 401) clearSession("登录已失效，请重新登录。");
        else showMessage(dishListMessage, error.message);
    }
}

async function loadDishCategories() {
    dishCategoryRequest?.abort();
    const controller = new AbortController();
    dishCategoryRequest = controller;
    dishCategory.replaceChildren(new Option("请选择菜品分类", ""));
    dishCategory.disabled = true;
    dishCategoryRetry.hidden = true;
    showMessage(dishCategoryMessage, "正在加载分类…");
    try {
        const categories = await request("/admin/category/list?type=1", {
            token: session.token, signal: controller.signal,
        });
        if (controller.signal.aborted) return;
        dishCategory.append(...categories.map(category => new Option(category.name, category.id)));
        dishCategory.disabled = categories.length === 0;
        showMessage(dishCategoryMessage, categories.length ? "" : "请先在分类管理中启用一个菜品分类。");
        dishCategoryRetry.hidden = categories.length > 0;
    } catch (error) {
        if (controller.signal.aborted) return;
        if (error.status === 401) clearSession("登录已失效，请重新登录。");
        else {
            showMessage(dishCategoryMessage, error.message);
            dishCategoryRetry.hidden = false;
        }
    }
}

function closeDishFlavorPicker() {
    dishFlavorKind.hidden = true;
    dishAddFlavor.setAttribute("aria-expanded", "false");
    dishFlavorKind.value = "";
}

function updateDishFlavorPicker() {
    const added = new Set([...dishFlavorGroups.children].map(group => group.dataset.flavor));
    dishFlavorKind.replaceChildren(new Option("请选择口味", ""));
    for (const name of Object.keys(dishFlavorOptions)) {
        if (!added.has(name)) dishFlavorKind.add(new Option(name, name));
    }
    dishAddFlavor.disabled = dishFlavorKind.options.length === 1;
}

function addDishFlavorGroup(name) {
    const group = document.createElement("fieldset");
    group.className = "dish-flavor-group";
    group.dataset.flavor = name;

    const legend = document.createElement("legend");
    legend.textContent = name;
    const choices = document.createElement("div");
    choices.className = "dish-flavor-choices";
    for (const value of dishFlavorOptions[name]) {
        const label = document.createElement("label");
        const input = document.createElement("input");
        input.type = "checkbox";
        input.value = value;
        label.append(input, value);
        choices.append(label);
    }

    const remove = document.createElement("button");
    remove.className = "secondary dish-small-button";
    remove.type = "button";
    remove.textContent = "移除" + name;
    remove.addEventListener("click", () => {
        group.remove();
        showMessage(dishFormError);
        updateDishFlavorPicker();
        dishAddFlavor.focus();
    });
    choices.addEventListener("change", () => showMessage(dishFormError));
    group.append(legend, choices, remove);
    dishFlavorGroups.append(group);
    closeDishFlavorPicker();
    updateDishFlavorPicker();
    choices.querySelector("input").focus();
}

function clearDishPreview() {
    if (dishPreviewUrl) URL.revokeObjectURL(dishPreviewUrl);
    dishPreviewUrl = undefined;
    dishPreview.removeAttribute("src");
    dishPreview.hidden = true;
}

function resetDishForm() {
    dishSaveRequest?.abort();
    dishSaveRequest = undefined;
    dishForm.reset();
    dishFields.disabled = false;
    dishSubmitButtons.forEach(button => { button.disabled = false; });
    dishImage.setCustomValidity("");
    uploadedDishImageKey = undefined;
    clearDishPreview();
    dishFlavorGroups.replaceChildren();
    closeDishFlavorPicker();
    updateDishFlavorPicker();
    showMessage(dishFormError);
    showMessage(dishFormStatus);
}

function renderDishPage(dishes, addingDish) {
    if (dishes !== (!dishView.hidden || !dishAddView.hidden)) showMessage(notice);
    if (addingDish && dishAddView.hidden) {
        updateDishFlavorPicker();
        loadDishCategories();
    }
    if (!addingDish && !dishAddView.hidden) {
        dishCategoryRequest?.abort();
        resetDishForm();
    }
    dishAddView.hidden = !addingDish;
    dishView.hidden = !dishes || addingDish;
    if (dishView.hidden) dishListRequest?.abort();
    else loadDishes(dishPage);
}

function resetDishPage() {
    dishListRequest?.abort();
    dishCategoryRequest?.abort();
    resetDishForm();
    dishSearchForm.reset();
    dishSearchKeyword = "";
    dishRecords.replaceChildren();
    dishPage = 1;
}

dishSearchForm.addEventListener("submit", event => {
    event.preventDefault();
    dishSearchKeyword = dishSearchInput.value.trim();
    dishPage = 1;
    loadDishes(dishPage);
});
dishPrevious.addEventListener("click", () => loadDishes(dishPage - 1));
dishNext.addEventListener("click", () => loadDishes(dishPage + 1));
dishCategoryRetry.addEventListener("click", loadDishCategories);

dishAddFlavor.addEventListener("click", () => {
    dishFlavorKind.hidden = false;
    dishAddFlavor.setAttribute("aria-expanded", "true");
    dishFlavorKind.focus();
});
dishFlavorKind.addEventListener("change", () => {
    if (dishFlavorKind.value) addDishFlavorGroup(dishFlavorKind.value);
});

dishImage.addEventListener("change", () => {
    uploadedDishImageKey = undefined;
    clearDishPreview();
    dishImage.setCustomValidity("");
    showMessage(dishFormError);
    const file = dishImage.files[0];
    if (!file) return;
    if (file.size === 0 || file.size > maxDishImageBytes || !/\.(?:jpe?g|png)$/i.test(file.name)) {
        const message = "请选择不超过 5 MB 的 JPG 或 PNG 图片。";
        dishImage.setCustomValidity(message);
        showMessage(dishFormError, message);
        return;
    }
    dishPreviewUrl = URL.createObjectURL(file);
    dishPreview.src = dishPreviewUrl;
    dishPreview.hidden = false;
});

dishPreview.addEventListener("error", () => {
    if (dishPreview.hidden) return;
    const message = "无法预览这张图片，请重新选择。";
    dishImage.setCustomValidity(message);
    showMessage(dishFormError, message);
    clearDishPreview();
});

dishForm.addEventListener("submit", async event => {
    event.preventDefault();
    if (dishSaveRequest || !session?.token) return;
    if (!dishCategory.value) {
        showMessage(dishFormError, "请选择已启用的菜品分类。");
        return;
    }
    const fields = new FormData(dishForm);
    const flavors = [];
    for (const group of dishFlavorGroups.children) {
        const selected = [...group.querySelectorAll("input:checked")].map(input => input.value);
        if (selected.length === 0) {
            showMessage(dishFormError, "请选择「" + group.dataset.flavor + "」的选项，或移除这组口味。");
            group.querySelector("input").focus();
            return;
        }
        flavors.push({ name: group.dataset.flavor, value: JSON.stringify(selected) });
    }

    const controller = new AbortController();
    dishSaveRequest = controller;
    const token = session.token;
    dishFields.disabled = true;
    dishSubmitButtons.forEach(button => { button.disabled = true; });
    showMessage(dishFormError);
    try {
        if (!uploadedDishImageKey) {
            showMessage(dishFormStatus, "正在上传图片…");
            const image = new FormData();
            image.append("file", dishImage.files[0]);
            const imageKey = await request("/admin/common/upload", {
                method: "POST", body: image, token, signal: controller.signal,
            });
            if (controller.signal.aborted) return;
            uploadedDishImageKey = imageKey;
        }
        showMessage(dishFormStatus, "正在保存菜品…");
        await request("/admin/dish", {
            method: "POST", token, signal: controller.signal,
            body: {
                name: fields.get("name").trim(),
                categoryId: Number(dishCategory.value),
                price: fields.get("price"),
                imageKey: uploadedDishImageKey,
                description: fields.get("description").trim() || null,
                flavors,
            },
        });
        if (controller.signal.aborted) return;
        resetDishForm();
        showMessage(notice, "菜品已添加。");
        dishPage = 1;
        if (event.submitter?.value === "continue") document.getElementById("dish-name").focus();
        else location.hash = "dishes";
    } catch (error) {
        if (controller.signal.aborted) return;
        showMessage(dishFormStatus);
        if (error.status === 401) clearSession("登录已失效，请重新登录。");
        else showMessage(dishFormError, error.message);
    } finally {
        if (dishSaveRequest === controller) {
            dishSaveRequest = undefined;
            dishFields.disabled = false;
            dishSubmitButtons.forEach(button => { button.disabled = false; });
        }
    }
});
