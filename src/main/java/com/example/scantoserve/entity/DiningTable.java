package com.example.scantoserve.entity;

import jakarta.persistence.*;
import lombok.*;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "dining_table",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_restaurant_table_number",
                        columnNames = {"table_number","restaurant_id"}
                ),
                @UniqueConstraint(
                        name = "uk_dining_table_qr_token",
                        columnNames = "qr_token"
                )
        },
        indexes = {
                @Index(
                        name = "idx_dining_table_restaurant",
                        columnList = "restaurant_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiningTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer tableNumber;

    @Column(nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DiningTableStatus status;

    @Column(nullable = false, length = 40,unique = true,name = "qr_token")
    private String qrToken;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;

        if (this.status == null) {
            this.status = DiningTableStatus.AVAILABLE;
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
