package com.product.dao.helpers;

import com.product.model.Producto;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.types.Decimal128;

import java.math.BigDecimal;

public class MongoProductoMapper {


    public Producto docAProducto(Document doc) {
        if (doc == null) {
            return null;
        }

        Producto producto = new Producto();
        try {
            producto.setCodigo(doc.getInteger("codigo", 0)); 
            producto.setNombre(doc.getString("nombre")); 
            
            Decimal128 precioDecimal128 = doc.get("precio", Decimal128.class);
            producto.setPrecio(precioDecimal128 != null ? precioDecimal128.bigDecimalValue() : BigDecimal.ZERO);
            
            producto.setStock(doc.getInteger("stock", 0));

        } catch (ClassCastException e) {
            System.err.println("ERROR [MongoProductoMapper]: Tipo de dato inesperado en documento MongoDB: " + e.getMessage() + " - Doc: " + doc.toJson());
            return null; 
        }

        if (producto.getCodigo() == 0 && !doc.containsKey("codigo")) {
            return null;
        }
        return producto;
    }

    // Convierte un objeto Producto a un Documento BSON.
    public Document productoADoc(Producto producto) {

        Document doc = new Document();
        doc.append("codigo", producto.getCodigo());
        doc.append("nombre", producto.getNombre());
        
        BigDecimal precio = producto.getPrecio();

        doc.append("precio", new Decimal128(precio != null ? precio : BigDecimal.ZERO)); 
        doc.append("stock", producto.getStock());
        return doc;
    }

    // Obtiene el siguiente código secuencial para un nuevo producto en la colección MongoDB.
    public int obtenerSgteCodigo(MongoCollection<Document> coleccion) {
        Document docMaxCodigo = coleccion.find()
                                        .sort(Sorts.descending("codigo")) 
                                        .projection(Projections.include("codigo")) 
                                        .limit(1)
                                        .first();
        int sgteCodigo = 1;
        if (docMaxCodigo != null) {
            Integer codigoActual = docMaxCodigo.getInteger("codigo");
            if (codigoActual != null) { 
                sgteCodigo = codigoActual + 1;
            }
        }
        return sgteCodigo;
    }
}
