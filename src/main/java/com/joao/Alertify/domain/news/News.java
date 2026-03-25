package com.joao.Alertify.domain.news;

import com.joao.Alertify.domain.stock.Stock;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "news",
        uniqueConstraints = @UniqueConstraint(columnNames = {"stock_id", "url"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(nullable = false, length = 512)
    private String title;

    @Column(length = 1024)
    private String description;

    @Column(nullable = false, length = 1024)
    private String url;

    private String sourceName;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fetchedAt = LocalDateTime.now();

    @Builder.Default
    private boolean notified = false;
}

