package vn.hoidanit.jobhunter.domain.response.user;

import java.time.Instant;

// import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;
import vn.hoidanit.jobhunter.util.constant.Gender;

@Getter
@Setter
public class ResCreateUserDTO {
    private long id;
    private String name;
    private String email;
    private Gender gender;
    private String address;
    private int age;

    // @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss a", timezone = "GMT+7")
    private Instant createdAt;

    private CompanyUserCreate company;

    @Getter
    @Setter
    public static class CompanyUserCreate {
        private long id;
        private String name;
    }
}
