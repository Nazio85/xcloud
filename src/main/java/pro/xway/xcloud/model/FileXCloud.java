package pro.xway.xcloud.model;

import javax.persistence.*;
import javax.xml.crypto.Data;
import java.util.Calendar;
import java.util.Date;

@Entity
@Table(name = "files")
public class FileXCloud {
    @Id
    @GeneratedValue
    private long id;
    private String name;
    private Long parentId;
    private Long userId;
    private Long size;
    private Calendar calendar;

    public FileXCloud(String name, Long parentId, Long userId, Long size) {
        this.name = name;
        this.parentId = parentId;
        this.userId = userId;
        this.calendar = Calendar.getInstance();
        this.size = size;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

    protected FileXCloud() {
    }

    public Long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void updateDate(){
        this.calendar = Calendar.getInstance();
    }
}
