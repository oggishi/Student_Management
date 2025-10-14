package ong.myapp.studentmanagement.model;


public class UserActivityHistory {
    private String name;
    private String userId;

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    private String timestamp;
    private String avatar;

    public UserActivityHistory(String avatar,String name,String userId, String timestamp) {
        this.avatar=avatar;
        this.name=name;
        this.userId = userId;
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    UserActivityHistory(){

}
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }


}
