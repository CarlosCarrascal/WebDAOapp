<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="es" class="dark h-full">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Error - Gestión de Productos</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
</head>
<body class="bg-gray-900 text-gray-200 antialiased flex flex-col min-h-screen">

<div class="container mx-auto px-4 py-6 flex-grow flex items-center justify-center"> 
    <div class="max-w-lg w-full">
        <div class="bg-gray-800 shadow-xl rounded-xl p-8 border border-red-500/50">
            <div class="text-center">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-16 w-16 text-red-500 mx-auto mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1"> <path stroke-linecap="round" stroke-linejoin="round" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /> </svg>

                <h1 class="text-2xl font-bold text-red-400 mb-2">¡Oops! Ha ocurrido un error</h1>
                <p class="text-base text-gray-400 mb-6">Lo sentimos, no se pudo completar la operación solicitada.</p>

                <div class="bg-red-900/30 border border-red-600 text-red-300 px-4 py-3 rounded-md text-left mb-6" role="alert">
                    <strong class="font-bold block">Detalle del error:</strong>
                    <c:choose>
                        <c:when test="${not empty mensajeError}">
                            <span class="block sm:inline mt-1"><c:out value="${mensajeError}" /></span>
                        </c:when>
                        <c:otherwise>
                            <span class="block sm:inline mt-1">No se proporcionaron detalles específicos del error.</span>
                        </c:otherwise>
                    </c:choose>
                </div>

                <a href="${pageContext.request.contextPath}/ProductoServlet"
                   class="inline-flex items-center px-6 py-3 border border-transparent shadow-sm text-base font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 focus:ring-offset-gray-800 transition duration-150 ease-in-out">
                   <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor"> <path fill-rule="evenodd" d="M9.707 14.707a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 1.414L7.414 9H15a1 1 0 110 2H7.414l2.293 2.293a1 1 0 010 1.414z" clip-rule="evenodd" /> </svg>
                    Volver a la página principal
                </a>
            </div>
        </div>
    </div>
</div> 

<footer class="py-4 text-center text-sm text-gray-400 border-t border-gray-700 flex-shrink-0">
    &copy; ${yearNow} - Mi Aplicación de Ventas con Tailwind CSS.
    <jsp:useBean id="yearNowBeanFooter" class="java.util.Date" /> 
    <fmt:formatDate value="${yearNowBeanFooter}" pattern="yyyy" var="yearNow" />
</footer>

</body>
</html>
