package vn.hoidanit.jobhunter.domain.response;

import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import vn.hoidanit.jobhunter.util.constant.Level;

@Getter
@Setter
public class ResUpdateJobDTO {
    private long id;
    private String name;
    private String location;
    private double salary;
    private int quantity;
    private Level level;
    private Instant startDate;
    private Instant endDate;
    private boolean active;
    private List<String> skills;
    private Instant updatedAt;
    private String updatedBy;
}
