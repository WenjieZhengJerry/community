package cn.lngfun.community.community.cache;

import cn.lngfun.community.community.dto.TagDTO;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TagCache {
    public static List<TagDTO> get() {
        List<TagDTO> tagDTOS = new ArrayList<>();
        TagDTO program = new TagDTO();
        program.setCategoryName("开发语言");
        program.setTags(Arrays.asList("Java","php","css","html","javascript","node.js","asp.net","python","c","c++","golang","objective-c","shell","swift","c#","sass","ruby","bash","less","lua","scala","perl"));

        TagDTO framework = new TagDTO();
        framework.setCategoryName("平台框架");
        framework.setTags(Arrays.asList("Spring","spring MVC","Spring-Boot","struts","yii","koa","laravel"));

        TagDTO server = new TagDTO();
        server.setCategoryName("服务器");
        server.setTags(Arrays.asList("linux","nginx","docker","apache","ubuntu","centos","tomcat","负载均衡","unix","hadoop","windows-server"));

        TagDTO database = new TagDTO();
        database.setCategoryName("数据库");
        database.setTags(Arrays.asList("mysql","redis","sql","oracle","nosql","sqlserver","mongodb","memcached","sqlite"));

        TagDTO tool = new TagDTO();
        tool.setCategoryName("开发工具");
        tool.setTags(Arrays.asList("eclipse","vscode","git","github","idea","maven","svn","visual-studio","subline-text","vim"));

        tagDTOS.add(program);
        tagDTOS.add(framework);
        tagDTOS.add(server);
        tagDTOS.add(database);
        tagDTOS.add(tool);

        return tagDTOS;
    }

    public static String filterInvalid(String tags) {
        String[] splitTag = StringUtils.split(tags, ",");
        List<TagDTO> tagDTOS = get();

        List<String> tagList = tagDTOS.stream().flatMap(tag -> tag.getTags().stream()).collect(Collectors.toList());
        String invalid = Arrays.stream(splitTag).filter(t -> !tagList.contains(t)).collect(Collectors.joining(","));

        return invalid;
    }
}
