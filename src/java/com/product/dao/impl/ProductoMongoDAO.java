package com.product.dao.impl;

import com.product.model.Producto;
import com.product.util.MongoConexion;
import com.product.dao.ProductoDAO;
import com.product.dao.helpers.MongoProductoMapper; 
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.bson.conversions.Bson;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ProductoMongoDAO implements ProductoDAO {

    private static final String NOMBRE_COLECCION = "ventas";
    private final MongoProductoMapper mapper;

    public ProductoMongoDAO() {
        this.mapper = new MongoProductoMapper();
    }

    // Obtiene la colección de MongoDB.
    private MongoCollection<Document> obtenerColeccion() {
        MongoDatabase db = MongoConexion.getDatabase();
        if (db == null) throw new MongoException("Fallo al obtener base de datos MongoDB.");
        MongoCollection<Document> coleccion = db.getCollection(NOMBRE_COLECCION);
        if (coleccion == null) throw new MongoException("Fallo al obtener colección: " + NOMBRE_COLECCION);
        return coleccion;
    }

    @Override
    public void insertar(Producto producto) throws SQLException {
        if (producto == null) 
            throw new IllegalArgumentException("Producto a insertar no puede ser nulo.");
        try {
            MongoCollection<Document> coleccion = obtenerColeccion();
            if (producto.getCodigo() <= 0) {
                producto.setCodigo(mapper.obtenerSgteCodigo(coleccion));
            }
            coleccion.insertOne(mapper.productoADoc(producto));
        } catch (MongoException e) {
            throw new SQLException("Mongo DAO: Error al insertar. " + e.getMessage(), e);
        }
    }

    @Override
    public List<Producto> listarTodos() throws SQLException {
        List<Producto> lista = new ArrayList<>();
        try (MongoCursor<Document> cursor = obtenerColeccion().find().sort(Sorts.ascending("codigo")).iterator()) {
            while (cursor.hasNext()) {
                Producto p = mapper.docAProducto(cursor.next());
                if (p != null) lista.add(p);
            }
        } catch (MongoException e) {
            throw new SQLException("Mongo DAO: Error al listar. " + e.getMessage(), e);
        }
        return lista;
    }

    @Override
    public List<Producto> buscarPorNombre(String nombre) throws SQLException {
        if (nombre == null || nombre.trim().isEmpty()) return listarTodos();
        
        List<Producto> lista = new ArrayList<>();
        Pattern regex = Pattern.compile(Pattern.quote(nombre.trim()), Pattern.CASE_INSENSITIVE);
        Bson filtro = Filters.regex("nombre", regex);

        try (MongoCursor<Document> cursor = obtenerColeccion().find(filtro).sort(Sorts.ascending("nombre")).iterator()) {
            while (cursor.hasNext()) {
                Producto p = mapper.docAProducto(cursor.next());
                if (p != null) lista.add(p);
            }
        } catch (MongoException e) {
            throw new SQLException("Mongo DAO: Error al buscar por nombre. " + e.getMessage(), e);
        }
        return lista;
    }

    @Override
    public Producto obtenerPorCodigo(int codigo) throws SQLException {
        if (codigo <= 0) return null;
        try {
            Document doc = obtenerColeccion().find(Filters.eq("codigo", codigo)).first();
            return mapper.docAProducto(doc);
        } catch (MongoException e) {
            throw new SQLException("Mongo DAO: Error al obtener por código. " + e.getMessage(), e);
        }
    }

    @Override
    public boolean actualizar(Producto producto) throws SQLException {
        if (producto == null) throw new IllegalArgumentException("Producto a actualizar no puede ser nulo.");
        if (producto.getCodigo() <= 0) throw new IllegalArgumentException("Código de producto inválido para actualizar.");

        try {
            Bson filtro = Filters.eq("codigo", producto.getCodigo());

            Document cambios = new Document();
            if (producto.getNombre() != null) cambios.append("nombre", producto.getNombre());
            BigDecimal precio = producto.getPrecio();
            cambios.append("precio", new Decimal128(precio != null ? precio : BigDecimal.ZERO));
            cambios.append("stock", producto.getStock());

            if (cambios.isEmpty()) return false; 
            
            Bson operacionUpdate = new Document("$set", cambios);
            UpdateResult resultado = obtenerColeccion().updateOne(filtro, operacionUpdate);
            return resultado.getModifiedCount() > 0;
        } catch (MongoException e) {
            throw new SQLException("Mongo DAO: Error al actualizar. " + e.getMessage(), e);
        }
    }

    @Override
    public boolean eliminar(int codigo) throws SQLException {
        if (codigo <= 0) throw new IllegalArgumentException("Código inválido para eliminar.");
        try {
            DeleteResult resultado = obtenerColeccion().deleteOne(Filters.eq("codigo", codigo));
            return resultado.getDeletedCount() > 0;
        } catch (MongoException e) {
            throw new SQLException("Mongo DAO: Error al eliminar. " + e.getMessage(), e);
        }
    }
}