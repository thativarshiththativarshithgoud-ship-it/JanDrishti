package in.jandrishti.model;

public record User(
    long id,
    String username,
    String email,
    String fullName,
    String phoneNumber,
    String role
) {}
