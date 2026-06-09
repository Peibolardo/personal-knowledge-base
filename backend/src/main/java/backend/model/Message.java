package backend.model;

import jakarta.persistence.*;
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

    // TODO add to message userId (Not being used users for now) 09/06/2026
    /**
    @ManyToOne
    @Column(name = "userId", updatable = false, nullable = false)
    private String user_id;
     */

    @Column(name = "information", updatable = false, nullable = false)
    private String information;

}
