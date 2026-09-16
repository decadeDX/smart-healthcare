package io.github.decadedx.smarthealthcare.mapper;

import io.github.decadedx.smarthealthcare.entity.Department;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 86183
* @description 针对表【department】的数据库操作Mapper
* @createDate 2026-09-15 21:26:44
* @Entity io.github.decadedx.smarthealthcare.entity.Department
*/
@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {

}




