package backend.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

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

    private String input;

    private String conversationId;

}
