package com.wesker.order.constant;

/** 后端业务异常与请求校验的固定提示。 */
public final class MessageConstant {

    private MessageConstant() {
    }

    // 登录与员工
    public static final String LOGIN_FAILED = "用户名或密码错误，或账号已锁定";
    public static final String LOGIN_REQUIRED = "请先登录";
    public static final String USERNAME_ALREADY_EXISTS = "用户名已存在";
    public static final String USERNAME_USED_BY_ADMIN = "该账号已被管理员使用，请更换员工账号";
    public static final String EMPLOYEE_NOT_FOUND = "员工不存在";

    // 分类
    public static final String CATEGORY_NAME_ALREADY_EXISTS = "分类名称已存在";
    public static final String CATEGORY_TYPE_REQUIRED = "请选择分类类型";
    public static final String CATEGORY_NOT_FOUND = "分类不存在";
    public static final String CATEGORY_BE_RELATED_BY_DISH = "当前分类关联了菜品，不能删除";
    public static final String CATEGORY_BE_RELATED_BY_SETMEAL = "当前分类关联了套餐，不能删除";
    public static final String CATEGORY_HAS_REFERENCES = "当前分类存在关联数据，不能删除";

    // 菜品
    public static final String ENABLED_DISH_CATEGORY_REQUIRED = "请选择已启用的菜品分类";
    public static final String DISH_NOT_FOUND = "菜品不存在";
    public static final String DISH_DELETE_SELECTION_REQUIRED = "请选择要删除的菜品";
    public static final String DISH_ON_SALE = "起售中的菜品不能删除";
    public static final String DISH_BE_RELATED_BY_SETMEAL = "当前菜品关联了套餐，不能删除";

    // 图片上传
    public static final String INVALID_IMAGE_SIZE = "请选择不超过 5 MB 的图片";
    public static final String INVALID_IMAGE_FORMAT = "请选择有效的 JPG 或 PNG 图片";
    public static final String IMAGE_READ_FAILED = "无法读取图片，请重新选择";
    public static final String UPLOAD_FAILED = "图片上传失败，请检查 OSS 配置或稍后重试";
    public static final String IMAGE_TOO_LARGE = "图片大小不能超过 5 MB";

    // 请求校验
    public static final String INVALID_REQUEST = "请求参数不符合要求";
}
