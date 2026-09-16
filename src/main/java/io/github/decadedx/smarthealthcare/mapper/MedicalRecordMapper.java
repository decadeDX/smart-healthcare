package io.github.decadedx.smarthealthcare.mapper;

import io.github.decadedx.smarthealthcare.entity.MedicalRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 86183
* @description 针对表【medical_record】的数据库操作Mapper
* @createDate 2026-09-15 21:26:49
* @Entity io.github.decadedx.smarthealthcare.entity.MedicalRecord
*/
@Mapper
public interface MedicalRecordMapper extends BaseMapper<MedicalRecord> {

}




