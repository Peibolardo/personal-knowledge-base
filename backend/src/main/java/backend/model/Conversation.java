package backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;

@Entity
@Table(name = "conversations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@RequiredArgsConstructor
@SQLDelete(sql = "UPDATE conversations set active = false, modification_date = NOW() WHERE id = ?")
@SQLRestriction("active = true")
public class Conversation {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @Column(name = "creation_date", nullable = false)
    private Instant creationDate;

    @PrePersist
    public void onPrePersist(){

        this.creationDate = Instant.now();

    }

}
