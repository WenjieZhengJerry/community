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

    @Scheduled(fixedRate = 1000 * 60 * 60 * 3)
    public void hotTagSchedule() {
        int offset = 0;
        int size = 10;
        List<Question> list = new ArrayList<>();
        Map<String, Integer> priorities = new HashMap<>();
        //取出所有问题的标签并统计权重
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
        //给标签根据权重从大到小排序
        hotTagCache.sortTags(priorities);
    }
}
