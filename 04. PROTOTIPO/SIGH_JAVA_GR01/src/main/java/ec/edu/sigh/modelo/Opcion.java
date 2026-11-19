package ec.edu.sigh.modelo;

//fila de catálogo para llenar un combo de (codigo+descripcion).
public record Opcion(String codigo, String descripcion) {
      public String getCodigo(){
          return codigo;
      }
      public String getDescripcion(){
          return descripcion;
      }
}