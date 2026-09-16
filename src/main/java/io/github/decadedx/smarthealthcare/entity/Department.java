package io.github.decadedx.smarthealthcare.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 
 * @TableName department
 */
@TableName(value ="department")
@Data
public class Department {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 科室名称
     */
    private String name;

    /**
     * 所属院区ID
     */
    private Integer campusId;

    /**
     * 科室主任(医生)ID
     */
    private Integer directorId;

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
        Department other = (Department) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getName() == null ? other.getName() == null : this.getName().equals(other.getName()))
            && (this.getCampusId() == null ? other.getCampusId() == null : this.getCampusId().equals(other.getCampusId()))
            && (this.getDirectorId() == null ? other.getDirectorId() == null : this.getDirectorId().equals(other.getDirectorId()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((getCampusId() == null) ? 0 : getCampusId().hashCode());
        result = prime * result + ((getDirectorId() == null) ? 0 : getDirectorId().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", name=").append(name);
        sb.append(", campusId=").append(campusId);
        sb.append(", directorId=").append(directorId);
        sb.append("]");
        return sb.toString();
    }
}