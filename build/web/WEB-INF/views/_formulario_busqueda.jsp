<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<div class="bg-gray-800 shadow-lg rounded-xl border border-gray-700 overflow-hidden">
    <div class="px-6 py-4 bg-gray-700/50 border-b border-gray-700">
         <h3 class="text-lg font-semibold text-white flex items-center">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-2 text-teal-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"> <path stroke-linecap="round" stroke-linejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" /> </svg>
            Buscar Productos
        </h3>
    </div>
    <div class="p-6 md:p-8">
        <form action="${pageContext.request.contextPath}/ProductoServlet" method="get">
            <input type="hidden" name="action" value="buscar">
            <div class="flex flex-col sm:flex-row sm:space-x-3 space-y-3 sm:space-y-0">
                 <label for="nombreBusqueda" class="sr-only">Buscar por Nombre:</label>
                 <input type="text"
                        id="nombreBusqueda"
                        name="nombreBusqueda"
                        placeholder="Escribe el nombre..."
                        value="${not empty nombreBusquedaActual ? nombreBusquedaActual : ''}"
                        class="flex-grow block w-full rounded-md border-gray-600 bg-gray-700 text-white shadow-sm focus:border-indigo-500 focus:ring focus:ring-indigo-500 focus:ring-opacity-50 transition duration-150 ease-in-out hover:bg-gray-600/60 px-4 py-2 text-base"> <%-- Aumentado px-4 y text-base --%>

                 <button type="submit" class="inline-flex items-center justify-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-teal-600 hover:bg-teal-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-teal-500 focus:ring-offset-gray-800 transition duration-150 ease-in-out">
                     <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-1 -ml-1" viewBox="0 0 20 20" fill="currentColor"> <path fill-rule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z" clip-rule="evenodd" /> </svg>
                     Buscar
                 </button>
                 <a href="${pageContext.request.contextPath}/ProductoServlet?action=listar"
                    class="inline-flex items-center justify-center px-4 py-2 border border-gray-600 shadow-sm text-sm font-medium rounded-md text-gray-300 bg-gray-700 hover:bg-gray-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 focus:ring-offset-gray-800 transition duration-150 ease-in-out"
                    title="Mostrar todos los productos">
                    Todos
                 </a>
            </div>
        </form>
    </div>
</div>
