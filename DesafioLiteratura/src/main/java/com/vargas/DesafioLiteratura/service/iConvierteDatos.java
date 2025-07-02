package com.vargas.DesafioLiteratura.service;


public interface iConvierteDatos {

    <T> T obtenerDatos(String json, Class<T> clase);
}