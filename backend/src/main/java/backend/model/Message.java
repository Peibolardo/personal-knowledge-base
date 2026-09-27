package backend.model;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "messages")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE messages set active = false, modification_date = NOW() WHERE id = ?")
@SQLRestriction("active = true")
public class Message {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    /**
     * Column to determine if the message comes from the user or from the OpenAI API
     */
    @Column(name = "role", updatable = false, nullable = false)
    private String role;

    @Column(name = "content", updatable = false, nullable = false )
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "creation_date", updatable = false, nullable = false)
    private Instant creationDate;

    @Column(name = "active", nullable = false)
    private boolean active;

    @PrePersist
    public void onPrePersist() {

        this.id = UUID.randomUUID().toString();
        this.creationDate = Instant.now();
        this.active = true;

    }

}
