<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<div class="bg-gray-800 shadow-lg rounded-xl border border-gray-700 overflow-hidden flex flex-col">
     <div class="px-6 py-3 bg-gray-700/50 border-b border-gray-700 flex-shrink-0">
         <h3 class="text-lg font-semibold text-white flex items-center">
             <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-2 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"> <path stroke-linecap="round" stroke-linejoin="round" d="M3 10h18M3 14h18m-9-4v8m-7 0h14a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z" /> </svg>
             Listado de Productos
         </h3>
     </div>
     <div class="overflow-y-auto max-h-96">
         <div class="overflow-x-auto align-middle inline-block min-w-full">
            <table class="min-w-full divide-y divide-gray-700">
                <thead class="bg-gray-700 sticky top-0 z-10">
                    <tr>
                        <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-400 uppercase tracking-wider">Código</th>
                        <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-400 uppercase tracking-wider">Nombre</th>
                        <th scope="col" class="px-6 py-3 text-right text-xs font-medium text-gray-400 uppercase tracking-wider">Precio</th>
                        <th scope="col" class="px-6 py-3 text-right text-xs font-medium text-gray-400 uppercase tracking-wider">Stock</th>
                        <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-400 uppercase tracking-wider">Almacenado En</th>
                        <th scope="col" class="px-6 py-3 text-center text-xs font-medium text-gray-400 uppercase tracking-wider">Acciones</th>
                    </tr>
                </thead>
                <tbody class="bg-gray-800 divide-y divide-gray-700">
                    <c:choose>
                        <c:when test="${not empty listaProductos}">
                            <c:forEach var="prod" items="${listaProductos}">
                                <tr class="hover:bg-gray-700/60 transition-colors duration-150">
                                    <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-white"><c:out value="${prod.codigo}" /></td>
                                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-300"><c:out value="${prod.nombre}" /></td>
                                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-300 text-right">
                                        <fmt:setLocale value="es_PE" />
                                        <fmt:formatNumber value="${prod.precio}" type="currency" />
                                    </td>
                                    <td class="px-6 py-4 whitespace-nowrap text-sm text-right">
                                        <span class="px-2.5 py-0.5 inline-flex text-xs leading-5 font-semibold rounded-full
                                            ${prod.stock < 10 ? 'bg-red-800 text-red-100' :
                                              (prod.stock < 50 ? 'bg-yellow-800 text-yellow-100' :
                                               'bg-green-800 text-green-100')}">
                                            <fmt:formatNumber value="${prod.stock}" type="number" groupingUsed="false"/>
                                        </span>
                                    </td>
                                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-400">
                                        <div class="flex flex-wrap gap-1 items-center">
                                            <c:if test="${not empty prod.storedIn}">
                                                <c:forEach var="loc" items="${prod.storedIn}">
                                                    <span class="px-2 py-0.5 inline-flex text-xs leading-5 font-semibold rounded-full capitalize
                                                        <c:choose>
                                                            <c:when test='${loc.equals("mysql")}'>bg-blue-700 text-blue-100</c:when>
                                                            <c:when test='${loc.equals("mongo")}'>bg-green-700 text-green-100</c:when>
                                                            <c:when test='${loc.equals("postgres")}'>bg-purple-700 text-purple-100</c:when>
                                                            <c:when test='${loc.equals("excel")}'>bg-yellow-700 text-yellow-100</c:when>
                                                            <c:otherwise>bg-gray-600 text-gray-200</c:otherwise>
                                                        </c:choose>
                                                    ">${loc}</span>
                                                </c:forEach>
                                            </c:if>
                                            <c:if test="${empty prod.storedIn}">
                                                <span class="italic text-gray-500">N/A</span>
                                            </c:if>
                                        </div>
                                    </td>
                                    <td class="px-6 py-4 whitespace-nowrap text-center text-sm font-medium space-x-3">
                                        <%-- MODIFICADO: Añadir &source=${prod.storedIn[0]} (asume storedIn no está vacío) --%>
                                        <c:if test="${not empty prod.storedIn}">
                                            <a href="${pageContext.request.contextPath}/ProductoServlet?action=mostrarEditar&codigo=${prod.codigo}&source=${prod.storedIn[0]}"
                                               class="text-indigo-400 hover:text-indigo-300 transition-colors duration-150"
                                               title="Editar Producto">
                                                <i class="bi bi-pencil-square text-base"></i>
                                            </a>
                                            <a href="${pageContext.request.contextPath}/ProductoServlet?action=eliminar&codigo=${prod.codigo}&source=${prod.storedIn[0]}"
                                               class="text-red-500 hover:text-red-400 transition-colors duration-150"
                                               title="Eliminar Producto"
                                               onclick="return confirm('¿Estás seguro de que deseas eliminar el producto \'${prod.nombre}\' (Código: ${prod.codigo}, Origen: ${prod.storedIn[0]}) de TODAS sus ubicaciones conocidas?');">
                                                <i class="bi bi-trash3-fill text-base"></i>
                                            </a>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="6" class="px-6 py-10 text-center text-sm text-gray-400 italic">
                                    No hay productos para mostrar.
                                </td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
         </div>
     </div>
</div>
