package com.vargas.DesafioLiteratura.service;

import com.vargas.DesafioLiteratura.model.Autor;
import com.vargas.DesafioLiteratura.repository.AutorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AutorService {
    @Autowired
    private AutorRepository autorRepository;

    public List<Autor> autoresRegistrados() {
        return autorRepository.findAll();
    }

    public List<Autor> autoresVivosEnAnio(Integer anio) {
        return autorRepository.findAutoresVivosEnAnio(anio);
    }
}