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
    <div class="p-6 md:p-8">
        <c:if test="${not empty requestScope.errorInsertar or not empty requestScope.errorActualizar}">
            <div class="mb-4 bg-red-800 border border-red-700 text-red-100 px-4 py-3 rounded-md relative flex items-start" role="alert">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2 text-red-300 flex-shrink-0" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd" /></svg>
                <span class="block sm:inline"><c:out value="${not empty requestScope.errorInsertar ? requestScope.errorInsertar : requestScope.errorActualizar}" /></span>
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/ProductoServlet" method="post" id="formProducto">
            <input type="hidden" name="action" value="${not empty productoEditar ? 'actualizar' : 'insertar'}">

            <div class="space-y-5">
                <div>
                    <label for="codigo_display" class="block text-sm font-medium text-gray-300 mb-1">Código:</label>
                    <c:if test="${not empty productoEditar}">
                        <input type="hidden" name="codigo" value="${productoEditar.codigo}">
                        <%-- Para la actualización, necesitamos saber dónde estaba almacenado originalmente --%>
                        <c:forEach var="dbKey" items="${productoEditar.storedIn}">
                            <input type="hidden" name="storedInOriginal" value="${dbKey}">
                        </c:forEach>
                    </c:if>
                    <input type="text" id="codigo_display" name="codigo_display" readonly
                           class="block w-full rounded-md border-gray-600 bg-gray-600 text-gray-400 shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 transition duration-150 ease-in-out px-3 py-2 text-base cursor-not-allowed"
                           value="${not empty productoEditar ? productoEditar.codigo : '[Automático]'}">
                </div>

                <div>
                    <label for="nombre" class="block text-sm font-medium text-gray-300 mb-1">Nombre:</label>
                    <input type="text" id="nombre" name="nombre" required value="${not empty productoEditar ? productoEditar.nombre : (not empty requestScope.formNombre ? requestScope.formNombre : '')}"
                           class="block w-full rounded-md border-gray-600 bg-gray-700 text-white shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 transition duration-150 ease-in-out hover:bg-gray-600/60 px-3 py-2 text-base">
                </div>

                <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
                    <div>
                        <label for="precio" class="block text-sm font-medium text-gray-300 mb-1">Precio (S/):</label>
                        <input type="number" id="precio" name="precio" step="0.01" min="0" required value="${not empty productoEditar ? productoEditar.precio : (not empty requestScope.formPrecio ? requestScope.formPrecio : '')}"
                               class="block w-full rounded-md border-gray-600 bg-gray-700 text-white shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 transition duration-150 ease-in-out hover:bg-gray-600/60 px-3 py-2 text-base">
                    </div>
                    <div>
                        <label for="stock" class="block text-sm font-medium text-gray-300 mb-1">Stock:</label>
                        <input type="number" id="stock" name="stock" min="0" required value="${not empty productoEditar ? productoEditar.stock : (not empty requestScope.formStock ? requestScope.formStock : '')}"
                               class="block w-full rounded-md border-gray-600 bg-gray-700 text-white shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 transition duration-150 ease-in-out hover:bg-gray-600/60 px-3 py-2 text-base">
                    </div>
                </div>

                <%-- Selección de Bases de Datos para Inserción (Checkboxes compactos) --%>
                <c:if test="${empty productoEditar}">
                    <div>
                        <label class="block text-sm font-medium text-gray-300 mb-2">Guardar en:</label>
                        <div class="grid grid-cols-2 gap-x-4 gap-y-2 p-3 bg-gray-700/30 rounded-md border border-gray-700">
                            <c:forEach var="daoKey" items="${daosDisponibles}">
                                <label for="db_${daoKey}" class="flex items-center text-gray-400 hover:text-gray-200 cursor-pointer p-1.5 rounded-md hover:bg-gray-600/50 transition-colors">
                                    <input type="checkbox" id="db_${daoKey}" name="targetDatabases" value="${daoKey}"
                                           <c:if test="${not empty requestScope.formTargetDatabases && requestScope.formTargetDatabases.contains(daoKey)}">checked</c:if>
                                           class="h-4 w-4 bg-gray-600 border-gray-500 rounded text-indigo-500 focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 focus:ring-offset-gray-800 mr-2 shrink-0">
                                    <span class="capitalize text-sm">${daoKey}</span>
                                </label>
                            </c:forEach>
                        </div>
                    </div>
                </c:if>

                <%-- Mostrar dónde está almacenado si se está editando --%>
                <c:if test="${not empty productoEditar && not empty productoEditar.storedIn}">
                     <div class="mt-1 text-sm text-gray-400 border-t border-gray-700 pt-4">
                         <p class="flex items-center mb-1">
                            <i class="bi bi-info-circle-fill text-indigo-400 mr-2 text-base"></i>
                            <span>Actualmente almacenado en:</span>
                         </p>
                         <div class="flex flex-wrap gap-2">
                            <c:forEach var="loc" items="${productoEditar.storedIn}">
                                <span class="px-2.5 py-1 inline-flex text-xs leading-5 font-semibold rounded-full capitalize
                                    <c:choose>
                                        <c:when test='${loc.equals("mysql")}'>bg-blue-800 text-blue-100</c:when>
                                        <c:when test='${loc.equals("mongo")}'>bg-green-800 text-green-100</c:when>
                                        <c:when test='${loc.equals("postgres")}'>bg-purple-800 text-purple-100</c:when>
                                        <c:when test='${loc.equals("excel")}'>bg-yellow-800 text-yellow-100</c:when>
                                        <c:otherwise>bg-gray-700 text-gray-200</c:otherwise>
                                    </c:choose>
                                ">${loc}</span>
                            </c:forEach>
                         </div>
                         <p class="mt-2 text-xs">La actualización se aplicará en estas fuentes.</p>
                     </div>
                </c:if>

                <div class="grid grid-cols-2 gap-4 items-center pt-3">
                    <div class="flex ${not empty productoEditar ? 'justify-start' : 'justify-end'}">
                        <c:choose>
                            <c:when test="${not empty productoEditar}">
                                <a href="${pageContext.request.contextPath}/ProductoServlet?action=listar" class="whitespace-nowrap inline-flex items-center justify-center py-2.5 px-5 border border-gray-600 shadow-sm text-sm font-medium rounded-md text-gray-300 bg-gray-700 hover:bg-gray-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 focus:ring-offset-gray-800 transition duration-150 ease-in-out">
                                    Cancelar
                                </a>
                            </c:when>
                            <c:otherwise>
                                <button type="reset" class="whitespace-nowrap inline-flex items-center justify-center py-2.5 px-5 border border-gray-600 shadow-sm text-sm font-medium rounded-md text-gray-300 bg-gray-700 hover:bg-gray-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 focus:ring-offset-gray-800 transition duration-150 ease-in-out">
                                    Limpiar
                                </button>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <div>
                        <button type="submit" class="w-full whitespace-nowrap inline-flex items-center justify-center py-2.5 px-5 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 focus:ring-offset-gray-800 transition duration-150 ease-in-out">
                            <c:choose>
                                <c:when test="${not empty productoEditar}">
                                    <i class="bi bi-save-fill mr-2"></i> Actualizar
                                </c:when>
                                <c:otherwise>
                                    <i class="bi bi-check-circle-fill mr-2"></i> Insertar
                                </c:otherwise>
                            </c:choose>
                        </button>
                    </div>
                </div>
            </div>
        </form>
    </div>
</div>
