package chdtu.com.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "feedback_users")
public class FeedbackUsers {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "feedback")
    private String feedback;

    @Column(name = "mood_feedback")
    private String moodFeedback;

    @Column(name = "critical")
    private Integer critical;

    @Column(name = "recommendation")
    private String recommendation;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;
}
