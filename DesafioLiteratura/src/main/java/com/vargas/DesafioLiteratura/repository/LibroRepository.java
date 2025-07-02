package com.vargas.DesafioLiteratura.repository;

import com.vargas.DesafioLiteratura.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LibroRepository extends JpaRepository<Libro, Long> {
    List<Libro> findByTituloContainingIgnoreCase(String titulo);

    @Query("SELECT l FROM Libro l WHERE :idioma MEMBER OF l.idiomas")
    List<Libro> findByIdioma(String idioma);

    @Query("SELECT COUNT(l) > 0 FROM Libro l WHERE l.titulo = :titulo")
    boolean existsByTitulo(String titulo);

    @Query("SELECT DISTINCT l FROM Libro l LEFT JOIN FETCH l.idiomas ORDER BY l.descargas DESC")
    List<Libro> findDistinctLibrosOrderByDescargasDesc();

    @Query("SELECT l FROM Libro l ORDER BY l.id")
    List<Libro> findAllOrderedById();

    Optional<Libro> findByTitulo(String titulo);

    boolean existsByTituloAndAutorNombre(String titulo, String autorNombre);
}