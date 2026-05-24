package com.monserrat.repository;

import com.monserrat.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VideoRepository extends JpaRepository<Video, Long> {
    List<Video> findByActivoTrueOrderByOrdenAsc();
    List<Video> findByTagAndActivoTrue(String tag);
}