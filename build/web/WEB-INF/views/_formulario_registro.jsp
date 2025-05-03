<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<div class="bg-gray-800 shadow-lg rounded-xl border border-gray-700 overflow-hidden">
    <div class="px-6 py-4 bg-gray-700/50 border-b border-gray-700">
        <h3 class="text-lg font-semibold text-white flex items-center">
            <c:choose>
                <c:when test="${not empty productoEditar}">
                     <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-2 text-orange-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"> <path stroke-linecap="round" stroke-linejoin="round" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" /> </svg>
                    Editar Producto (ID: ${productoEditar.codigo})
                </c:when>
                <c:otherwise>
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-2 text-indigo-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"> <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v3m0 0v3m0-3h3m-3 0H9m12 0a9 9 0 11-18 0 9 9 0 0118 0z" /> </svg>
                    Registrar Nuevo Producto
                </c:otherwise>
            </c:choose>
        </h3>
    </div>
    <div class="p-8">
        <c:if test="${not empty errorInsertar or not empty errorActualizar}">
            <div class="mb-4 bg-yellow-900/30 border border-yellow-600 text-yellow-300 px-4 py-3 rounded-md flex items-start" role="alert">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2 text-yellow-400 flex-shrink-0" viewBox="0 0 20 20" fill="currentColor"> <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.21 3.03-1.742 3.03H4.42c-1.532 0-2.493-1.696-1.742-3.03l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd" /> </svg>
                <span class="block sm:inline"><c:out value="${not empty errorInsertar ? errorInsertar : errorActualizar}" /></span>
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/ProductoServlet" method="post" id="formProducto">
             <input type="hidden" name="action" value="${not empty productoEditar ? 'actualizar' : 'insertar'}">

             <div class="space-y-6">
                <div>
                    <label for="codigo" class="block text-sm font-medium text-gray-300 mb-1">Código:</label>
                    <c:if test="${not empty productoEditar}">
                         <input type="hidden" name="codigo" value="${productoEditar.codigo}">
                    </c:if>
                    <input type="text" id="codigo" name="codigo_display" readonly
                           class="block w-full rounded-md border-gray-600 bg-gray-600 text-gray-400 shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 transition duration-150 ease-in-out px-3 py-2 text-base cursor-not-allowed"
                           value="${not empty productoEditar ? productoEditar.codigo : '[Automático]'}">
                </div>

                <div>
                    <label for="nombre" class="block text-sm font-medium text-gray-300 mb-1">Nombre:</label>
                    <input type="text" id="nombre" name="nombre" required value="${not empty productoEditar ? productoEditar.nombre : (not empty formNombre ? formNombre : '')}"
                           class="block w-full rounded-md border-gray-600 bg-gray-700 text-white shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 transition duration-150 ease-in-out hover:bg-gray-600/60 px-3 py-2 text-base">
                </div>

                 <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
                     <div>
                        <label for="precio" class="block text-sm font-medium text-gray-300 mb-1">Precio:</label>
                        <input type="number" id="precio" name="precio" step="0.01" min="0" required value="${not empty productoEditar ? productoEditar.precio : (not empty formPrecio ? formPrecio : '')}"
                               class="block w-full rounded-md border-gray-600 bg-gray-700 text-white shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 transition duration-150 ease-in-out hover:bg-gray-600/60 px-3 py-2 text-base">
                     </div>
                     <div>
                        <label for="stock" class="block text-sm font-medium text-gray-300 mb-1">Stock:</label>
                        <input type="number" id="stock" name="stock" min="0" required value="${not empty productoEditar ? productoEditar.stock : (not empty formStock ? formStock : '')}"
                               class="block w-full rounded-md border-gray-600 bg-gray-700 text-white shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 transition duration-150 ease-in-out hover:bg-gray-600/60 px-3 py-2 text-base">
                     </div>
                </div>

                <div class="grid grid-cols-2 gap-4 items-center">
                     <div class="flex justify-end">
                         <c:choose>
                             <c:when test="${not empty productoEditar}">
                                <a href="${pageContext.request.contextPath}/ProductoServlet?action=listar" class="whitespace-nowrap inline-flex justify-center py-2 px-4 border border-gray-600 shadow-sm text-sm font-medium rounded-md text-gray-300 bg-gray-700 hover:bg-gray-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 focus:ring-offset-gray-800 transition duration-150 ease-in-out">
                                    Cancelar
                                </a>
                             </c:when>
                             <c:otherwise>
                                 <button type="reset" class="whitespace-nowrap inline-flex justify-center py-2 px-4 border border-gray-600 shadow-sm text-sm font-medium rounded-md text-gray-300 bg-gray-700 hover:bg-gray-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 focus:ring-offset-gray-800 transition duration-150 ease-in-out">
                                      Limpiar
                                 </button>
                             </c:otherwise>
                         </c:choose>
                     </div>

                     <div>
                         <button type="submit" class="w-full whitespace-nowrap inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 focus:ring-offset-gray-800 transition duration-150 ease-in-out">
                               <c:choose>
                                    <c:when test="${not empty productoEditar}">
                                        <i class="bi bi-save-fill me-1"></i> Actualizar
                                    </c:when>
                                    <c:otherwise>
                                       <i class="bi bi-check-circle me-1"></i> Insertar
                                    </c:otherwise>
                               </c:choose>
                         </button>
                     </div>
                </div>

             </div>
        </form>
    </div>
</div>