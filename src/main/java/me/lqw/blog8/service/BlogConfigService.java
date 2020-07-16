package me.lqw.blog8.service;

import com.fasterxml.jackson.core.type.TypeReference;
import me.lqw.blog8.BlogConstants;
import me.lqw.blog8.mapper.BlogConfigMapper;
import me.lqw.blog8.model.BlogConfig;
import me.lqw.blog8.model.CommentCheckStrategy;
import me.lqw.blog8.model.config.CheckStrategy;
import me.lqw.blog8.util.JacksonUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * 博客设置系实现类
 */
@Service
public class BlogConfigService implements Serializable {


    private final BlogConfigMapper configMapper;

    public BlogConfigService(BlogConfigMapper configMapper) {
        this.configMapper = configMapper;
    }

    @Transactional(readOnly = true)
    public Optional<CommentCheckStrategy> findCurrentCheckStrategy(){

        Optional<BlogConfig> checkStrategyOp = configMapper.selectByKey(BlogConstants.COMMENT_CHECK_STRATEGY);
        if(checkStrategyOp.isPresent()){
            BlogConfig blogConfig = checkStrategyOp.get();
            String value = blogConfig.getValue();
            if(StringUtils.isEmpty(value)){
                return Optional.of(CommentCheckStrategy.FIRST);
            }

            List<CheckStrategy<CommentCheckStrategy>> checkStrategies = JacksonUtil.parseObject(value, new TypeReference<List<CheckStrategy<CommentCheckStrategy>>>() {
            });

            if(checkStrategies == null || checkStrategies.isEmpty()){

                return Optional.of(CommentCheckStrategy.FIRST);
            }

            Optional<CheckStrategy<CommentCheckStrategy>> any = checkStrategies.stream().filter(CheckStrategy::getActive).findAny();
            return any.map(commentCheckStrategyCheckStrategy -> Optional.of(commentCheckStrategyCheckStrategy.getT())).orElseGet(() -> Optional.of(CommentCheckStrategy.FIRST));

        }
        return Optional.of(CommentCheckStrategy.FIRST);
    }


    @Transactional(readOnly = true)
    public List<CheckStrategy<CommentCheckStrategy>> selectAllCommentCheckStrategy(String key){
        Optional<BlogConfig> checkStrategyOp = configMapper.selectByKey(key);
        if(checkStrategyOp.isPresent()){
            BlogConfig blogConfig = checkStrategyOp.get();
            String value = blogConfig.getValue();
            if(StringUtils.isEmpty(value)){
                return Collections.emptyList();
            }
            List<CheckStrategy<CommentCheckStrategy>> checkStrategies = JacksonUtil.parseObject(value, new TypeReference<List<CheckStrategy<CommentCheckStrategy>>>() {
            });

            if(checkStrategies == null || checkStrategies.isEmpty()){

                return Collections.emptyList();
            }

            return checkStrategies;
        }
        return Collections.emptyList();
    }

}
