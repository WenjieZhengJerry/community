package cn.lngfun.community.community.dto;

import lombok.Data;

@Data
public class HotTagDTO implements Comparable {
    private String name;
    private Integer priority;

    /**
     * 重载compareTo方法，自己构造排序规则
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(Object o) {
        return this.getPriority() - ((HotTagDTO) o).getPriority();
    }
}
