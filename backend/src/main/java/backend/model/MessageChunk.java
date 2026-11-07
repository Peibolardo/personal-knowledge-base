package backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "message_chunks", uniqueConstraints = @UniqueConstraint(
                name = "uk_message_chunks_message_index",
                columnNames = {"message_id", "chunk_index"}
        )
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE message_chunks set active = false WHERE id = ?")
@SQLRestriction("active = true")
public class MessageChunk {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "token_count", nullable = false)
    private Integer tokenCount;

    /**
     * Nullable until embedding generation is introduced.
     */
    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 1536)
    @Column(name = "embedding", columnDefinition = "vector(1536)")
    private float[] embedding;

    @Column(name = "embedding_model", length = 255)
    private String embeddingModel;

    @Column(name = "creation_date", updatable = false, nullable = false)
    private Instant creationDate;

    @Column(name = "active", nullable = false)
    private boolean active;

    @PrePersist
    public void onPrePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        this.creationDate = Instant.now();
        this.active = true;
    }
}
