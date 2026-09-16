package io.github.decadedx.smarthealthcare.mapper;

import io.github.decadedx.smarthealthcare.entity.Appointment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 86183
* @description 针对表【appointment】的数据库操作Mapper
* @createDate 2026-09-15 21:18:28
* @Entity io.github.decadedx.smarthealthcare.entity.Appointment
*/
@Mapper
public interface AppointmentMapper extends BaseMapper<Appointment> {
}




