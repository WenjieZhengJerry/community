package cn.lngfun.community.community.cache;

import cn.lngfun.community.community.dto.HotTagDTO;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Data
public class HotTagCache {
    private List<String> hots = new ArrayList<>();

    /**
     * 给无序的标签元素按从大到小排序
     *
     * @param tags
     */
    public void sortTags(Map<String, Integer> tags) {
        int max = 10;
        //新建一个最大长度为10的优先级队列
        PriorityQueue<HotTagDTO> priorityQueue = new PriorityQueue<>(max);

        tags.forEach((name, priority) -> {
            HotTagDTO hotTagDTO = new HotTagDTO();
            hotTagDTO.setName(name);
            hotTagDTO.setPriority(priority);
            if (priorityQueue.size() < max) {
                //如果当前队列长度小于最大长度则直接入队
                priorityQueue.add(hotTagDTO);
            } else {
                //队列已满，取出队首元素作比较，大于队首元素则入队
                HotTagDTO minHot = priorityQueue.peek();
                if (hotTagDTO.compareTo(minHot) > 0) {
                    priorityQueue.poll();
                    priorityQueue.add(hotTagDTO);
                }
            }
        });
        List<String> sortedTags = new ArrayList<>();
        //依次取出队列元素并add进入list里
        HotTagDTO poll = priorityQueue.poll();
        while (poll != null) {
            //List里的add(index, Element)：在索引位置采用头插法插入元素
            sortedTags.add(0, poll.getName());
            poll = priorityQueue.poll();
        }

        hots = sortedTags;
    }

}
