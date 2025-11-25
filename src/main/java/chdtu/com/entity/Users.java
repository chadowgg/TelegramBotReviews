package chdtu.com.entity;

import chdtu.com.enums.Faculty;
import chdtu.com.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.glassfish.grizzly.http.util.TimeStamp;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "users")
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "surname")
    private String surname;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "patronymic")
    private String patronymic;

    @Column(name = "position")
    private String position;

    @Column(name = "faculty")
    private String faculty;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<FeedbackUsers> feedbackUsersDB;

    @Transient
    private String status;
}
