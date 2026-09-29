package backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "conversations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE conversations set active = false, modification_date = NOW() WHERE id = ?")
@SQLRestriction("active = true")
public class Conversation{

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @Column(name = "title", updatable = true)
    private String title;

    @Column(name = "modification_date", nullable = false)
    private Instant modificationDate;

    @Column(name = "creation_date", nullable = false, updatable = false)
    private Instant creationDate;

    @Column(name = "active", nullable = false, columnDefinition = "boolean default true" )
    private boolean active;

    @PrePersist
    public void onPrePersist(){

        this.id = UUID.randomUUID().toString();
        Instant now = Instant.now();
        this.creationDate = now;
        this.modificationDate = now;
        this.active = true;

    }

    @PreUpdate
    public void onPreUpdate(){
        this.modificationDate = Instant.now();
    }

}
