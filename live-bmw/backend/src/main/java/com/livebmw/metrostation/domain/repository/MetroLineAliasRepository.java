package com.livebmw.metrostation.domain.repository;

import com.livebmw.metrostation.domain.entity.MetroLineAlias;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MetroLineAliasRepository extends JpaRepository<MetroLineAlias, Integer> {
    Optional<MetroLineAlias> findByNormalizedName(String normalizedName);
}
