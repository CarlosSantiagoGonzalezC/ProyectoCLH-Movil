package com.example.appmovilclh1.modelo

class Producto constructor(id:Int ,codigo:Int, nombre:String, cantidad:Int, precio:Int,
                           descripcion:String, urlImagen:String, categoria:Int, usuario:Int) {
    var id = id
    var codigo = codigo
    var nombre = nombre
    var cantidad = cantidad
    var precio = precio
    var descripcion = descripcion
    var usuario = usuario
    var categoria = categoria
    var urlImagen = urlImagen

    override fun toString(): String {
        return nombre
    }
}