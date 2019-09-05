package cn.lngfun.community.community.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PagingDTO<T> {
    private List<T> data;
    private boolean showPrevious;
    private boolean showNext;
    private boolean showFirstPage;
    private boolean showEndPage;
    private Integer currentPage;
    private Integer totalPage;
    private Integer totalCount;
    private List<Integer> pages = new ArrayList<>();

    public void setPaging(Integer totalPage, Integer page) {
        this.totalPage = totalPage;
        this.currentPage = page;
        pages.add(page);
        for (int i = 1; i <= 3; i++) {
            if (page - i > 0) {
                pages.add(0, page - i);
            }

            if (page + i <= totalPage) {
                pages.add(page + i);
            }
        }

        //是否展示上一页
        if (page == 1) {
            showPrevious = false;
        } else {
            showPrevious = true;
        }
        //是否展示下一页
        if (page == totalPage) {
            showNext = false;
        } else {
            showNext = true;
        }
        //是否展示首页
        if (pages.contains(1)) {
            showFirstPage = false;
        } else {
            showFirstPage = true;
        }
        //是否展示尾页
        if (pages.contains(totalPage)) {
            showEndPage = false;
        } else {
            showEndPage = true;
        }
    }

    public Integer calculateOffset(Integer page, Integer size) {
        //计算totalPage
        Integer totalPage = totalCount % size == 0 ? totalCount / size : totalCount / size + 1;

        //边界控制
        if (page < 1) {
            page = 1;
        }
        if (page > totalPage) {
            page = totalPage;
        }
        this.setPaging(totalPage, page);//装填页面元素

        return page > 0 ? size * (page - 1) : 0;//如果页数等于0 代表这一页没有元素
    }
}
