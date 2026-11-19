package com.example.scantoserve.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "restaurants",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_restaurant_owner_name",
                        columnNames = {"owner_id", "name"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_restaurant_owner",
                        columnList = "owner_id"
                ),
                @Index(
                                name = "idx_restaurant_status",
                                columnList = "status"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,length=100)
    private String name;

    @Column(nullable = false,length=255)
    private String description;

    @Column(nullable = false,length=100)
    private String email;

    @Column(nullable = false,length=15,unique = true)
    private String mobileNumber;

    @Column(nullable = false,length=100)
    private String addressLine;

    @Column(nullable = false,length=30)
    private String city;

    @Column(nullable = false,length=30)
    private String state;

    @Column(nullable = false,length=6)
    private String postalCode;

    @Column(nullable = false,length=20)
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length=10)
    private RestaurantStatus status;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "owner_id",
    nullable = false,
    foreignKey = @ForeignKey(name = "fk_restaurant_owner"))
    private User owner;

    @CreationTimestamp
    @Column(nullable = false,updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

        if(this.status==null){
            this.status=RestaurantStatus.ACTIVE;
        }
    }

    @PreUpdate
    public void onUpdate() {
        LocalDateTime now = LocalDateTime.now();
        this.updatedAt = now;

    }
}
