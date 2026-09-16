package io.github.decadedx.smarthealthcare.mapper;

import io.github.decadedx.smarthealthcare.entity.Campus;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

/**
* @author 86183
* @description 针对表【campus】的数据库操作Mapper
* @createDate 2026-09-15 21:26:40
* @Entity io.github.decadedx.smarthealthcare.entity.Campus
*/
@Mapper
public interface CampusMapper extends BaseMapper<Campus> {
}




