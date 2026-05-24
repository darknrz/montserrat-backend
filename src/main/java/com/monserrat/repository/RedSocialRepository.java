package com.monserrat.repository;

import com.monserrat.entity.RedSocial;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RedSocialRepository extends JpaRepository<RedSocial, Long> {
    List<RedSocial> findByActivoTrueOrderByOrdenAsc();
}