package com.vargas.DesafioLiteratura.service;

import com.vargas.DesafioLiteratura.model.Autor;
import com.vargas.DesafioLiteratura.model.DatosAutor;
import com.vargas.DesafioLiteratura.model.DatosLibros;
import com.vargas.DesafioLiteratura.model.Libro;
import com.vargas.DesafioLiteratura.repository.AutorRepository;
import com.vargas.DesafioLiteratura.repository.LibroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LibroService {
    @Autowired
    private LibroRepository libroRepository;

    @Autowired
    private AutorRepository autorRepository;

    public void guardarLibro(DatosLibros datosLibro) {
        // 1. Verificar si el libro ya existe antes de crearlo
        String nombreAutor = datosLibro.autor().isEmpty() ? null : datosLibro.autor().get(0).nombre();
        boolean existe = libroRepository.existsByTituloAndAutorNombre(datosLibro.titulo(), nombreAutor);

        if (existe) {
            return; // Si ya existe, salimos del método
        }

        // 2. Crear el libro solo si no existe
        Libro libro = new Libro();
        libro.setTitulo(datosLibro.titulo());
        libro.setIdiomas(datosLibro.idiomas());
        libro.setDescargas(datosLibro.numeroDeDescargas().intValue());

        // 3. Procesar el autor solo si existe
        if (!datosLibro.autor().isEmpty()) {
            DatosAutor autorApi = datosLibro.autor().get(0);

            Autor autor = new Autor();
            autor.setNombre(autorApi.nombre() != null ? autorApi.nombre() : "Anónimo");

            // Manejo seguro de fecha de nacimiento
            if (autorApi.fechaDeNacimiento() != null && !autorApi.fechaDeNacimiento().isEmpty()) {
                try {
                    autor.setFechaNacimiento(Integer.parseInt(autorApi.fechaDeNacimiento()));
                } catch (NumberFormatException e) {
                    autor.setFechaNacimiento(null);
                }
            } else {
                autor.setFechaNacimiento(null);
            }

            autor = autorRepository.save(autor);
            libro.setAutor(autor);
        }

        // 4. Procesar formatos/URL de lectura
        if (datosLibro.formatos() != null) {
            libro.setUrlLectura(
                    datosLibro.formatos().get("text/html") != null ?
                            datosLibro.formatos().get("text/html") :
                            datosLibro.formatos().get("text/plain")
            );
        }

        // 5. Guardar el libro
        libroRepository.save(libro);
    }

    public List<Libro> listarTodosLosLibros() {
        return libroRepository.findAllOrderedById();
    }

    public List<Libro> buscarPorTitulo(String titulo) {
        return libroRepository.findByTituloContainingIgnoreCase(titulo);
    }

    public boolean existeLibroPorTitulo(String titulo) {
        return libroRepository.existsByTitulo(titulo);
    }


    @Transactional(readOnly = true)
    public List<Libro> top10Descargas() {
        List<Libro> libros = libroRepository.findDistinctLibrosOrderByDescargasDesc()
                .stream()
                .limit(10)
                .toList();

        // Inicializar las colecciones necesarias
        libros.forEach(l -> {
            if (l.getIdiomas() != null) {
                l.getIdiomas().size(); // Esto fuerza la carga
            }
        });

        return libros;
    }

    public List<Libro> buscarPorIdioma(String idioma) {
        return libroRepository.findByIdioma(idioma);
    }

    public Optional<Libro> buscarPorTituloExacto(String titulo) {
        return libroRepository.findByTitulo(titulo);
    }
}