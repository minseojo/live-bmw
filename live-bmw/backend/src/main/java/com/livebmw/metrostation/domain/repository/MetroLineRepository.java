package com.livebmw.metrostation.domain.repository;

import com.livebmw.metrostation.domain.entity.MetroLine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetroLineRepository extends JpaRepository<MetroLine, Integer> {
}
