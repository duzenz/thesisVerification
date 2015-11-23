package model;

public class User {

    private int userId;
    private String gender;
    private int age;
    private String country;
    private String registered;
    private String ageCol;
    private String registerCol;
    
    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public String getGender() {
        return gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }
    public int getAge() {
        return age;
    }
    public void setAge(int age) {
        this.age = age;
    }
    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
    }
    public String getRegistered() {
        return registered;
    }
    public void setRegistered(String registered) {
        this.registered = registered;
    }
    public String getAgeCol() {
        return ageCol;
    }
    public void setAgeCol(String ageCol) {
        this.ageCol = ageCol;
    }
    public String getRegisterCol() {
        return registerCol;
    }
    public void setRegisterCol(String registerCol) {
        this.registerCol = registerCol;
    }
    @Override
    public String toString() {
        return "User [userId=" + userId + ", gender=" + gender + ", age=" + age + ", country=" + country + ", registered=" + registered + ", ageCol=" + ageCol + ", registerCol=" + registerCol + "]";
    }
    
}
