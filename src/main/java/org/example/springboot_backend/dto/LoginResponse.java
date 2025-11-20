package org.example.springboot_backend.dto;
import java.util.List;
import org.example.springboot_backend.controller.SubscriptionResponse;

public class LoginResponse {
    private String token;
    private String email;
    private String fullName;
    private String name;
    private String role;
    private SubscriptionResponse subscription;
    private List<String> permissions;
    private List<UserExtraStorageResponse> extraStorageSubscriptions;

    public LoginResponse(String token, String email, String fullname, String name, String role, 
        SubscriptionResponse subscription, List<String> permissions, List<UserExtraStorageResponse> extraStorageSubscriptions) {
        this.token = token;
        this.email = email;
        this.fullName = fullname;
        this.name = name;
        this.role = role;
        this.subscription = subscription;
        this.permissions = permissions;
        this.extraStorageSubscriptions = extraStorageSubscriptions;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public SubscriptionResponse getSubscription() { return subscription; }
    public void setSubscription(SubscriptionResponse subscription) { this.subscription = subscription; }
    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
    public List<UserExtraStorageResponse> getExtraStorageSubscriptions() { return extraStorageSubscriptions; }
    public void setExtraStorageSubscriptions(List<UserExtraStorageResponse> extraStorageSubscriptions) { this.extraStorageSubscriptions = extraStorageSubscriptions; }
}
