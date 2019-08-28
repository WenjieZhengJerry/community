package cn.lngfun.community.community.schedule;

import cn.lngfun.community.community.cache.HotTagCache;
import cn.lngfun.community.community.mapper.QuestionMapper;
import cn.lngfun.community.community.model.Question;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class HotTagTasks {

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private HotTagCache hotTagCache;

    @Scheduled(fixedRate = 5000)
    public void hotTagSchedule() {
        int offset = 0;
        int size = 2;
        log.info("开始时间是：{}", new Date());
        List<Question> list = new ArrayList<Question>();
        Map<String, Integer> priorities = new HashMap<>();

        while (offset == 0 || list.size() == size) {
            list = questionMapper.list(offset, size);
            for (Question question : list) {
                String[] tags = StringUtils.split(question.getTag(), ",");
                for (String tag : tags) {
                    Integer priority = priorities.get(tag);
                    if (priority != null) {
                        priorities.put(tag, priority + 5 + question.getCommentCount());
                    } else {
                        priorities.put(tag, 5 + question.getCommentCount());
                    }
                }
            }
            offset += size;
        }

        hotTagCache.setTags(priorities);
        hotTagCache.getTags().forEach(
                (k, v) -> {
                    System.out.println(k + ":" + v);
                }
        );
        log.info("结束时间是：{}", new Date());
    }
}
