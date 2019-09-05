package cn.lngfun.community.community.cache;

import java.util.HashMap;
import java.util.Map;

public class CategoryCache {
    private static Map<Integer, String> category = new HashMap<>();

    CategoryCache() {
        category.put(1, "提问");
        category.put(2, "讨论");
        category.put(3, "分享");
        category.put(4, "建议");
        category.put(5, "Bug");
    }

    public static String getCategoryName(Integer categoryType) {
        return category.get(categoryType);
    }
}
