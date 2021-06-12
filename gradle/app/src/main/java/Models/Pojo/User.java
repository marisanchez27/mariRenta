package Models.Pojo;

public class User {
private String LastName;
private String Name;
private String Email;
private String Role;
private byte[]Image;
private String Active;

public User(String lastName,
            String name,
            String email,
            String role,
            byte[] image,
            String active) {
    LastName = lastName;
    Name = name;
    Email = email;
    Role = role;
    Image = image;
    Active = active;
}

public String getLastName() {
    return LastName;
}

public String getName() {
    return Name;
}

public String getEmail() {
    return Email;
}

public String getRole() {
    return Role;
}

public byte[] getImage() {
    return Image;
}

public String getActive() {
    return Active;
}
}

