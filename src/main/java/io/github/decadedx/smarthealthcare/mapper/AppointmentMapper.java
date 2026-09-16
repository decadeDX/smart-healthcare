package io.github.decadedx.smarthealthcare.mapper;

import io.github.decadedx.smarthealthcare.entity.Appointment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
* @author 86183
* @description 针对表【appointment】的数据库操作Mapper
* @createDate 2026-09-15 21:18:28
* @Entity io.github.decadedx.smarthealthcare.entity.Appointment
*/
@Mapper
public interface AppointmentMapper extends BaseMapper<Appointment> {

    /**
     * 一次读取组装挂号详情所需的全部字段；关联表使用 LEFT JOIN，供 Service 区分资源不存在和关联数据缺失。
     *
     * @param appointmentId 挂号单主键
     * @return 联合查询结果；同一挂号单出现多行时表示病历基数不符合 0..1 约束
     */
    List<AppointmentDetailSource> findAppointmentDetailSource(@Param("appointmentId") Integer appointmentId);
}




