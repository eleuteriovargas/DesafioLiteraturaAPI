package com.vargas.DesafioLiteratura.principal;

import com.vargas.DesafioLiteratura.model.Autor;
import com.vargas.DesafioLiteratura.model.Datos;
import com.vargas.DesafioLiteratura.model.DatosLibros;
import com.vargas.DesafioLiteratura.model.Libro;
import com.vargas.DesafioLiteratura.service.AutorService;
import com.vargas.DesafioLiteratura.service.ConsumoAPI;
import com.vargas.DesafioLiteratura.service.ConvierteDatos;
import com.vargas.DesafioLiteratura.service.LibroService;

import java.awt.*;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class Principal {
    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private final String URL_BASE = "https://gutendex.com/books/";
    private ConvierteDatos conversor = new ConvierteDatos();
    private final LibroService libroService;
    private final AutorService autorService;

    public Principal(LibroService libroService, AutorService autorService) {
        this.libroService = libroService;
        this.autorService = autorService;
    }

    public void muestraElMenu() {
        init();

        var opcion = -1;
        while (opcion != 0) {
            System.out.println("\n-----------");
            System.out.println("Elija la opción a través de su número");
            var menu = """
                    1 - Buscar libro por titulo
                    2 - Listar libros registrados
                    3 - Listar autores registrados
                    4 - Listar autores vivos en un determinado año
                    5 - Listar libros por idioma
                    6 - Abrir libro para leer
                    
                    0 - Salir
                    """;
            System.out.println(menu);

            try {
                opcion = teclado.nextInt();
                teclado.nextLine(); // Limpiar buffer

                switch (opcion) {
                    case 1 -> buscarLibroPorTitulo();
                    case 2 -> listarLibrosRegistrados();
                    case 3 -> listarAutoresRegistrados();
                    case 4 -> listarAutoresVivos();
                    case 5 -> listarLibrosPorIdioma();
                    case 6 -> abrirLibroEnNavegador();

                    case 0 -> System.out.println("Cerrando la aplicación...");
                    default -> System.out.println("Opción inválida. Intente nuevamente.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Error: Debe ingresar un número válido.");
                teclado.nextLine(); // Limpiar entrada incorrecta
                opcion = -1;
            }
        }
    }

    private void buscarLibroPorTitulo() {
        System.out.println("\nIngrese el título del libro que desea buscar:");
        String titulo = teclado.nextLine();

        if (titulo.isBlank()) {
            System.out.println("Error: Ingrese un título válido.");
            return;
        }

        // Modifica la URL para buscar exactamente el título
        String urlBusqueda = URL_BASE + "?search=" + URLEncoder.encode(titulo, StandardCharsets.UTF_8) + "&title=" + URLEncoder.encode(titulo, StandardCharsets.UTF_8);

        try {
            var json = consumoAPI.obtenerDatos(urlBusqueda);
            var datos = conversor.obtenerDatos(json, Datos.class);

            if (datos.resultados().isEmpty()) {
                System.out.println("\nLibro no encontrado");
                return;
            }

            // Filtra para mostrar solo la versión con más descargas
            DatosLibros libroMasPopular = datos.resultados().stream()
                    .max(Comparator.comparingDouble(DatosLibros::numeroDeDescargas))
                    .orElseThrow();

            if (!libroService.existeLibroPorTitulo(libroMasPopular.titulo())) {
                libroService.guardarLibro(libroMasPopular);
                System.out.println("\n------ LIBRO PRINCIPAL ------");
            } else {
                System.out.println("\n------ LIBRO YA REGISTRADO ------");
            }

            imprimirLibroDesdeDatos(libroMasPopular);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void listarLibrosRegistrados() {
        List<Libro> libros = libroService.listarTodosLosLibros();

        System.out.println("\nLIBROS REGISTRADOS");
        if (libros.isEmpty()) {
            System.out.println("No hay libros registrados en la base de datos.");
        } else {
            libros.forEach(this::imprimirLibro);
        }
    }

    private void listarAutoresRegistrados() {
        List<Autor> autores = autorService.autoresRegistrados();

        System.out.println("\nAUTORES REGISTRADOS");
        if (autores.isEmpty()) {
            System.out.println("No hay autores registrados en la base de datos.");
        } else {
            autores.forEach(this::imprimirAutor);
        }
    }

    private void listarAutoresVivos() {
        Integer anio = null;
        int intentos = 0;
        final int MAX_INTENTOS = 3;

        while (intentos < MAX_INTENTOS) {
            System.out.println("\nIngrese el año vivo de autor(es) que desea buscar (entre 1000 y 2025):");

            try {
                String entrada = teclado.nextLine();
                anio = Integer.parseInt(entrada);

                if (anio >= 1000 && anio <= 2025) {
                    break;
                } else {
                    System.out.println("Año fuera de rango. Debe ser entre 1000 y 2025.");
                    intentos++;
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Debe ingresar un número válido.");
                intentos++;
            }
        }

        if (anio == null) {
            System.out.println("Demasiados intentos fallidos. Volviendo al menú.");
            return;
        }

        List<Autor> autores = autorService.autoresVivosEnAnio(anio);

        System.out.println("\nAUTORES VIVOS EN " + anio);
        if (autores.isEmpty()) {
            System.out.println("No se encontraron autores vivos en ese año.");
        } else {
            autores.forEach(this::imprimirAutor);
        }
    }

    private void listarLibrosPorIdioma() {
        System.out.println("\nIngrese el idioma para buscar en los libros:");
        System.out.println("es - Español");
        System.out.println("en - Inglés");
        System.out.println("fr - Francés");
        System.out.println("pt - Portugués");
        System.out.print("\nSeleccione: ");

        String idioma = teclado.nextLine().trim().toLowerCase();

        if (!List.of("es", "en", "fr", "pt").contains(idioma)) {
            System.out.println("Idioma no válido. Use es, en, fr o pt.");
            return;
        }

        List<Libro> libros = libroService.buscarPorIdioma(idioma);

        System.out.println("\nLIBROS EN " + idioma.toUpperCase());
        if (libros.isEmpty()) {
            System.out.println("No se encontraron libros en ese idioma.");
        } else {
            libros.forEach(this::imprimirLibro);
        }
    }

    private void imprimirLibro(Libro libro) {
        System.out.println("\n------LIBRO------");
        System.out.println("Titulo: " + libro.getTitulo());
        System.out.println("Autor: " +
                (libro.getAutor() != null ? libro.getAutor().getNombre() : "Desconocido"));
        System.out.println("Idioma: " +
                (libro.getIdiomas() != null ? String.join(", ", libro.getIdiomas()) : "Desconocido"));
        System.out.println("Numero de descargas: " +
                (libro.getDescargas() != null ? libro.getDescargas() : "0"));
        System.out.println("------------------------------");
    }

    private void imprimirLibroDesdeDatos(DatosLibros libroApi) {
        System.out.println("Titulo: " + libroApi.titulo());
        System.out.println("Autor: " +
                (!libroApi.autor().isEmpty() ? libroApi.autor().get(0).nombre() : "Desconocido"));
        System.out.println("Idioma: " +
                (!libroApi.idiomas().isEmpty() ? libroApi.idiomas().get(0) : "Desconocido"));
        System.out.println("Descargas: " + libroApi.numeroDeDescargas());
        System.out.println("------------------------------");
    }

    private void imprimirAutor(Autor autor) {
        System.out.println("\nAutor: " + autor.getNombre());
        System.out.println("Fecha de nacimiento: " +
                (autor.getFechaNacimiento() != null ? autor.getFechaNacimiento() : "Desconocida"));
        System.out.println("Fecha de fallecimiento: " +
                (autor.getFechaMuerte() != null ? autor.getFechaMuerte() : "Presente"));

        if (autor.getLibros() != null && !autor.getLibros().isEmpty()) {
            System.out.println("Libros:");
            autor.getLibros().forEach(libro ->
                    System.out.println(" - " + libro.getTitulo()));
        } else {
            System.out.println("Libros: Ninguno registrado");
        }
    }

    private void abrirLibroEnNavegador() {
        System.out.println("\nIngrese el título exacto del libro:");
        String titulo = teclado.nextLine();

        Optional<Libro> libroOpt = libroService.buscarPorTituloExacto(titulo);

        if (libroOpt.isPresent() && libroOpt.get().getUrlLectura() != null) {
            try {
                System.out.println("\nAbriendo libro en tu navegador...");
                Desktop.getDesktop().browse(new URI(libroOpt.get().getUrlLectura()));
            } catch (Exception e) {
                System.out.println("Error al abrir el libro: " + e.getMessage());
                System.out.println("Puedes acceder manualmente aquí: " + libroOpt.get().getUrlLectura());
            }
        } else {
            System.out.println("Libro no encontrado o no disponible para lectura online");
        }
    }

    public void init() {
        System.out.println("Aplicación lista. Use la opción 1 para buscar y guardar libros.");
    }
}



