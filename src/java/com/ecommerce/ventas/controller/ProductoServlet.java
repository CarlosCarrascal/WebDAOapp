package com.ecommerce.ventas.controller;


import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.ecommerce.ventas.dao.ProductoDAO;
import com.ecommerce.ventas.dao.ProductoDAOImpl;
import com.ecommerce.ventas.model.Producto;
import com.ecommerce.ventas.util.DatabaseConnection;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

@WebServlet(name = "ProductoServlet", urlPatterns = {"/ProductoServlet"})
public class ProductoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private ProductoDAO productoDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        this.productoDAO = new ProductoDAOImpl();
        System.out.println("ProductoServlet inicializado.");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            action = "listar";
        }
        System.out.println("Acción GET: " + action);

        try {
            switch (action) {
                case "buscar":
                    buscarProductos(request, response);
                    break;
                case "eliminar":
                    eliminarProducto(request, response);
                    break;
                case "mostrarEditar":
                    mostrarFormularioEditar(request, response);
                    break;
                case "listar":
                default:
                    listarProductos(request, response);
                    break;
            }
        } catch (SQLException e) {
            handleError(request, response, "Error de Base de Datos procesando la acción '" + action + "'.", e);
        } catch (NumberFormatException e) {
             handleError(request, response, "Error procesando acción '" + action + "': El código del producto debe ser numérico.", e);
        }
         catch (Exception e) {
            handleError(request, response, "Error inesperado procesando la acción '" + action + "'.", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        System.out.println("Acción POST: " + action);

        if ("insertar".equals(action)) {
             try {
                insertarProducto(request, response);
            } catch (SQLException e) {
                handleError(request, response, "Error de Base de Datos al registrar el producto.", e);
            } catch (IllegalArgumentException e) { 
                 System.err.println("Error de validación/formato en doPost (insertar): " + e.getMessage());
                 guardarDatosFormularioEnRequest(request, false);
                 request.setAttribute("errorInsertar", e.getMessage());
                 listarProductosYMostrarVista(request, response);
            } catch (Exception e) {
                handleError(request, response, "Error inesperado al registrar el producto.", e);
            }

        } else if ("actualizar".equals(action)) {
             try {
                actualizarProducto(request, response);
            } catch (SQLException e) {
                handleError(request, response, "Error de Base de Datos al actualizar el producto.", e);

            } catch (IllegalArgumentException e) { 
                 System.err.println("Error de validación/formato en doPost (actualizar): " + e.getMessage());
                 guardarDatosFormularioEnRequest(request, true);
                 request.setAttribute("errorActualizar", e.getMessage());
                 
                  try {
                     int codigoError = Integer.parseInt(request.getParameter("codigo"));
                     Producto prodError = productoDAO.obtenerPorCodigo(codigoError);
                     if(prodError != null) request.setAttribute("productoEditar", prodError);
                 } catch(Exception innerEx) {
                     System.err.println("No se pudo recargar el producto para editar tras error de actualización.");
                 }
                 listarProductosYMostrarVista(request, response);

            } catch (Exception e) {
                handleError(request, response, "Error inesperado al actualizar el producto.", e);
            }
        } else {
            response.sendRedirect(request.getContextPath() + "/ProductoServlet");
        }
    }

    // Métodos privados

    private void mostrarFormularioEditar(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException, NumberFormatException {
        System.out.println("Ejecutando mostrarFormularioEditar...");
        int codigo = Integer.parseInt(request.getParameter("codigo"));
        Producto productoExistente = productoDAO.obtenerPorCodigo(codigo);

        if (productoExistente != null) {
            request.setAttribute("productoEditar", productoExistente);
            System.out.println("Producto encontrado para editar: " + productoExistente);
             listarProductosYMostrarVista(request, response);
        } else {
             System.err.println("No se encontró producto para editar con código: " + codigo);
             HttpSession session = request.getSession();
             session.setAttribute("mensajeErrorFlash", "No se encontró el producto con código " + codigo + " para editar.");
             response.sendRedirect(request.getContextPath() + "/ProductoServlet?action=listar");
        }
    }

    private void actualizarProducto(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException, NumberFormatException, IllegalArgumentException {
        System.out.println("Ejecutando actualizarProducto...");
        int codigo = Integer.parseInt(request.getParameter("codigo"));
        String nombre = request.getParameter("nombre");
        String precioStr = request.getParameter("precio");
        String stockStr = request.getParameter("stock");

        if (nombre == null || nombre.trim().isEmpty() ||
            precioStr == null || precioStr.trim().isEmpty() ||
            stockStr == null || stockStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Todos los campos (nombre, precio, stock) son obligatorios.");
        }
        BigDecimal precio = new BigDecimal(precioStr.replace(",", "."));
        int stock = Integer.parseInt(stockStr);

        if (precio.compareTo(BigDecimal.ZERO) < 0 || stock < 0) {
             throw new IllegalArgumentException("El precio y el stock no pueden ser negativos.");
        }
        Producto productoActualizado = new Producto(codigo, nombre, precio, stock);
        System.out.println("Producto a actualizar: " + productoActualizado);
        boolean actualizado = productoDAO.actualizar(productoActualizado);
        HttpSession session = request.getSession();
        if (actualizado) {
            session.setAttribute("mensajeExito", "¡Producto '" + nombre + "' (Cod: " + codigo + ") actualizado exitosamente!");
            System.out.println("Producto actualizado: " + codigo);
        } else {
             session.setAttribute("mensajeErrorFlash", "No se pudo actualizar el producto con código " + codigo + ".");
             System.err.println("No se pudo actualizar el producto: " + codigo);
        }
        response.sendRedirect(request.getContextPath() + "/ProductoServlet?action=listar");
    }

     private void guardarDatosFormularioEnRequest(HttpServletRequest request, boolean incluirCodigo) {
         if (incluirCodigo) {
             request.setAttribute("formCodigo", request.getParameter("codigo"));
         }
        request.setAttribute("formNombre", request.getParameter("nombre"));
        request.setAttribute("formPrecio", request.getParameter("precio"));
        request.setAttribute("formStock", request.getParameter("stock"));
    }

     private void insertarProducto(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException, NumberFormatException, IllegalArgumentException {
        System.out.println("Ejecutando insertarProducto (autoincrement)...");
        String nombre = request.getParameter("nombre");
        String precioStr = request.getParameter("precio");
        String stockStr = request.getParameter("stock");

        if (nombre == null || nombre.trim().isEmpty() ||
            precioStr == null || precioStr.trim().isEmpty() ||
            stockStr == null || stockStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Todos los campos (nombre, precio, stock) son obligatorios.");
        }
        BigDecimal precio = new BigDecimal(precioStr.replace(",", "."));
        int stock = Integer.parseInt(stockStr);

        if (precio.compareTo(BigDecimal.ZERO) < 0 || stock < 0) {
             throw new IllegalArgumentException("El precio y el stock no pueden ser negativos.");
        }
        Producto nuevoProducto = new Producto(nombre, precio, stock);
        productoDAO.insertar(nuevoProducto);
        HttpSession session = request.getSession();
        String codigoGenerado = (nuevoProducto.getCodigo() > 0) ? String.valueOf(nuevoProducto.getCodigo()) : "?";
        session.setAttribute("mensajeExito", "¡Producto '" + nombre + "' (Cod: " + codigoGenerado + ") registrado exitosamente!");
        System.out.println("Producto insertado, redirigiendo...");
        response.sendRedirect(request.getContextPath() + "/ProductoServlet?action=listar");
    }

    private void eliminarProducto(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException, NumberFormatException {
        System.out.println("Ejecutando eliminarProducto...");
        int codigo = Integer.parseInt(request.getParameter("codigo"));
        boolean eliminado = productoDAO.eliminar(codigo);
        HttpSession session = request.getSession();
        if (eliminado) {
            session.setAttribute("mensajeExito", "Producto con código " + codigo + " eliminado exitosamente.");
            System.out.println("Producto eliminado: " + codigo);
        } else {
            session.setAttribute("mensajeErrorFlash", "No se pudo eliminar el producto con código " + codigo + " (puede que ya no exista).");
            System.err.println("No se pudo eliminar el producto: " + codigo);
        }
        response.sendRedirect(request.getContextPath() + "/ProductoServlet?action=listar");
    }

    private void listarProductos(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        System.out.println("Ejecutando listarProductos...");
        List<Producto> listaProductos = productoDAO.listarTodos();
        request.setAttribute("listaProductos", listaProductos);
        mostrarVistaPrincipal(request, response);
    }

     private void listarProductosYMostrarVista(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            System.out.println("Ejecutando listarProductosYMostrarVista...");
            if (request.getAttribute("productoEditar") == null && request.getAttribute("errorActualizar") != null) {
                 try {
                     int codigoError = Integer.parseInt(request.getParameter("codigo"));
                     Producto prodError = productoDAO.obtenerPorCodigo(codigoError);
                     if(prodError != null) request.setAttribute("productoEditar", prodError);
                 } catch(Exception innerEx) {
                     System.err.println("No se pudo recargar el producto para editar tras error de actualización.");
                 }
            }

            List<Producto> listaProductos = productoDAO.listarTodos();
            request.setAttribute("listaProductos", listaProductos);
        } catch (SQLException e) {
            System.err.println("Error al listar productos en listarProductosYMostrarVista: " + e.getMessage());
            request.setAttribute("errorGeneral", "No se pudo recargar la lista de productos.");
            request.setAttribute("listaProductos", new ArrayList<>());
        }
        mostrarVistaPrincipal(request, response);
    }

    private void buscarProductos(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        String nombreBusqueda = request.getParameter("nombreBusqueda");
        System.out.println("Ejecutando buscarProductos con nombre: " + nombreBusqueda);
        List<Producto> listaProductos;
        if (nombreBusqueda != null && !nombreBusqueda.trim().isEmpty()) {
            listaProductos = productoDAO.buscarPorNombre(nombreBusqueda);
            request.setAttribute("nombreBusquedaActual", nombreBusqueda);
        } else {
            listaProductos = productoDAO.listarTodos();
        }
        request.setAttribute("listaProductos", listaProductos);
        mostrarVistaPrincipal(request, response);
    }

     private void mostrarVistaPrincipal(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/productos.jsp");
        dispatcher.forward(request, response);
    }

    private void handleError(HttpServletRequest request, HttpServletResponse response, String mensaje, Exception excepcion)
            throws ServletException, IOException {
        System.err.println("Error en Servlet: " + mensaje);
        if (excepcion != null) {
            excepcion.printStackTrace();
        }
        request.setAttribute("mensajeError", mensaje + (excepcion != null ? " (" + excepcion.getClass().getSimpleName() + ")" : ""));
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/error.jsp");
        dispatcher.forward(request, response);
    }

    @Override
    public void destroy() {
        super.destroy();
        DatabaseConnection.closeConnection();
        System.out.println("ProductoServlet destruido y conexión cerrada.");
    }
}
