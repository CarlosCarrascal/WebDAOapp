package com.product.controller;

import com.product.dao.ProductoDAO;
import com.product.dao.impl.ProductoMySqlDAO;
import com.product.dao.impl.ProductoMongoDAO;
import com.product.dao.impl.ProductoPostgresDAO;
import com.product.dao.impl.ProductoExcelDAO;
import com.product.model.Producto;
import com.product.util.MySqlConexion;
import com.product.util.MongoConexion;
import com.product.util.PostgresConexion;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.Comparator;
import java.util.stream.Collectors;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(name = "ProductoServlet", urlPatterns = {"/ProductoServlet"})
public class ProductoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private Map<String, ProductoDAO> daosDisponibles;
    private List<ProductoDAO> daosLecturaConfigurados;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        daosDisponibles = new HashMap<>();
        daosLecturaConfigurados = new ArrayList<>();

        try {
            ProductoDAO mysqlDao = new ProductoMySqlDAO();
            MySqlConexion.getConnection();
            daosDisponibles.put("mysql", mysqlDao);

            ProductoDAO mongoDao = new ProductoMongoDAO();
            MongoConexion.getDatabase();
            daosDisponibles.put("mongo", mongoDao);

            ProductoDAO postgresDao = new ProductoPostgresDAO();
            PostgresConexion.getConnection();
            daosDisponibles.put("postgres", postgresDao);
            
            ProductoDAO excelDao = new ProductoExcelDAO();
            daosDisponibles.put("excel", excelDao);

            Properties props = new Properties();
            InputStream input = null;
            ServletContext context = config.getServletContext();
            String propertiesFilePath = "/WEB-INF/classes/config.properties";
            Set<String> readTargetsConfig = new HashSet<>();

            try {
                input = context.getResourceAsStream(propertiesFilePath);
                 if (input == null) input = getClass().getClassLoader().getResourceAsStream("config.properties");

                if (input != null) {
                    props.load(input);
                    String readDbString = props.getProperty("read.databases", "mysql,mongo,postgres,excel").trim().toLowerCase();
                    readTargetsConfig.addAll(Arrays.asList(readDbString.split("\\s*,\\s*")));
                } else {
                    System.err.println("WARN: config.properties no encontrado. Usando defaults para lectura: mysql,mongo,postgres,excel");
                    readTargetsConfig.addAll(Arrays.asList("mysql", "mongo", "postgres", "excel"));
                }
            } catch (IOException ex) {
                 System.err.println("ERROR: Leyendo config.properties. Usando defaults para lectura. " + ex.getMessage());
                 readTargetsConfig.addAll(Arrays.asList("mysql", "mongo", "postgres", "excel"));
            } finally {
                if (input != null) try { input.close(); } catch (IOException e) { /* ignore */ }
            }

            for (String target : readTargetsConfig) {
                if (daosDisponibles.containsKey(target)) {
                    daosLecturaConfigurados.add(daosDisponibles.get(target));
                    System.out.println("INFO: DAO de lectura configurado globalmente: " + target);
                } else {
                     System.err.println("WARN: Base de datos de lectura '" + target + "' configurada pero no disponible/implementada.");
                }
            }

            if (daosDisponibles.isEmpty()) throw new ServletException("No se configuró ningún DAO disponible.");
            if (daosLecturaConfigurados.isEmpty()) throw new ServletException("No se configuró DB válida para lectura global.");

            System.out.println("INFO: DAOs disponibles inicializados: " + String.join(", ", daosDisponibles.keySet()));
            System.out.println("INFO: DAOs lectura global configurados: " + readTargetsConfig);

        } catch (Exception e) {
             throw new ServletException("No se pudo inicializar DAOs o conexiones", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) action = "listar";
        try {
            switch (action) {
                case "buscar": buscarProductosConfigurable(request, response); break;
                case "eliminar": eliminarProductoSeleccionado(request, response); break; 
                case "mostrarEditar": mostrarFormularioEditarConfigurable(request, response); break;
                case "listar": default: listarProductosConfigurable(request, response); break;
            }
        } catch (SQLException e) { handleError(request, response, "Error DB", e);
        } catch (NumberFormatException e) { handleError(request, response, "Error formato numérico", e);
        } catch (Exception e) { handleError(request, response, "Error inesperado", e); }
    }

     @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8"); 
        String action = request.getParameter("action");
        String targetRedirect = request.getContextPath() + "/ProductoServlet?action=listar";
        try {
            if ("insertar".equals(action)) { insertarProductoEnSeleccionadas(request, response); return; }
            if ("actualizar".equals(action)) { actualizarProductoEnOriginales(request, response); return; }
            response.sendRedirect(targetRedirect);
        } catch (SQLException e) { handleError(request, response, "Error DB", e);
        } catch (IllegalArgumentException e) {
             request.setAttribute( ("insertar".equals(action) ? "errorInsertar" : "errorActualizar"), e.getMessage());
             guardarDatosFormularioEnRequest(request, "actualizar".equals(action));
             recargarProductoParaEdicionOnError(request, action);
             listarProductosYMostrarVista(request, response);
        } catch (Exception e) { handleError(request, response, "Error inesperado", e); }
    }

    private void recargarProductoParaEdicionOnError(HttpServletRequest request, String action) {
         if ("actualizar".equals(action)) {
              try {
                 int codigoError = Integer.parseInt(request.getParameter("codigo"));
                 String sourceError = request.getParameter("source");
                 if (codigoError > 0 && sourceError != null && !sourceError.isEmpty()) {
                    Producto prodError = obtenerProductoEspecifico(codigoError, sourceError, request);
                    if(prodError != null) {
                        request.setAttribute("productoEditar", prodError);
                    }
                 } else if (codigoError > 0) {
                     Producto prodError = obtenerProductoConsolidadoParaEdicion(codigoError, request);
                     if(prodError != null) request.setAttribute("productoEditar", prodError);
                 }
             } catch(Exception innerEx) { 
                 System.err.println("Error menor al intentar recargar producto para edición: " + innerEx.getMessage());
             }
         }
    }

    private List<Producto> listarProductosConfigurable(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        List<Producto> listaProductos = obtenerProductosDesdeConfiguradas(dao -> dao.listarTodos());
        request.setAttribute("listaProductos", listaProductos);
        request.setAttribute("daosDisponibles", daosDisponibles.keySet());
        mostrarVistaPrincipal(request, response);
        return listaProductos;
    }

     private List<Producto> buscarProductosConfigurable(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        String nombreBusqueda = request.getParameter("nombreBusqueda");
        List<Producto> listaProductos;
        if (nombreBusqueda == null || nombreBusqueda.trim().isEmpty()){
             return listarProductosConfigurable(request, response);
        }
        listaProductos = obtenerProductosDesdeConfiguradas(dao -> dao.buscarPorNombre(nombreBusqueda));
        request.setAttribute("nombreBusquedaActual", nombreBusqueda);
        request.setAttribute("listaProductos", listaProductos);
        request.setAttribute("daosDisponibles", daosDisponibles.keySet());
        mostrarVistaPrincipal(request, response);
        return listaProductos;
    }

     private void mostrarFormularioEditarConfigurable(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException, NumberFormatException {
        int codigo = Integer.parseInt(request.getParameter("codigo"));
        String source = request.getParameter("source");

        if (source == null || source.trim().isEmpty()) {
            System.err.println("WARN: No se proporcionó 'source' para editar producto código: " + codigo + ". Intentando carga consolidada.");
            Producto productoConsolidado = obtenerProductoConsolidadoParaEdicion(codigo, request);
            if (productoConsolidado != null) {
                request.setAttribute("productoEditar", productoConsolidado);
                listarProductosYMostrarVista(request, response);
            } else {
                HttpSession session = request.getSession();
                session.setAttribute("mensajeErrorFlash", "No se encontró producto " + codigo + " para editar (sin fuente especificada).");
                response.sendRedirect(request.getContextPath() + "/ProductoServlet?action=listar");
            }
            return;
        }
        
        Producto productoExistente = obtenerProductoEspecifico(codigo, source, request);

        if (productoExistente != null) {
            request.setAttribute("productoEditar", productoExistente);
            listarProductosYMostrarVista(request, response);
        } else {
             HttpSession session = request.getSession();
             session.setAttribute("mensajeErrorFlash", "No se encontró producto " + codigo + " de la fuente '" + source + "' para editar.");
             response.sendRedirect(request.getContextPath() + "/ProductoServlet?action=listar");
        }
    }

    private Producto obtenerProductoEspecifico(int codigo, String sourceDaoKey, HttpServletRequest request) throws SQLException {
        ProductoDAO daoFuente = daosDisponibles.get(sourceDaoKey.trim().toLowerCase());
        if (daoFuente == null) {
            System.err.println("WARN: DAO para la fuente especificada '" + sourceDaoKey + "' no encontrado.");
            return null;
        }
        Producto productoBase = null;
        try {
            productoBase = daoFuente.obtenerPorCodigo(codigo);
        } catch (Exception e) {
            System.err.println("WARN: Error al obtener producto código " + codigo + " desde " + sourceDaoKey + ": " + e.getMessage());
            return null;
        }
        if (productoBase != null) {
            productoBase.getStoredIn().clear();
            productoBase.addStorageLocation(sourceDaoKey);
            for (Map.Entry<String, ProductoDAO> entry : daosDisponibles.entrySet()) {
                String otraDaoKey = entry.getKey();
                if (otraDaoKey.equals(sourceDaoKey)) continue;
                ProductoDAO otroDao = entry.getValue();
                try {
                    Producto pOtro = otroDao.obtenerPorCodigo(codigo);
                    if (pOtro != null &&
                        pOtro.getNombre().equals(productoBase.getNombre()) &&
                        (pOtro.getPrecio() == null ? productoBase.getPrecio() == null : pOtro.getPrecio().compareTo(productoBase.getPrecio()) == 0)
                       ) {
                        productoBase.addStorageLocation(otraDaoKey);
                    }
                } catch (Exception e) { /* Ignorar */ }
            }
        }
        return productoBase;
    }
    
    private Producto obtenerProductoConsolidadoParaEdicion(int codigo, HttpServletRequest request) throws SQLException {
        Producto productoConsolidado = null;
        List<String> fuentesDondeSeEncontro = new ArrayList<>();
        for (Map.Entry<String, ProductoDAO> entry : daosDisponibles.entrySet()) {
            ProductoDAO dao = entry.getValue();
            String daoKey = entry.getKey();
            try {
                Producto productoParcial = dao.obtenerPorCodigo(codigo);
                if (productoParcial != null) {
                    if (productoConsolidado == null) {
                        productoConsolidado = productoParcial;
                    }
                    fuentesDondeSeEncontro.add(daoKey);
                }
            } catch (Exception e) {  }
        }
        if (productoConsolidado != null) {
            productoConsolidado.setStoredIn(fuentesDondeSeEncontro);
        }
        return productoConsolidado;
    }

    @FunctionalInterface
    interface DaoListOperation { List<Producto> execute(ProductoDAO dao) throws SQLException; }

    private List<Producto> obtenerProductosDesdeConfiguradas(DaoListOperation operation) throws SQLException {
         List<Producto> todosLosProductosResultantes = new ArrayList<>();
         for(ProductoDAO dao : daosLecturaConfigurados) {
             String daoKey = daosDisponibles.entrySet().stream()
                                .filter(entry -> entry.getValue().equals(dao))
                                .map(Map.Entry::getKey)
                                .findFirst().orElse("fuente_desconocida");
             try {
                 List<Producto> productosDeEsteDao = operation.execute(dao);
                 for (Producto p : productosDeEsteDao) {
                     if (p != null && p.getCodigo() > 0) {
                         p.getStoredIn().clear();
                         p.addStorageLocation(daoKey);
                         todosLosProductosResultantes.add(p);
                     }
                 }
             } catch (Exception e) { 
                 System.err.println("WARN: Error al leer en " + daoKey + " (" + dao.getClass().getSimpleName() + "): " + e.getMessage()); 
             }
         }
         todosLosProductosResultantes.sort(Comparator.comparing(Producto::getCodigo)
                                           .thenComparing(Producto::getNombre, Comparator.nullsLast(String::compareToIgnoreCase)));
         return todosLosProductosResultantes;
    }

    private void insertarProductoEnSeleccionadas(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException, NumberFormatException, IllegalArgumentException {
        String nombre = request.getParameter("nombre");
        String precioStr = request.getParameter("precio");
        String stockStr = request.getParameter("stock");
        String[] targetDbKeys = request.getParameterValues("targetDatabases");

        if (nombre == null || nombre.trim().isEmpty() || precioStr == null || precioStr.trim().isEmpty() || stockStr == null || stockStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Todos los campos (nombre, precio, stock) son obligatorios.");
        }
        if (targetDbKeys == null || targetDbKeys.length == 0) {
            throw new IllegalArgumentException("Debe seleccionar al menos una base de datos destino para la inserción.");
        }

        BigDecimal precio = new BigDecimal(precioStr.replace(",", "."));
        int stock = Integer.parseInt(stockStr);
        if (precio.compareTo(BigDecimal.ZERO) < 0 || stock < 0) {
             throw new IllegalArgumentException("Precio y stock no pueden ser negativos.");
        }
        
        Producto nuevoProductoBase = new Producto(nombre, precio, stock);
        Map<String, Boolean> resultados = new HashMap<>();
        String errorMsgAcumulado = "";
        int codigoGeneradoPrincipal = 0; 

        for (String dbKey : targetDbKeys) {
            ProductoDAO dao = daosDisponibles.get(dbKey.trim().toLowerCase());
            if (dao == null) { 
                resultados.put(dbKey, false);
                errorMsgAcumulado += "DAO para '" + dbKey + "' no encontrado; ";
                continue; 
            }

            String daoNameFriendly = dbKey.substring(0, 1).toUpperCase() + dbKey.substring(1);
            boolean exitoEnEsteDao = false;
            Producto productoParaEsteDao = new Producto(nombre, precio, stock); 

            try {
                if (codigoGeneradoPrincipal > 0) {
                    productoParaEsteDao.setCodigo(codigoGeneradoPrincipal);
                }
                dao.insertar(productoParaEsteDao); 
                if (productoParaEsteDao.getCodigo() > 0 && codigoGeneradoPrincipal == 0 && 
                    (dao instanceof ProductoMySqlDAO || dao instanceof ProductoPostgresDAO)) {
                    codigoGeneradoPrincipal = productoParaEsteDao.getCodigo();
                }
                else if (productoParaEsteDao.getCodigo() > 0 && codigoGeneradoPrincipal == 0 && 
                         (dao instanceof ProductoMongoDAO || dao instanceof ProductoExcelDAO)) {
                    codigoGeneradoPrincipal = productoParaEsteDao.getCodigo();
                }
                exitoEnEsteDao = true;
            } catch (Exception e) { 
                errorMsgAcumulado += daoNameFriendly + ": " + e.getMessage() + "; ";
                System.err.println("Error al insertar en " + daoNameFriendly + ": " + e.getMessage());
            }
            resultados.put(daoNameFriendly, exitoEnEsteDao);
        }
        
        if (codigoGeneradoPrincipal > 0) nuevoProductoBase.setCodigo(codigoGeneradoPrincipal);
        
        HttpSession session = request.getSession();
        long exitosCount = resultados.values().stream().filter(b -> b).count();

        if (exitosCount == targetDbKeys.length) {
            session.setAttribute("mensajeExito", "Producto '" + nuevoProductoBase.getNombre() + "' (Cód: " + nuevoProductoBase.getCodigo() + ") registrado exitosamente en: " + String.join(", ", resultados.keySet().stream().filter(k -> resultados.get(k)).collect(Collectors.toList())) + ".");
        } else if (exitosCount > 0) {
            session.setAttribute("mensajeAdvertencia", "Producto '" + nuevoProductoBase.getNombre() + "' registrado parcialmente. Éxitos: " + String.join(", ", resultados.keySet().stream().filter(k -> resultados.get(k)).collect(Collectors.toList())) + ". Fallos: " + errorMsgAcumulado);
        } else {
            session.setAttribute("mensajeErrorFlash", "Error total al registrar el producto '" + nuevoProductoBase.getNombre() + "'. Detalles: " + errorMsgAcumulado);
        }
        response.sendRedirect(request.getContextPath() + "/ProductoServlet?action=listar");
    }

    private void actualizarProductoEnOriginales(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException, NumberFormatException, IllegalArgumentException {
        int codigo = Integer.parseInt(request.getParameter("codigo"));
        String nombre = request.getParameter("nombre");
        String precioStr = request.getParameter("precio");
        String stockStr = request.getParameter("stock");
        String[] storedInOriginalKeys = request.getParameterValues("storedInOriginal"); 

        if (nombre == null || nombre.trim().isEmpty() || precioStr == null || precioStr.trim().isEmpty() || stockStr == null || stockStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Todos los campos (nombre, precio, stock) son obligatorios.");
        }
        if (storedInOriginalKeys == null || storedInOriginalKeys.length == 0) {
            throw new IllegalArgumentException("No se especificaron las bases de datos originales donde actualizar el producto.");
        }

        BigDecimal precio = new BigDecimal(precioStr.replace(",", "."));
        int stock = Integer.parseInt(stockStr);
        if (precio.compareTo(BigDecimal.ZERO) < 0 || stock < 0) {
             throw new IllegalArgumentException("Precio y stock no pueden ser negativos.");
        }
        
        Producto productoActualizado = new Producto(codigo, nombre, precio, stock);
        Map<String, Boolean> resultados = new HashMap<>();
        String errorMsgAcumulado = "";

        for (String dbKey : storedInOriginalKeys) {
            ProductoDAO dao = daosDisponibles.get(dbKey.trim().toLowerCase());
            if (dao == null) { 
                resultados.put(dbKey, false);
                errorMsgAcumulado += "DAO para '" + dbKey + "' no encontrado; ";
                continue; 
            }
            String daoNameFriendly = dbKey.substring(0, 1).toUpperCase() + dbKey.substring(1);
            boolean exitoEnEsteDao = false;
            try { 
                exitoEnEsteDao = dao.actualizar(productoActualizado);
            } catch (Exception e) { 
                errorMsgAcumulado += daoNameFriendly + ":" + e.getMessage() + "; ";
                 System.err.println("Error al actualizar en " + daoNameFriendly + ": " + e.getMessage());
            }
            resultados.put(daoNameFriendly, exitoEnEsteDao);
        }
        
        HttpSession session = request.getSession();
        long exitosCount = resultados.values().stream().filter(b -> b).count();

        if (exitosCount == storedInOriginalKeys.length) {
            session.setAttribute("mensajeExito", "Producto '" + productoActualizado.getNombre() + "' (Cód: " + codigo + ") actualizado exitosamente en: " + String.join(", ", resultados.keySet()) + ".");
        } else if (exitosCount > 0) {
             session.setAttribute("mensajeAdvertencia", "Producto '" + productoActualizado.getNombre() + "' (Cód: " + codigo + ") actualizado parcialmente. Éxitos en: " + String.join(", ", resultados.keySet().stream().filter(k -> resultados.get(k)).collect(Collectors.toList())) + ". Fallos: " + errorMsgAcumulado);
        } else {
            session.setAttribute("mensajeErrorFlash", "Error total al actualizar el producto " + codigo + ". Detalles: " + errorMsgAcumulado);
        }
        response.sendRedirect(request.getContextPath() + "/ProductoServlet?action=listar");
    }

    private void eliminarProductoSeleccionado(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException, NumberFormatException {
        int codigo = Integer.parseInt(request.getParameter("codigo"));
        String source = request.getParameter("source");
        HttpSession session = request.getSession();

        if (source == null || source.trim().isEmpty()) {
            session.setAttribute("mensajeErrorFlash", "Fuente no especificada para eliminar el producto " + codigo + ". Operación cancelada.");
            response.sendRedirect(request.getContextPath() + "/ProductoServlet?action=listar");
            return;
        }

        Producto productoAEliminar = obtenerProductoEspecifico(codigo, source, request);
        
        if (productoAEliminar == null) {
            session.setAttribute("mensajeErrorFlash", "No se encontró el producto código " + codigo + " de la fuente '" + source + "' para eliminar.");
            response.sendRedirect(request.getContextPath() + "/ProductoServlet?action=listar");
            return;
        }

        List<String> dbsAEliminarDe = new ArrayList<>(productoAEliminar.getStoredIn());
        Map<String, Boolean> resultados = new HashMap<>();
        String errorMsg = "";
        String nombreProductoEliminado = productoAEliminar.getNombre(); // Guardar nombre para mensaje

        if (dbsAEliminarDe.isEmpty()) {
            dbsAEliminarDe.add(source);
        }

        for (String dbKey : dbsAEliminarDe) {
             ProductoDAO dao = daosDisponibles.get(dbKey.trim().toLowerCase());
             if (dao == null) { 
                resultados.put(dbKey, false);
                errorMsg += "DAO para '" + dbKey + "' no encontrado; ";
                continue; 
             }
             String daoNameFriendly = dbKey.substring(0, 1).toUpperCase() + dbKey.substring(1);
             boolean ok = false;
             try { 
                 ok = dao.eliminar(codigo);
             } catch (Exception e) { 
                 errorMsg += daoNameFriendly + ":" + e.getMessage() + "; ";
                 System.err.println("Error al eliminar en " + daoNameFriendly + ": " + e.getMessage());
             }
             resultados.put(daoNameFriendly, ok);
        }
        
        long exitosCount = resultados.values().stream().filter(b -> b).count();

        if (exitosCount == dbsAEliminarDe.size() && !dbsAEliminarDe.isEmpty()) {
            session.setAttribute("mensajeExito", "Producto '" + nombreProductoEliminado + "' (Cód: " + codigo + ") eliminado exitosamente de: " + String.join(", ", resultados.keySet()) + ".");
        } else if (exitosCount > 0) {
            session.setAttribute("mensajeAdvertencia", "Producto '" + nombreProductoEliminado + "' (Cód: " + codigo + ") eliminado parcialmente. Éxitos en: " + String.join(", ", resultados.keySet().stream().filter(k -> resultados.get(k)).collect(Collectors.toList())) + ". Fallos: " + errorMsg);
        } else if (!dbsAEliminarDe.isEmpty()){
            session.setAttribute("mensajeErrorFlash", "Error total al eliminar producto '" + nombreProductoEliminado + "' (Cód: " + codigo + "). Detalles: " + errorMsg);
        }
        response.sendRedirect(request.getContextPath() + "/ProductoServlet?action=listar");
    }

     private void listarProductosYMostrarVista(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            if (request.getAttribute("productoEditar") == null && (request.getAttribute("errorActualizar") != null || request.getAttribute("errorInsertar") != null) ) {
                 if ("actualizar".equals(request.getParameter("action"))) {
                    recargarProductoParaEdicionOnError(request, request.getParameter("action"));
                 }
            }
            List<Producto> listaProductos = obtenerProductosDesdeConfiguradas(dao -> dao.listarTodos());
            request.setAttribute("listaProductos", listaProductos);
        } catch (Exception e) {
            System.err.println("Error al listar productos para la vista: " + e.getMessage());
            request.setAttribute("errorGeneral", "No se pudo recargar lista de productos.");
            request.setAttribute("listaProductos", new ArrayList<>());
        }
        request.setAttribute("daosDisponibles", daosDisponibles.keySet());
        mostrarVistaPrincipal(request, response);
    }

     private void mostrarVistaPrincipal(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/productos.jsp");
        dispatcher.forward(request, response);
    }

     private void handleError(HttpServletRequest request, HttpServletResponse response, String mensaje, Exception excepcion)
            throws ServletException, IOException {
        System.err.println("Error Servlet: " + mensaje + " -> " + excepcion.getMessage() + (excepcion.getCause() != null ? " Causa: " + excepcion.getCause().getMessage() : ""));
        excepcion.printStackTrace();
        request.setAttribute("mensajeErrorCritico", mensaje + " (" + excepcion.getClass().getSimpleName() + ")");
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/error.jsp");
        dispatcher.forward(request, response);
    }

     private void guardarDatosFormularioEnRequest(HttpServletRequest request, boolean incluirCodigo) {
         if (incluirCodigo) {
            request.setAttribute("formCodigo", request.getParameter("codigo"));
            if (request.getParameter("source") != null) {
                request.setAttribute("formSource", request.getParameter("source"));
            }
         }
        request.setAttribute("formNombre", request.getParameter("nombre"));
        request.setAttribute("formPrecio", request.getParameter("precio"));
        request.setAttribute("formStock", request.getParameter("stock"));
        if (request.getParameterValues("targetDatabases") != null) {
            request.setAttribute("formTargetDatabases", Arrays.asList(request.getParameterValues("targetDatabases")));
        }
        if (request.getParameterValues("storedInOriginal") != null) {
            request.setAttribute("formStoredInOriginal", Arrays.asList(request.getParameterValues("storedInOriginal")));
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        MongoConexion.closeConnection();
        MySqlConexion.closeConnection();
        PostgresConexion.closeConnection();
        System.out.println("ProductoServlet destruido, conexiones cerradas.");
    }
}
