package com.livebmw.metro.domain.repository;

import com.livebmw.metro.domain.entity.MetroLine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetroLineRepository extends JpaRepository<MetroLine, Integer> {
}
