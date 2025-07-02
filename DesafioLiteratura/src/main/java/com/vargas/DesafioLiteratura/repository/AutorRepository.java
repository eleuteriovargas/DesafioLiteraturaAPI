package com.vargas.DesafioLiteratura.repository;

import com.vargas.DesafioLiteratura.model.Autor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AutorRepository extends JpaRepository<Autor, Long> {
    @Query("SELECT a FROM Autor a WHERE a.fechaNacimiento <= :anio AND (a.fechaMuerte IS NULL OR a.fechaMuerte >= :anio)")
    List<Autor> findAutoresVivosEnAnio(Integer anio);

    List<Autor> findByNombreContainingIgnoreCase(String nombre);
}
