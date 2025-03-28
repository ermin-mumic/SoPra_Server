package ch.uzh.ifi.hase.soprafs24.rest.dto;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;

public class UserGetDTO {

    private Long id;
    private String username;
    private String creationDate;
    private UserStatus status;
    private String birthday;



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public String getBirthday() { return birthday; }

    public void setBirthday(String birthday) { this.birthday = birthday; }

    public String getCreationDate() { return creationDate; }

    public void setCreationDate(String creationDate) { this.creationDate = creationDate; }

}
