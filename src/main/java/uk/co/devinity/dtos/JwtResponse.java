package uk.co.devinity.dtos;

public class JwtResponse {
    private String token;
    private static final String TYPE = "Bearer";

    public JwtResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return TYPE;
    }

}