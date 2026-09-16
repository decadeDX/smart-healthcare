package io.github.decadedx.smarthealthcare.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 
 * @TableName medical_record
 */
@TableName(value ="medical_record")
@Data
public class MedicalRecord {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 关联的挂号单ID
     */
    private Integer appointmentId;

    /**
     * 诊断结果
     */
    private String diagnosis;

    /**
     * 处方信息
     */
    private String prescription;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        MedicalRecord other = (MedicalRecord) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getAppointmentId() == null ? other.getAppointmentId() == null : this.getAppointmentId().equals(other.getAppointmentId()))
            && (this.getDiagnosis() == null ? other.getDiagnosis() == null : this.getDiagnosis().equals(other.getDiagnosis()))
            && (this.getPrescription() == null ? other.getPrescription() == null : this.getPrescription().equals(other.getPrescription()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getAppointmentId() == null) ? 0 : getAppointmentId().hashCode());
        result = prime * result + ((getDiagnosis() == null) ? 0 : getDiagnosis().hashCode());
        result = prime * result + ((getPrescription() == null) ? 0 : getPrescription().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", appointmentId=").append(appointmentId);
        sb.append(", diagnosis=").append(diagnosis);
        sb.append(", prescription=").append(prescription);
        sb.append("]");
        return sb.toString();
    }
}