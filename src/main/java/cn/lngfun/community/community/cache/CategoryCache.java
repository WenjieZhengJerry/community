package cn.lngfun.community.community.cache;

import java.util.HashMap;
import java.util.Map;

public class CategoryCache {
    private static Map<Integer, String> categoryName = new HashMap<>();
    private static Map<Integer, String> categoryColor = new HashMap<>();

    public static String getCategoryName(Integer categoryType) {
        categoryName.put(1, "提问");
        categoryName.put(2, "讨论");
        categoryName.put(3, "分享");
        categoryName.put(4, "建议");
        categoryName.put(5, "Bug");
        return categoryName.get(categoryType);
    }

    public static String getCategoryColor(Integer categoryType) {
        categoryColor.put(1, "#5bc0de");
        categoryColor.put(2, "#5cb85c");
        categoryColor.put(3, "#337ab7");
        categoryColor.put(4, "#f0ad4e");
        categoryColor.put(5, "#d9534f");
        return categoryColor.get(categoryType);
    }
}
