package com.livebmw.metro.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "metro_line_alias",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_metro_line_alias_normalized", columnNames = "normalized_name")
        },
        indexes = {
                @Index(name = "idx_metro_line_alias_normalized", columnList = "normalized_name")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class MetroLineAlias {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** 원본 표기 (예: "9호선(연장)") */
    @Column(name = "raw_name", nullable = false, length = 100)
    private String rawName;

    /** 정규화된 표기 (공백 제거/BOM 제거 등) - 검색 키 */
    @Column(name = "normalized_name", nullable = false, length = 100)
    private String normalizedName;

    /** 매핑 대상 라인 코드 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "line_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_alias_line"))
    private MetroLine line;
}
